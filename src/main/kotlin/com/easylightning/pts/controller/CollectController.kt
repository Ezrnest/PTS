package com.easylightning.pts.controller

import com.easylightning.pts.dao.mysql.UserDAO
import com.easylightning.pts.pojo.Datum
import com.easylightning.pts.pojo.SensorData
import com.easylightning.pts.pojo.SensorInfo
import com.easylightning.pts.service.DataService
import com.easylightning.pts.service.UserService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.SQLException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.emptyMap
import kotlin.collections.listOf
import kotlin.collections.set
import kotlin.random.Random


/**
 * 处理收集信息等
 *
 * Created by liyicheng at 2020-11-02 20:56
 */
@RestController
@RequestMapping()
class CollectController {

    companion object {
        val logger = LogManager.getLogger()
        const val RESPONSE_INVALID = "invalid" // invalid authentication
        const val RESPONSE_SUCCESS = "success" //
        const val RESPONSE_FAILURE = "failure" //
    }

    @Value("\${app.isDev}")
    var isDev: Boolean = false


    @Autowired
    lateinit var userService: UserService


    @Autowired
    lateinit var sensorService: DataService

    @Autowired
    lateinit var userDAO: UserDAO

    @Autowired
    lateinit var dataService: DataService


    @PostMapping("/collect/register")
    fun register(@RequestParam username: String,
                 @RequestParam password: String,
                 @RequestParam imei: String): String {
        val result = userService.registerUser(username, password)
        return if (result) {
            logger.info("Successfully registered user: $username")
            RESPONSE_SUCCESS
        } else {
            logger.info("Failed to register user: $username")
            RESPONSE_FAILURE
        }
    }


    @PostMapping("/collect/registerSensor")
    fun registerSensors(@RequestBody sensorInfo: SensorInfo): String {
        val user = userService.checkAuthentication(sensorInfo.username, sensorInfo.password)
        if (user == null) {
            logger.info("Invalid authentication: ${sensorInfo.username} : ${sensorInfo.password}")
            return RESPONSE_INVALID
        }
        val success = sensorService.registerSensors(user, sensorInfo)
        return if (success) {
            logger.info("Successfully registered sensor: ${sensorInfo.username} - ${sensorInfo.sensors}")
            RESPONSE_SUCCESS
        } else {
            logger.info("Failed to register sensor for user: ${sensorInfo.username}")
            RESPONSE_FAILURE
        }
    }


    @GetMapping("/collect/testRegister")
    fun test(): String {
        if (!isDev) {
            return RESPONSE_INVALID
        }
        logger.info("Testing register...")
        val ss = SensorInfo(
                "test", "pass", "imei0",
                listOf("s1", "s2")
        )
        return registerSensors(ss)
    }

    //
    @GetMapping("/collect/testAdd")
    fun testAdd(): String {
        if (!isDev) {
            return RESPONSE_INVALID
        }
        logger.info("Testing add data...")
        val ss = SensorData("test", "pass", "imei0",
                listOf("s1", "s2"),
                listOf(System.currentTimeMillis(), System.currentTimeMillis() + 1L),
                listOf(
                        listOf(Random.nextFloat(), Random.nextFloat() + 2f),
                        listOf(null, Random.nextFloat() + 2f)
                )
        )
        logger.info("added")
        return addData(ss)
    }

    @PostMapping("/collect/addData")
    fun addData(@RequestBody sensorData: SensorData): String {
//        val user = userService.checkAuthentication(
//                sensorData.username, sensorData.password)
//        logger.info("Adding data from ${sensorData.username}")
        val pair = userService.checkDevice(sensorData.username, sensorData.password, sensorData.imei)
        if (pair == null) {
            logger.warn("Device doesn't exist: ${sensorData.username}.${sensorData.imei}")
            return RESPONSE_INVALID
        }
        if (!sensorData.checkDataValid()) {
            logger.warn("Invalid data from ${sensorData.username}")
            return RESPONSE_INVALID
        }
        val success = sensorService.addData(pair.second, sensorData)
        return if (success) {
            RESPONSE_SUCCESS
        } else {
            RESPONSE_FAILURE
        }
    }


    /*
    Methods for compatibility:
     */
    @RequestMapping(value = ["/api/ios/addDatum"], method = [RequestMethod.POST])
    fun addDatum(@RequestBody datum: Datum): String {
//        println("开始新增...")
        val (sensors, values) = datum.values.toList().unzip()
        val sd = SensorData(datum.user, datum.password, datum.imei, sensors, listOf(datum.time), listOf(values))
//        return this.phoneSensorService.addDatum(datum)
        return addData(sd)
    }

    @RequestMapping(value = ["/api/ios/registerPhone"], method = [RequestMethod.POST])
    fun addPhone(@RequestParam(defaultValue = "imei") imei: String,
                 @RequestParam(defaultValue = "user") user: String,
                 @RequestParam(defaultValue = "password") password: String,
                 @RequestParam(defaultValue = "sensor") sensor: String): String {


        return registerSensors(SensorInfo(user, password, imei, listOf(sensor)))
//        println("注册$imei.$sensor")
//        return this.phoneSensorService.registerPhoneSensor(user, imei, sensor)
    }

    @RequestMapping(value = ["/api/ios/registerPhoneSensors"], method = [RequestMethod.POST])
    @Throws(SQLException::class)
    fun addPhoneSensors(@RequestParam imei: String, @RequestParam user: String, @RequestParam password: String,
                        @RequestParam sensors: List<String>): String {
        return registerSensors(SensorInfo(user, password, imei, sensors))
//        println("注册$imei:$sensors")
//        val success= ArrayList<String>()
//        val failed = ArrayList<String>()
//        val failedMessage = ArrayList<String>()
//        val var9 = sensors.size
//        for (var10 in 0 until var9) {
//            val sensor = sensors[var10]
//            try {
//                this.phoneSensorService.registerPhoneSensor(user, imei, sensor)
//                success.add(sensor)
//            } catch (var13: SQLException) {
//                failed.add(sensor)
//                failedMessage.add(var13.message)
//            }
//        }
//        return if (failed.size == 0) {
//            ResponseEntity<Any?>(emptyMap<Any, Any>(), HttpStatus.OK)
//        } else {
//            val map: MutableMap<String?, Any?> = HashMap<Any?, Any?>()
//            map["success"] = success
//            map["failed"] = failed
//            map["failedMessages"] = failedMessage
//            ResponseEntity<Any?>(map, HttpStatus.BAD_REQUEST)
//        }
    }


    @RequestMapping("/collect/testSelect")
    fun testSelect(): String {
        if (!isDev) {
            return RESPONSE_INVALID
        }
        val user = userDAO.findByUsername("test").get()
        val uid = user.uid!!
        val sensorData = dataService.recentData(uid, 15, 15)
        val result = buildString {
            for (row in sensorData.data) {
                row.data.joinTo(this, ",") {
                    it.toString()
                }
                appendln()
            }
        }
        return result
    }

//    @RequestMapping("/collect/testImage", produces = [MediaType.IMAGE_PNG_VALUE])
//    @ResponseBody
//    fun testImage(): ByteArray {
//        if (!isDev) {
//            return ByteArray(0)
//        }
//        val data = "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"
//        val result = Base64.getDecoder().decode(data)
////    """
////        <img src=“data:image/png;base64,”/>
////    """.trimIndent()
//        return result
//    }

//    @RequestMapping("/collect/testImage", produces = [MediaType.TEXT_HTML_VALUE])
//    @ResponseBody
//    fun testImage(): String {
//        if (!isDev) {
//            return "ByteArray(0)"
//        }
//        val data = "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"
//        val html = """
//            <html>
//            <body>
//            <img src="data:image/png;base64, $data">
//            </body>
//            </html>
//        """.trimIndent()
////        val result = Base64.getDecoder().decode(data)
////    """
////        <img src=“data:image/png;base64,”/>
////    """.trimIndent()
//        return html
//    }
}