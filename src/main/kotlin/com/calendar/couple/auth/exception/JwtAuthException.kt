package com.calendar.couple.auth.exception

import org.springframework.security.core.AuthenticationException

sealed class JwtAuthException(
	message: String,
) : AuthenticationException(message) {
	class ExpiredTokenException(
		message: String = "토큰이 만료되었습니다",
	) : JwtAuthException(message)

	class InvalidTokenException(
		message: String = "유효하지 않은 토큰입니다",
	) : JwtAuthException(message)

	class MalformedTokenException(
		message: String = "잘못된 형식의 토큰입니다",
	) : JwtAuthException(message)

	class UnsupportedTokenException(
		message: String = "지원하지 않는 토큰입니다",
	) : JwtAuthException(message)

	class SignatureException(
		message: String = "토큰 서명이 유효하지 않습니다",
	) : JwtAuthException(message)
}