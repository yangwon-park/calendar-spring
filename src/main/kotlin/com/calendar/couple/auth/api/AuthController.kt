package com.calendar.couple.auth.api

import com.calendar.couple.auth.api.dto.SignInRequest
import com.calendar.couple.auth.api.dto.SignInResponse
import com.calendar.couple.common.dto.CommonResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController {
	@PostMapping("/sign-in")
	fun signIn(
		@Valid @RequestBody request: SignInRequest,
	): CommonResponse<SignInResponse> =
		CommonResponse.success(
			SignInResponse("", ""),
		)
}