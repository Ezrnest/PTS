package com.easylightning.pts.dao.iotdb

import com.easylightning.pts.pojo.DataRow
import com.easylightning.pts.pojo.DataTable
import com.easylightning.pts.pojo.SensorData
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant


/*
 * Created by liyicheng at 2020-11-02 22:36
 */

@Suppress("SqlResolve")
@Repository
class SensorDataDAO {


    val logger = LogManager.getLogger()

    @Autowired
    @Qualifier("iotdbTemplate")
    lateinit var template: JdbcTemplate


    fun existSensor(uid: Long, tid: Long, sensor: String): Boolean {
        val query = "SHOW TIMESERIES root.phone.u${uid}.t${tid}.${sensor}"
        return template.queryForList(query).size > 0
    }

    fun registerSensor(uid: Long, tid: Long, sensor: String) {
        val query = "CREATE TIMESERIES root.phone.u${uid}.t${tid}.${sensor} WITH DATATYPE=FLOAT, ENCODING=GORILLA"
        template.update(query)
    }

    //    private fun makeInsertSql(sensorData: SensorData) : String {
//        val username = sensorData.username
//        val query = buildString {
//            append("INSERT INTO root.phone.")
//            append(username)
//            append("(timestamp,")
//            sensorData.columns.joinTo(this, ",", postfix = ")")
//            append(" VALUES(?")
//            repeat(sensorData.columns.size){
//                append(",?")
//            }
//            append(")")
//        }
//        return query
//    }
    private fun makeInsertSqls(uid: Long, tid: Long, sensorData: SensorData): List<String> {

        val queryBase = buildString {
            append("INSERT INTO root.phone.")
            append("u").append(uid)
            append(".")
            append("t").append(tid)
            append("(timestamp,")
        }
        val sqls = sensorData.times.zip(sensorData.values).map { (time, value) ->
            buildString {
                append(queryBase)

                val data = sensorData.sensors.zip(value).filterNot { it.second == null }

                data.joinTo(this, ",", postfix = ")") {
                    it.first
                }
                append(" VALUES(")
                append(time)
                data.joinTo(this, ",", ",", ")") {
                    it.second!!.toString()
                }
            }
        }
        return sqls
    }

    fun addData(uid: Long, tid: Long, sensorData: SensorData) {
        val sqls = makeInsertSqls(uid, tid, sensorData)
        template.batchUpdate(*sqls.toTypedArray())
    }

    fun dataCount(uid: Long): Int {
        val sql = "SELECT COUNT(*) FROM root.phone.u${uid}"
        return template.queryForMap(sql).values.map {
            if (it is String) {
                it.toInt()
            } else {
                0
            }
        }.sum() ?: 0
    }

    fun latestDataTime(uid: Long): Long {
        val sql = "SELECT MAX_TIME(*) FROM root.phone.u${uid}"
        return template.queryForMap(sql).values.map {
            if (it is String) {
                it.toLong()
            } else {
                0L
            }
        }.max() ?: 0L
    }

    fun latestDataTime(uid: Long, tid: Long): Long {
        val sql = "SELECT MAX_TIME(*) FROM root.phone.u${uid}.t${tid}"
        return template.queryForMap(sql).values.map {
            if (it is String) {
                it.toLong()
            } else {
                0L
            }
        }.max() ?: 0L
    }


    fun recentData(uid: Long, duration: Int, limit: Int): DataTable {
        val sql = "SELECT * FROM root.phone.u${uid} WHERE time > NOW() - ${duration}m LIMIT $limit"
        return template.query<DataTable>(sql) { rs ->
            val metadata = rs.metaData
            val n1 = (2..metadata.columnCount).map { metadata.getColumnLabel(it) }
            val times = arrayListOf<Instant>()
            val c1 = (2..metadata.columnCount).map { arrayListOf<Any?>() }
//            val data =  arrayListOf<DataRow>()
            while (rs.next()) {
                val time = Instant.ofEpochMilli(rs.getLong(1))
                times += time
                for (i in 2..metadata.columnCount) {
                    c1[i - 2] += rs.getString(i)
                }
            }
            val (n2, c2) = n1.zip(c1).filterNot { (_, c) -> c.all { it == null } }.unzip()
            val names = arrayListOf("Time").also { it.addAll(n2) }
//            val data = c2.fold(A)
            val data = arrayListOf<DataRow>()
            for (i in times.indices) {
                val row = ArrayList<Any?>(c1.size)
                for (c in c2) {
                    row += c[i]
                }
                data += DataRow(times[i], row)
            }
            DataTable(names, data)
        } ?: DataTable.EMPTY
    }

//    fun lastActiveTable(uid : Long) : Long{
//        val sql = "SELECT * FROM root.phone.u${uid} WHERE time > NOW() - ${duration}m LIMIT $limit"
//    }

}