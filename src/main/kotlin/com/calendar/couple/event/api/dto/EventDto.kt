package com.calendar.couple.event.api.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CreateEventRequest(
	val calendarId: Long,
	val categoryId: Long,
	@field:NotBlank(message = "제목은 필수입니다.")
	val title: String,
	val description: String? = null,
	val isAllDay: Boolean = false,
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