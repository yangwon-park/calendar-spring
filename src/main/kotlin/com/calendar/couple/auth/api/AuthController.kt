package com.calendar.couple.auth.api

import com.calendar.couple.auth.api.dto.RefreshTokenRequest
import com.calendar.couple.auth.api.dto.SignInRequest
import com.calendar.couple.auth.api.dto.SignInResponse
import com.calendar.couple.auth.application.service.AuthService
import com.calendar.couple.common.dto.CommonResponse
import com.calendar.couple.common.dto.CommonResponse.Companion.success
import com.calendar.couple.common.dto.CommonStatusResponse
import com.calendar.couple.security.userdetails.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val authService: AuthService,
) {
	@PostMapping("/sign-in")
	fun signIn(
		@Valid @RequestBody request: SignInRequest,
	): CommonResponse<SignInResponse> =
		success(
			authService.signIn(
				request.code,
				request.provider,
			),
		)

	@PostMapping("/refresh")
	fun refreshToken(
		@Valid @RequestBody request: RefreshTokenRequest,
	): CommonResponse<SignInResponse> =
		success(
			authService.renewToken(
				request.refreshToken,
			),
		)

	@DeleteMapping("/logout")
	fun logout(
		@AuthenticationPrincipal userDetails: CustomUserDetails,
		@RequestHeader("Authorization") authorization: String,
	): CommonStatusResponse {
		val accessToken = extractToken(authorization)
		authService.logout(userDetails.accountId, accessToken)

		return CommonStatusResponse.success()
	}

	private fun extractToken(authorization: String): String {
		require(authorization.startsWith("Bearer ")) { "Invalid Authorization header format" }
		return authorization.substring(7)
	}
}