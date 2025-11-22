package com.calendar.couple.auth.infrastructure.oauth2

import com.calendar.couple.common.properties.oauth2.KakaoOAuth2Properties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoOAuth2Client(
	private val restClient: RestClient,
	private val kakaoOAuth2Properties: KakaoOAuth2Properties,
): OAuth2Client {
	override fun getAccessToken(code: String): String {
		val response =
			restClient
				.post()
				.uri(kakaoOAuth2Properties.tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(
					"grant_type=authorization_code" +
						"&client_id=${kakaoOAuth2Properties.clientId}" +
						"&redirect_uri=${kakaoOAuth2Properties.redirectUri}" +
						"&code=$code",
				)
				.retrieve()
				.body(KakaoTokenResponse::class.java) ?: throw IllegalArgumentException("Kakao 통신 에러 발생")

		return response.accessToken
	}

	override fun getUserInfo(accessToken: String): OAuth2UserInfo {
		val response =
			restClient.get()
				.uri(kakaoOAuth2Properties.apiUri)
				.header("Authorization", "Bearer $accessToken")
				.retrieve()
				.body(KakaoUserResponse::class.java) ?: throw IllegalArgumentException("Kakao 통신 에러 발생")

		return OAuth2UserInfo(
			response.id,
			"test@test.com",
			response.kakaoAccount.profile.nickname,
		)
	}

	private data class KakaoTokenResponse(
		@field:JsonProperty("access_token")
		val accessToken: String,

		@field:JsonProperty("token_type")
		val tokenType: String,

		@field:JsonProperty("refresh_token")
		val refreshToken: String,

		@field:JsonProperty("expires_in")
		val expiresIn: Int,

		@field:JsonProperty("scope")
		val scope: String? = null,

		@field:JsonProperty("refresh_token_expires_in")
		val refreshTokenExpiresIn: Int? = null,
	)

	private data class KakaoUserResponse(
		val id: String,
		@field:JsonProperty(value = "kakao_account")
		val kakaoAccount: KakaoAccount,
	) 
	
	// 추후 프로퍼티 추가
	private data class KakaoAccount(
		val profile: Profile,
	)

	private data class Profile(
		val nickname: String,
	)
}