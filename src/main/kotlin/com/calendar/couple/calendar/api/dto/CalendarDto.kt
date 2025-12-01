package com.calendar.couple.calendar.api.dto

data class CalendarResponse(
	val calendarId: Long,
	val name: String,
	val type: String,
	val description: String?,
)

data class CalendarUpdateRequest(
	val name: String,
	val type: String,
	val color: String,
	val description: String?,
)