package com.calendar.couple.auth.api.dto

import jakarta.validation.constraints.NotBlank

data class SignInRequest(
	@field:NotBlank(message = "Social Token은 필수입니다.")
	val code: String,
	@field:NotBlank(message = "Social Provider는 필수입니다.")
	val provider: String,
)

data class RefreshTokenRequest(
	@field:NotBlank(message = "Refresh Token은 필수입니다.")
	val refreshToken: String,
)