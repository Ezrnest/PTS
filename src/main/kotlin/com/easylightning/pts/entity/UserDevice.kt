package com.easylightning.pts.entity

import java.io.Serializable
import javax.persistence.*


@Entity
@Table(name = "user_device")
class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var tid: Long? = null

    var uid : Long ? =null
    var imei : String ? =null

}