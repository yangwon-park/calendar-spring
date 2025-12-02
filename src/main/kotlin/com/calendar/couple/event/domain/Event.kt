package com.calendar.couple.event.domain

import java.time.LocalDateTime

data class Event(
	val id: Long? = null,
	val accountId: Long,
	val calendarId: Long,
	val categoryId: Long,
	val title: String,
	val description: String?,
	val isAllDay: Boolean,
	val startAt: LocalDateTime,
	val endAt: LocalDateTime?,
)