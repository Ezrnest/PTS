package com.easylightning.pts.dao.mysql

import com.easylightning.pts.entity.Report
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.*

@Table("report")
interface ReportDAO : JpaRepository<Report, Long> {

    fun findByUidAndDateAndName(uid: Long, date: LocalDate, name: String): Optional<Report>

    fun existsByUidAndDateAndName(uid: Long, date: LocalDate, name: String): Boolean

    @Modifying
    @Query("DELETE FROM pts.report WHERE `date` < ?", nativeQuery = true)
    fun removeObsolete(date: LocalDate)

    fun removeByUid(uid: Long)
}