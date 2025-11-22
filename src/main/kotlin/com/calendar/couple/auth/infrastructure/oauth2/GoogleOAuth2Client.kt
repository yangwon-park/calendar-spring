package com.calendar.couple.auth.infrastructure.oauth2

import com.calendar.couple.common.properties.oauth2.GoogleOAuth2Properties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
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
	override fun getAccessToken(code: String): String {
		val response =
			restClient
				.post()
				.uri(googleOAuth2Properties.tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(
					"grant_type=$GRANT_TYPE" +
						"&client_id=${googleOAuth2Properties.clientId}" +
						"&client_secret=${googleOAuth2Properties.clientSecret}" +
						"&redirect_uri=${googleOAuth2Properties.redirectUri}" +
						"&code=$code",
				).retrieve()
				.body(GoogleTokenResponse::class.java) ?: throw IllegalArgumentException("Google 통신 에러 발생")
		
		return response.accessToken
	}

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

	private data class GoogleTokenResponse(
		@field:JsonProperty("access_token")
		val accessToken: String,
		@field:JsonProperty("token_type")
		val tokenType: String,
		@field:JsonProperty("expires_in")
		val expiresIn: Int,
		@field:JsonProperty("refresh_token")
		val refreshToken: String? = null,
		@field:JsonProperty("scope")
		val scope: String? = null,
	)

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

	companion object {
		private const val GRANT_TYPE = "authorization_code"
	}
}