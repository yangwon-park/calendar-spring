package com.calendar.couple.couple.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name =  "couple")
@Suppress("ProtectedInFinal")
class CoupleEntity(
	@Column(name = "account1_id",)
	val account1Id: Long,
	@Column(name = "account2_id",)
	val account2Id: Long,
	val startDate: LocalDate,
) : BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null
}