package com.calendar.couple.common.properties.oauth2

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakao")
data class KakaoOAuth2Properties(
	val clientId: String,
	val redirectUri: String,
	val tokenUri: String = "https://kauth.kakao.com/oauth/token",
	val apiUri: String = "https://kapi.kakao.com/oauth/token",
)