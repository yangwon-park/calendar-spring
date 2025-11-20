package com.calendar.couple.auth.infrastructure.oauth2

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Facebook OAuth2 API 클라이언트
 */
@Component
class FacebookOAuth2Client(
	private val restClient: RestClient,
) : OAuth2Client {
	/**
	 * Authorization Code를 사용하여 Access Token 발급
	 *
	 * @param code Authorization Code
	 * @return Access Token
	 */
	override fun getAccessToken(code: String): String {
		// TODO: Facebook OAuth2 Token Endpoint 호출
		// GET https://graph.facebook.com/v18.0/oauth/access_token
		// - code
		// - client_id
		// - client_secret
		// - redirect_uri
		throw NotImplementedError("Facebook OAuth2 getAccessToken 구현 필요")
	}

	/**
	 * Access Token을 사용하여 사용자 정보 조회
	 *
	 * @param accessToken Access Token
	 * @return FacebookUserInfo
	 */
	override fun getUserInfo(accessToken: String): OAuth2UserInfo {
		// TODO: Facebook Graph API 호출
		// GET https://graph.facebook.com/me
		// - access_token={accessToken}
		// - fields=id,name,email,picture
		throw NotImplementedError("Facebook OAuth2 getUserInfo 구현 필요")
	}
}