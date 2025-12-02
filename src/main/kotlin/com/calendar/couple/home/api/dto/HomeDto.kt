package com.calendar.couple.home.api.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class HomeResponse(
	val eventInfos: List<EventInfo>,
)

data class EventInfo(
	val calendarId: Long,
	val categoryId: Long,
	val eventAt: LocalDateTime,
)

data class HomeCoupleInfo(
	val accountInfo: AccountInfo,
	val coupleInfo: CoupleInfo?,
)

data class AccountInfo(
	val name: String,
)

data class CoupleInfo(
	val partnerId: Long,
	val partnerName: String,
	val startDate: LocalDate,
	val daysCount: Long = ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1L,
)