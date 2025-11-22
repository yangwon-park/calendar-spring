package com.calendar.couple.auth.infrastructure.oauth2

import com.calendar.couple.common.properties.oauth2.KakaoOAuth2Properties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoOAuth2Client(
	private val restClient: RestClient,
	private val kakaoOAuth2Properties: KakaoOAuth2Properties,
): OAuth2Client {
	override fun getAccessToken(code: String): String {
		TODO("Not yet implemented")
	}

	override fun getUserInfo(accessToken: String): OAuth2UserInfo {
		TODO("Not yet implemented")
	}
}