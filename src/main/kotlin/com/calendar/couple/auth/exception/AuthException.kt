package com.calendar.couple.auth.exception

import org.springframework.security.core.AuthenticationException

/**
 * 인증 관련 예외
 */
sealed class AuthException(
	message: String,
) : AuthenticationException(message) {
	class AccountNotFoundException(
		message: String = "계정을 찾을 수 없습니다",
	) : AuthException(message)

	// 기획 보강 후 사용
	class AccountDisabledException(
		message: String = "사용할 수 없는 계정입니다",
	) : AuthException(message)

	// 기획 보강 후 사용
	class AccountLockedException(
		message: String = "정지된 계정입니다",
	) : AuthException(message)
}