package com.calendar.couple.couple.infrastructure

import com.calendar.couple.couple.domain.Couple
import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity

object CoupleMapper {
	fun Couple.toEntity() = CoupleEntity(account1Id, account2Id, startDate)
}