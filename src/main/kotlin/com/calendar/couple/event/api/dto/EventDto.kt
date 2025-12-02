package com.calendar.couple.event.api.dto

import java.time.LocalDateTime

data class CreateEventRequest(
	val calendarId: Long,
	val categoryId: Long,
	val title: String,
	val description: String? = null,
	val isAllDay: Boolean,
	val startAt: LocalDateTime,
	val endAt: LocalDateTime? = null,
)

data class CreateEventResponse(
	val id: Long,
	val calendarId: Long,
	val categoryId: Long,
	val title: String,
	val description: String?,
	val isAllDay: Boolean,
	val startAt: LocalDateTime,
	val endAt: LocalDateTime?,
)