package com.calendar.couple.common.properties.oauth2

import org.springframework.boot.context.properties.ConfigurationProperties

// Jasypt가 없음!!!!!!!!!!!
@ConfigurationProperties(prefix = "google")
data class GoogleOAuth2Properties(
	val clientId: String,
	val clientSecret: String,
	val redirectUri: String,
	val authorizationUri: String = "https://accounts.google.com/o/oauth2/v2/auth",
	val tokenUri: String = "https://oauth2.googleapis.com/token",
	val userInfoUri: String = "https://www.googleapis.com/oauth2/v3/userinfo",
	val scopes: List<String> = listOf("email", "name"),
)