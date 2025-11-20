package com.calendar.couple.security.jwt

import com.calendar.couple.auth.exception.JwtAuthException
import com.calendar.couple.common.dto.CommonErrorResponse
import com.calendar.couple.common.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
	private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
	private val log = KotlinLogging.logger {}

	override fun commence(
		request: HttpServletRequest,
		response: HttpServletResponse,
		authException: AuthenticationException,
	) {
		val errorCode =
			when (authException) {
				is JwtAuthException.ExpiredTokenException -> ErrorCode.EXPIRED_TOKEN
				is JwtAuthException -> ErrorCode.INVALID_TOKEN
				is LockedException -> ErrorCode.BANNED_ACCOUNT
				is DisabledException -> ErrorCode.WITHDRAWN_ACCOUNT
				else -> ErrorCode.UNAUTHORIZED
			}

		val message = authException.message ?: AUTHENTICATION_EXCEPTION_MESSAGE

		log.warn { "인증 실패: ${errorCode.status} - $message" }

		val errorResponse = CommonErrorResponse(message, errorCode.status)

		response.status = HttpServletResponse.SC_UNAUTHORIZED
		response.contentType = CONTENT_TYPE
		response.writer.write(objectMapper.writeValueAsString(errorResponse))
	}

	private companion object {
		private const val AUTHENTICATION_EXCEPTION_MESSAGE = "인증에 실패하였습니다."
		private const val CONTENT_TYPE = "application/json;charset=UTF-8"
	}
}