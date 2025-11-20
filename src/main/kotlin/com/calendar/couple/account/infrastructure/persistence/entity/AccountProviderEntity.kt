package com.calendar.couple.account.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "account_provider")
class AccountProviderEntity(
	val accountId: Long,
	val provider: String,
	val providerUserId: String,
) : BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	companion object {
		fun create(
			accountId: Long,
			provider: String,
			providerUserId: String,
		): AccountProviderEntity = AccountProviderEntity(accountId, provider, providerUserId)
	}
}