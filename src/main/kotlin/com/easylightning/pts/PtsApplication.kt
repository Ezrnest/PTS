package com.easylightning.pts

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class PtsApplication

fun main(args: Array<String>) {
	runApplication<PtsApplication>(*args)
}
