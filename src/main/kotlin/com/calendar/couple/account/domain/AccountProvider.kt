package com.calendar.couple.account.domain

import com.calendar.couple.account.domain.enums.Provider

data class AccountProvider(
	val accountId: Long,
	val provider: Provider,
	val providerUserId: String,
)