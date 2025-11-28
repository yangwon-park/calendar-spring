package com.calendar.couple.event.domain

import java.time.LocalDateTime

data class Event(
	val accountId: Long,
	val calendarId: Long,
	val categoryId: Long,
	val title: String,
	val description: String?,
	val eventAt: LocalDateTime,
)