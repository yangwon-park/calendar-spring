package com.calendar.couple.account.domain

import com.calendar.couple.account.domain.enums.AdminRole

data class Admin(
	val id: Long?,
	val accountId: Long,
	val role: AdminRole,
)