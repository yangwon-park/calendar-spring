package com.calendar.couple.event.api.dto

import java.time.LocalDateTime

data class CreateEventRequest(
	val calendarId: Long,
	val categoryId: Long,
	val title: String,
	val description: String? = null,
	val eventAt: LocalDateTime,
)

data class CreateEventResponse(
	val id: Long,
)