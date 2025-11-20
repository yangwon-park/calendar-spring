package com.calendar.couple.common.exception

enum class ErrorCode(
	val status: Int,
	val message: String,
) {
	/**
	 * 회원 10xx
	 */
	ACCOUNT_NOT_FOUND(1001, "존재하지 않는 계정입니다"),

	/**
	 * 인증/인가 40xx
	 */
	INVALID_CREDENTIALS(4001, "이메일 또는 비밀번호가 올바르지 않습니다"),
	EXPIRED_TOKEN(4002, "토큰이 만료되었습니다"),
	INVALID_TOKEN(4003, "유효하지 않은 토큰입니다"),
	BANNED_ACCOUNT(4004, "정지된 계정입니다"),
	WITHDRAWN_ACCOUNT(4005, "탈퇴한 계정입니다"),
	UNAUTHORIZED(4010, "인증이 실패하였습니다"),
}