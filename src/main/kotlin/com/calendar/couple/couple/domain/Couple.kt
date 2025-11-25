package com.calendar.couple.couple.domain

import java.time.LocalDate

data class Couple(
	val account1Id: Long,
	val account2Id: Long,
	val startDate: LocalDate,
)