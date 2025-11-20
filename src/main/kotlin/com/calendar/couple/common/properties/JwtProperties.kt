package com.calendar.couple.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
	val secret: String,
	val issuer: String,
	val audience: String,
	val accessTokenExpiration: Duration,
	val refreshTokenExpiration: Duration,
)