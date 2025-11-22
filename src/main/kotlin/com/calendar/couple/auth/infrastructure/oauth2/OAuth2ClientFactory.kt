package com.calendar.couple.auth.infrastructure.oauth2

import org.springframework.stereotype.Component

/**
 * OAuth2 Provider에 따라 적절한 OAuth2 Client를 반환하는 팩토리 클래스
 */
@Component
class OAuth2ClientFactory(
	private val googleClient: GoogleOAuth2Client,
	private val kakaoClient: KakaoOAuth2Client,
) {
	fun getClient(provider: String) =
		when (provider) {
			"GOOGLE" -> googleClient
			"KAKAO" -> kakaoClient
			else -> throw IllegalArgumentException("Unsupported provider: $provider")
		}
}