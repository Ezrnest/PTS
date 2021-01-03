package com.easylightning.pts.entity

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity(name = "report")
class Report(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var hid: Long? = null,

        var uid: Long? = null,

        var date: LocalDate? = null,

        var name: String? = null,

        var type: String? = null,

        var content: String? = null

) {


}