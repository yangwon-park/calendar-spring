package com.calendar.couple.common.properties.oauth2

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakao")
data class KakaoOAuth2Properties(
	val clientId: String,
	val tokenUri: String = "https://kauth.kakao.com/oauth/token",
	val apiUri: String = "https://kapi.kakao.com/v2/user/me",
)