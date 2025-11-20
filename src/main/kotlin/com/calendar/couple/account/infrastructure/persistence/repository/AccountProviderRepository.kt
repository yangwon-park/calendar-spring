package com.calendar.couple.account.infrastructure.persistence.repository

import com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountProviderRepository : JpaRepository<AccountProviderEntity, Long> {
	fun findByProviderAndProviderUserId(
		provider: String,
		providerUserId: String,
	): AccountProviderEntity?
}