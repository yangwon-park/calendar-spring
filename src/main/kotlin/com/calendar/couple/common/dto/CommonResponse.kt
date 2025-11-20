package com.calendar.couple.common.dto

import org.springframework.http.HttpStatus

data class CommonResponse<T>(
	val data: T,
	val status: Int,
) {
	companion object {
		fun <T> success(response: T) = CommonResponse(response, HttpStatus.OK.value())
	}
}

data class CommonStatusResponse(
	val status: Int,
) {
	companion object {
		fun success() = CommonStatusResponse(HttpStatus.OK.value())

		fun create() = CommonStatusResponse(HttpStatus.CREATED.value())
	}
}

data class CommonErrorResponse(
	val message: String?,
	val status: Int, // 개발팀 사전 약속한 Custom Status Code 전달 (ErrorCode)
)