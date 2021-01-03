package com.easylightning.pts.service

import com.easylightning.pts.dao.iotdb.SensorDataDAO
import com.easylightning.pts.dao.mysql.UserDeviceDAO
import com.easylightning.pts.entity.User
import com.easylightning.pts.entity.UserDevice
import com.easylightning.pts.pojo.DataTable
import com.easylightning.pts.pojo.SensorData
import com.easylightning.pts.pojo.SensorInfo
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant


/**
 * 收集数据、提供数据的服务
 *
 * Created by liyicheng at 2020-11-02 20:18
 */
@Service
@Transactional
class DataService {
    val logger = LogManager.getLogger()

    @Autowired
    lateinit var sensorDataDAO: SensorDataDAO

    @Autowired
    lateinit var deviceDAO: UserDeviceDAO

    private val sensorNamePattern = "\\w+".toRegex()

    private fun checkSensorNames(names: List<String>): Boolean {
        return names.all { sensorNamePattern.matches(it) }
    }

    /**
     * Register all the sensors for a user.
     *
     * @return `true` if the sensors are successfully registered or they have been registered previously. Returns
     * `false` if the names of sensors are invalid or error occurs.
     */
    @Transactional
    fun registerSensors(user: User, sensorInfo: SensorInfo): Boolean {
        if (!checkSensorNames(sensorInfo.sensors)) {
            logger.warn("Invalid sensor names: ${sensorInfo.sensors}")
            return false
        }
        val username = sensorInfo.username
        val uid = user.uid!!
        val op = deviceDAO.findByUidAndImei(uid, sensorInfo.imei)
        val device = op.orElseGet {
            val device = UserDevice()
            device.imei = sensorInfo.imei
            device.uid = user.uid
            deviceDAO.save(device)
        }
        val tid = device.tid!!

        return try {
            for (s in sensorInfo.sensors) {
                if (!sensorDataDAO.existSensor(uid, tid, s)) {
                    sensorDataDAO.registerSensor(uid, tid, s)
                    logger.debug("Registered sensor: $username : $s")
                }
            }
            true
        } catch (e: DataAccessException) {
            logger.warn(e)
            false
        }

    }

    fun addData(device: UserDevice, sensorData: SensorData): Boolean {
        if (!checkSensorNames(sensorData.sensors)) {
            logger.warn("Invalid sensor names: ${sensorData.sensors}")
            return false
        }

        return try {
            sensorDataDAO.addData(device.uid!!, device.tid!!, sensorData)
            true
        } catch (e: DataAccessException) {
            logger.warn(e.localizedMessage)
            false
        }
    }

    fun dataCount(uid: Long): Int {
        return try {
            sensorDataDAO.dataCount(uid)
        } catch (e: DataAccessException) {
            logger.warn(e.localizedMessage)
            -1
        }
    }

    fun lastUpdateTime(uid: Long): Instant {
        return try{
            Instant.ofEpochMilli(sensorDataDAO.latestDataTime(uid))
        }catch (e: DataAccessException) {
            logger.warn(e.localizedMessage)
            Instant.EPOCH
        }
    }

    fun lastUpdateTime(uid : Long, tid : Long) : Instant{
        return try{
            Instant.ofEpochMilli(sensorDataDAO.latestDataTime(uid,tid))
        }catch (e: DataAccessException) {
//            logger.warn(e.localizedMessage)
            Instant.EPOCH
        }

    }

    /**
     *
     * @param duration in minute
     */
    fun recentData(uid : Long,duration : Int, limit : Int = 10) : DataTable{
        return try{
//            device.
            val dt = sensorDataDAO.recentData(uid,duration,limit)
            val cols = dt.columns.map { it.substringAfterLast('.') }
//            val sorted =
            DataTable(cols,dt.data)
        }catch (e: DataAccessException) {
            logger.warn(e)
            return DataTable.EMPTY

        }
    }

//    fun lastActiveTable(uid : Long){
//        return try{
//            val table =
//        }
//    }
}