package com.calendar.couple.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
	val allowedOrigins: List<String>,
	val allowedHeaders: List<String> = listOf("*"),
	val allowedMethods: List<String> = listOf("*"),
	val allowCredentials: Boolean = true,
)