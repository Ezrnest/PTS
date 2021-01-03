package com.easylightning.pts.dao.mysql

import com.easylightning.pts.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import java.util.*

@Table("user_device")
interface UserDeviceDAO : JpaRepository<UserDevice,Long>{

    fun findByUidAndImei(uid: Long, imei: String) : Optional<UserDevice>

    fun existsByUidAndImei(uid: Long, imei: String) : Boolean
//    fun save

    fun findAllByUid(uid : Long) : List<UserDevice>
}