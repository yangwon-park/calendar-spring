package com.calendar.couple.auth.api.dto

class AuthResponse

data class SignInResponse(
	val accessToken: String,
	val refreshToken: String,
)