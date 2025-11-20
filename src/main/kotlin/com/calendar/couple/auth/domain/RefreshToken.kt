package com.calendar.couple.auth.domain

import java.time.Instant

data class RefreshToken(
	val accountId: Long,
	val token: String,
	val expirationTime: Instant,
)