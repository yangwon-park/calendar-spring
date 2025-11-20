package com.calendar.couple.security.userdetails.dto

data class UserDetailsDto(
	val id: Long,
	val email: String,
	val password: String?,
	val role: String,
	val isBanned: Boolean,
	val isDeleted: Boolean,
	val isWithdraw: Boolean,
)