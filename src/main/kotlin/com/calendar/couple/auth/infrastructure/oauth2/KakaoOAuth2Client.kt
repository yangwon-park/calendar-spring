package com.calendar.couple.auth.infrastructure.oauth2

import com.calendar.couple.common.properties.oauth2.KakaoOAuth2Properties
import com.fasterxml.jackson.annotation.JsonProperty
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

private val log = KotlinLogging.logger {}

@Component
class KakaoOAuth2Client(
	private val restClient: RestClient,
	private val kakaoOAuth2Properties: KakaoOAuth2Properties,
) : OAuth2Client {
	override fun getUserInfo(accessToken: String): OAuth2UserInfo {
		log.info { "Kakao OAuth2 Access Token: $accessToken" }
		
		val response =
			restClient
				.get()
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