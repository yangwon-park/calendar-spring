package com.calendar.couple.account.infrastructure.persistence.repository

import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, Long> {
	fun findByEmail(email: String): AccountEntity?
}