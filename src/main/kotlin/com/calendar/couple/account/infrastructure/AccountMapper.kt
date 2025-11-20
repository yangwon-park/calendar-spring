package com.calendar.couple.account.infrastructure

import com.calendar.couple.account.domain.Account
import com.calendar.couple.account.domain.AccountProvider
import com.calendar.couple.account.domain.Admin
import com.calendar.couple.account.domain.enums.AccountRole
import com.calendar.couple.account.domain.enums.Provider
import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity
import com.calendar.couple.account.infrastructure.persistence.entity.AdminEntity

object AccountMapper {
	fun AccountEntity.toDomain() =
		Account(
			id,
			email,
			password,
			name,
			AccountRole.valueOf(role),
			Provider.valueOf(provider),
			isDeleted,
			isBanned,
			isWithdraw,
		)

	fun AccountProviderEntity.toDomain() =
		AccountProvider(
			accountId,
			Provider.valueOf(provider),
			providerUserId,
		)

	fun Account.toEntity() =
		AccountEntity(
			email,
			password,
			name,
			role.name,
			provider.name,
		)

	fun Admin.toEntity() =
		AdminEntity(
			accountId,
			role.name,
		)

	fun AccountProvider.toEntity() =
		AccountProviderEntity(
			accountId,
			provider.name,
			providerUserId,
		)
}