package com.easylightning.pts.service

import com.easylightning.pts.dao.mysql.ReportDAO
import com.easylightning.pts.dao.mysql.ReportImageDAO
import com.easylightning.pts.entity.Report
import com.easylightning.pts.entity.ReportImage
import com.easylightning.pts.entity.User
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate

@Controller
@Transactional
class InfoService(env: Environment) {
    val logger = LogManager.getLogger()

    @Autowired
    lateinit var reportImageDAO: ReportImageDAO

    @Autowired
    lateinit var reportDAO: ReportDAO

    @Autowired
    lateinit var userService: UserService

    val TYPE_DAILY = 0

    @Value("\${app.reportPythonPathImage}")
    var pythonPathImage: String = ""

    @Value("\${app.reportPythonPathHtml}")
    var pythonPathHtml: String = ""

    @Value("\${app.python}")
    var pythonExe: String = "python"

//    @Autowired
//    lateinit

    final val dailyReports: Set<String>
    final val weeklyReports: Set<String>


    init {
        dailyReports = env.getProperty("app.dailyReports", "").split(",").map {
            it.trim()
        }.toSet()
        weeklyReports = env.getProperty("app.weeklyReports", "").split(",").map {
            it.trim()
        }.toSet()
    }


    private val REPORT_INFO_REGEX = "reportName=(\\w+),type=(\\w+)".toRegex()

    fun dailyUpdate() {
        //TODO
        logger.info("Updating reports...")

//        for(u in )
        val yesterday = LocalDate.now().minusDays(1)
        val users = userService


        logger.info("Clearing obsolete reports...")
        clearObsoleteReports()
    }

    private fun <T> executePython(cmd: Array<String>, f: (InputStream) -> T): T {
        val process = Runtime.getRuntime().exec(cmd)
        val ins = process.inputStream
        val errOut = process.errorStream
        val result = f(ins)
        ins.close()
        val br = BufferedReader(InputStreamReader(errOut))
        val errMessage = br.readText()
        if (errMessage.isNotBlank()) {
            logger.warn("Error in executing ${cmd.contentToString()}, message:")
            logger.warn(errMessage)
        }
        errOut.close()
        return result
    }


    fun generateReport(uid: Long, start: LocalDate, end: LocalDate, names: Collection<String>): Map<String, Report> {
        val p = userService.activeDevice(uid)
        if (p == null) {
            logger.info("No active device: ${uid}")
            return emptyMap()
        }
        val tid = p.first.tid!!


        val tableName = "root.phone.u${uid}.t${tid}"

        val exe = pythonExe
        val command = pythonPathHtml
        val cmds = if (names.isEmpty()) {
            arrayOf(exe, command, tableName, start.toString(), end.toString())
        } else {
            arrayOf(exe, command, tableName, start.toString(), end.toString(), names.joinToString(separator = ","))
        }

        return executePython(cmds) { ins ->
            val reader = BufferedReader(InputStreamReader(ins))
            val results = mutableMapOf<String, Report>()
            while (true) {
                val nameLine: String = reader.readLine()?.replace(" ", "") ?: break
                val m = REPORT_INFO_REGEX.matchEntire(nameLine)
                if (m == null) {
                    logger.warn("Bad output: ${nameLine.take(20)}")
                    continue
                }
//                if (!REPORT_INFO_REGEX.matches(nameLine)) {
//                    continue
//                }
                val name = m.groupValues[1]
                val type = m.groupValues[2]
                val content: String = reader.readLine() ?: break
                results[name] = Report(null, uid, end, name, type, content)
            }
            results
        }
    }

    private fun saveReport(report: Report) {
        if (reportDAO.existsByUidAndDateAndName(report.uid!!, report.date!!, report.name!!)) {
            return
        }
        reportDAO.save(report)
    }

    private fun updateReport(uid: Long, start: LocalDate, end: LocalDate, types: Collection<String> = emptyList()):
            Map<String, Report> {
        logger.info("Updating report: $uid - ${types.joinToString(",")}")
        val results = generateReport(uid, start, end, types)
        for ((_, report) in results) {
            saveReport(report)
        }
        return results
    }

    fun updateAllReports(uid: Long, date: LocalDate) {
        logger.info("Updating all reports...")
        updateDailyReport(uid, date, dailyReports)
        updateWeeklyReport(uid, date, weeklyReports)
    }


    fun updateDailyReport(uid: Long, date: LocalDate, types: Collection<String> = emptyList()):
            Map<String, Report> {
        return updateReport(uid, date, date, types)
    }


    fun updateWeeklyReport(uid: Long, date: LocalDate, types: Collection<String> = emptyList()):
            Map<String, Report> {
        val start = date.minusDays(6)
        return updateReport(uid, start, date, types)
    }


    fun clearAllReports(uid: Long) {
//        val now = LocalDate.now()
        reportDAO.removeByUid(uid)
//        reportImageDAO.removeObsolete(now)
    }

    fun clearObsoleteReports() {
        val now = LocalDate.now()
        val d = now.minusDays(7)
        reportImageDAO.removeObsolete(d)
        reportDAO.removeObsolete(d)
    }

//    fun existDailyReport(uid : Long, day : Instant) : Boo

    fun getReport(user: User, date: LocalDate, name: String): Report? {
        val uid = user.uid!!
        val op = reportDAO.findByUidAndDateAndName(uid, date, name)
        if (op.isPresent) {
            logger.info("Using cached report: ${user.username} $name")
            return op.get()
        }

        val rm = when (name) {
            in dailyReports -> {
                updateDailyReport(uid, date, listOf(name))
            }
            in weeklyReports -> {
                updateWeeklyReport(uid, date, listOf(name))
            }
            else -> {
                logger.warn("Invalid report name: $name")
                return null
            }
        }
        return rm[name]
    }


    private fun updateDailyReportImage(uid: Long, date: LocalDate): ByteArray {
        val result = generateDailyReportImage(uid, date)
        val reportImage = ReportImage()
        reportImage.uid = uid
        reportImage.date = date
        reportImage.type = 0
        reportImage.image = result
        reportImageDAO.save(reportImage)
        return result
    }

    fun generateDailyReportImage(uid: Long, date: LocalDate): ByteArray {
        val p = userService.activeDevice(uid) ?: return ByteArray(0)
        val tid = p.first.tid!!
        val year = date.year.toString()
        val month = date.month.value.toString()
        val day = date.dayOfMonth.toString()

        val tableName = "root.phone.u${uid}.t${tid}"

        val exe = pythonExe
        val command = pythonPathImage
        val cmds = arrayOf(exe, command, tableName, year, month, day)

        return executePython(cmds) { ins ->
            val dis = DataInputStream(ins)
            dis.readAllBytes()
        }
    }

    fun getDailyReportImage(user: User, date: LocalDate): ByteArray {
        val uid = user.uid!!
        val op = reportImageDAO.findByUidAndDateAndType(uid, date, TYPE_DAILY)
        return if (op.isEmpty) {
            logger.info("Updating daily image: ${user.username}")
            updateDailyReportImage(uid, date)
        } else {
            logger.info("Using cached daily image: $uid")
            val reportImage = op.get()
            reportImage.image!!
        }
    }

}