package com.calendar.couple.couple.infrastructure.persistence.repository

import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface CoupleRepository : JpaRepository<CoupleEntity, Long> {
	fun existsByAccount1IdOrAccount2Id(
		account1Id: Long,
		account2Id: Long,
	): Boolean

	fun findByAccount1IdOrAccount2Id(
		account1Id: Long,
		account2Id: Long,
	): CoupleEntity?

	fun deleteByAccount1IdOrAccount2Id(
		account1Id: Long,
		account2Id: Long,
	)

	@Modifying(clearAutomatically = true)
	@Query("UPDATE CoupleEntity c SET c.startDate = :startDate WHERE c.id = :id")
	fun updateStartDate(
		id: Long,
		startDate: LocalDate,
	)
}