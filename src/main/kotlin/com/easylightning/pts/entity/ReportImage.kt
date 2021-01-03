package com.easylightning.pts.entity

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity(name = "report_image")
class ReportImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var iid: Long? = null

    var uid: Long? = null

    var date: LocalDate? = null

    var type: Int? = null

    var image: ByteArray? = null
}