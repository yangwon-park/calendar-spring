package com.calendar.couple.couple.infrastructure.persistence.repository

import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CoupleRepository : JpaRepository<CoupleEntity, Long> {
	fun existsByAccount1IdOrAccount2Id(
		account1Id: Long,
		account2Id: Long,
	): Boolean

	fun findByAccount1IdOrAccount2Id(
		account1Id: Long,
		account2Id: Long,
	): CoupleEntity?
}