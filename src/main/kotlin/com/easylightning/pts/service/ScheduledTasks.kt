package com.easylightning.pts.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.config.ScheduledTask
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*




//Created by lyc at 2020-12-13 21:24

/**
 *
 *
 * @author liyicheng
 */
@Component
class ScheduledTasks  {

    @Autowired
    lateinit var infoService: InfoService



    companion object {
        val logger = LogManager.getLogger()
    }
    @Scheduled(cron = "0 0 3 * * * ")
    fun updateDailyReports(){
        infoService.dailyUpdate()
    }

}