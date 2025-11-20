package com.calendar.couple

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CoupleApplication

fun main(args: Array<String>) {
	runApplication<CoupleApplication>(*args)
}