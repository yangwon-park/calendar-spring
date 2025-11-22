package com.calendar.couple.auth.infrastructure.oauth2

import com.calendar.couple.common.properties.oauth2.GoogleOAuth2Properties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Google OAuth2 API 클라이언트
 */
@Component
class GoogleOAuth2Client(
	private val restClient: RestClient,
	private val googleOAuth2Properties: GoogleOAuth2Properties,
) : OAuth2Client {
	override fun getUserInfo(accessToken: String): OAuth2UserInfo {
		val response =
			restClient
				.get()
				.uri(googleOAuth2Properties.userInfoUri)
				.header("Authorization", "Bearer $accessToken")
				.retrieve()
				.body(GoogleUserResponse::class.java) ?: throw IllegalArgumentException("Google 통신 에러 발생")

		return OAuth2UserInfo(
			response.id,
			response.email,
			response.name,
		)
	}

	private data class GoogleUserResponse(
		@field:JsonProperty("sub")
		val id: String,
		@field:JsonProperty("email")
		val email: String,
		@field:JsonProperty("name")
		val name: String,
		@field:JsonProperty("email_verified")
		val emailVerified: Boolean = true,
	)
}