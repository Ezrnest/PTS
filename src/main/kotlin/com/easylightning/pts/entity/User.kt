package com.easylightning.pts.entity

import javax.persistence.*

@Entity
@Table(name = "user")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var uid : Long?= null

    var username: String? = null

//    var imei: String? = null

    var password: String? = null



}