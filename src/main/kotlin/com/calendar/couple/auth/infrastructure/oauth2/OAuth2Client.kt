package com.calendar.couple.auth.infrastructure.oauth2

interface OAuth2Client {
	fun getUserInfo(accessToken: String): OAuth2UserInfo
}

data class OAuth2UserInfo(
	val id: String,
	val email: String,
	val name: String,
)