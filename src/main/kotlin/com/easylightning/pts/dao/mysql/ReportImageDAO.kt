package com.easylightning.pts.dao.mysql

import com.easylightning.pts.entity.ReportImage
import com.easylightning.pts.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.util.*

@Table("report_image")
interface ReportImageDAO : JpaRepository<ReportImage, Long> {

    fun findByUidAndDateAndType(uid: Long, date: LocalDate, type: Int): Optional<ReportImage>

    @Modifying
    @Query("DELETE FROM pts.report_image WHERE date < ?",nativeQuery = true)
    fun removeObsolete(date: LocalDate)
}