package com.calendar.couple.account.domain

import com.calendar.couple.account.domain.enums.AccountRole
import com.calendar.couple.account.domain.enums.Provider

data class Account(
	val id: Long?,
	val email: String,
	val password: String?,
	val name: String,
	val role: AccountRole,
	val provider: Provider,
	val isDeleted: Boolean,
	val isBanned: Boolean,
	val isWithdraw: Boolean,
)