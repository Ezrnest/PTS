package com.easylightning.pts.controller

import com.easylightning.pts.dao.mysql.UserDAO
import com.easylightning.pts.entity.User
import com.easylightning.pts.service.DataService
import com.easylightning.pts.service.InfoService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * 负责处理个人报表信息等。
 *
 */
@Controller
class InfoController(env: Environment) {

    @Autowired
    lateinit var userDAO: UserDAO

    @Autowired
    lateinit var dataService: DataService

    @Autowired
    lateinit var infoService: InfoService

    @Value("\${app.isDev}")
    var isDev: Boolean = false

    private val logger = LogManager.getLogger()

    @RequestMapping("/me")
    fun homePage(
            request: HttpServletRequest, model: Model
    ): String {
        val username = request.remoteUser
        val op = userDAO.findByUsername(username)
        if (op.isEmpty) {
            return "error"
        }
        val user = op.get()
        val nickname = user.username
        model.addAttribute("username", request.remoteUser)
        model.addAttribute("nickname", nickname)

        val uid = user.uid!!
        val count = dataService.dataCount(uid)
        val lastUpdateTime = dataService.lastUpdateTime(uid)
        model.addAttribute("dataCount", count)
        model.addAttribute("lastUpdateTime", lastUpdateTime)


        val sensorData = dataService.recentData(uid, 15, 15) // 15 minutes
        model.addAttribute("sensorData", sensorData)

        return "home"
    }

    private fun getYesterday(): LocalDate {
        return LocalDate.now().minusDays(1)
    }

    private fun parseDate(dateStr: String): LocalDate {
        val date = if (dateStr.isEmpty()) {
            getYesterday()
        } else {
            LocalDate.parse(dateStr)
        }
        return date
    }

//    val REPORT_NAMES : List<String>
//    init{
//        REPORT_NAMES
//    }

    private val REPORT_NAMES: Set<String>

    init {
        val t = mutableSetOf<String>()
        t.addAll(env.getProperty("app.reportSVG", "").split(","))
        t.addAll(env.getProperty("app.reportPNG", "").split(","))
        REPORT_NAMES = t
    }

    private fun checkAuth(request: HttpServletRequest): User? {
        val username = request.remoteUser
        val op = userDAO.findByUsername(username)
        if (op.isEmpty) {
            return null
        }
        return op.get()
    }

    @RequestMapping("/me/reports")
    fun reports(request: HttpServletRequest,
                @RequestParam(name = "date", defaultValue = "", required = false) dateStr: String,
                model: Model): String {
        val user = checkAuth(request) ?: return "error"
        val date = parseDate(dateStr)
        val nickname = user.username
        model.addAttribute("username", request.remoteUser)
        model.addAttribute("nickname", nickname)
        model.addAttribute("date", date)
//        val reportNames = infoService.dailyReports.asSequence() + infoService.weeklyReports.asSequence()
        val reports = REPORT_NAMES.map { name ->
            name to infoService.getReport(user, date, name)
        }
//        model.addAttribute("reports",reports)
        for ((name, report) in reports) {
            model.addAttribute(name, report?.content.toString())
        }

        val now = LocalDate.now()
        val prevWeek = (1..7).map { now.minusDays(it.toLong()) }
        model.addAttribute("prevWeek", prevWeek)
        return "reports"
    }

    @RequestMapping("/me/clear")
    fun clearReports(request: HttpServletRequest): String {
        val user = checkAuth(request) ?: return "redirect:/me"
        infoService.clearAllReports(user.uid!!)
        return "redirect:/me"
    }

    @RequestMapping("/me/loadAll")
    fun loadAllReports(request: HttpServletRequest,
                       @RequestParam(name = "date", defaultValue = "", required = false) dateStr: String): String {
        val user = checkAuth(request) ?: return "redirect:/me"
        val date = parseDate(dateStr)
        infoService.updateAllReports(user.uid!!, date)
        return "redirect:/me"
    }


    @GetMapping("/me/reportImage", produces = [MediaType.IMAGE_PNG_VALUE])
    @ResponseBody
    fun reportImage(request: HttpServletRequest,
                    @RequestParam(defaultValue = "daily", required = false) type: String,
                    @RequestParam(name = "date", defaultValue = "", required = false) dateStr: String
    ): ByteArray {
        val user = checkAuth(request) ?: return ByteArray(0)

        val date = if (dateStr.isEmpty()) {
            getYesterday()
        } else {
            LocalDate.parse(dateStr)
        }

        return infoService.getDailyReportImage(user, date)
    }


//    private val CONTENT_TYPE_MAPPING = mapOf(
//            "html" to "text/html",
//            "svg" to "image/svg+xml"
//    )


    private fun reportText(request: HttpServletRequest, response: HttpServletResponse,
                           name: String,
                           dateStr: String): String {
        val user = checkAuth(request) ?: return "error"
        val date = parseDate(dateStr)

        val report = infoService.getReport(user, date, name) ?: return "error"
//        val type = CONTENT_TYPE_MAPPING.getOrDefault(report.type, "text/html")
//        response.contentType = type
//        logger.info("Returning report: name=$name, report.type=${report.type}, html.type=$type")
        return report.content!!
    }

    @GetMapping("/me/reportHtml", produces = [MediaType.TEXT_HTML_VALUE]) //
    @ResponseBody
    fun reportHtml(request: HttpServletRequest, response: HttpServletResponse,
                   @RequestParam(defaultValue = "heat", required = false) name: String,
                   @RequestParam(name = "date", defaultValue = "", required = false) dateStr: String
    ): String {
        return reportText(request, response, name, dateStr)
    }


    @GetMapping("/me/reportSVG", produces = ["image/svg+xml"]) //
    @ResponseBody
    fun reportSVG(request: HttpServletRequest, response: HttpServletResponse,
                  @RequestParam(required = true) name: String,
                  @RequestParam(name = "date", defaultValue = "", required = false) dateStr: String

    ): String {
        return reportText(request, response, name, dateStr)
    }

    @GetMapping("/me/reportPNG", produces = [MediaType.IMAGE_PNG_VALUE]) //
    @ResponseBody
    fun reportPNG(request: HttpServletRequest, response: HttpServletResponse,
                  @RequestParam(required = true) name: String,
                  @RequestParam(name = "date", defaultValue = "", required = false) dateStr: String
    ): ByteArray {
        val text = reportText(request, response, name, dateStr)
        return Base64.getDecoder().decode(text)
    }


}