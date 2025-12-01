package com.calendar.couple.calendar.domain

data class Calendar(
	val calendarId: Long? = null,
	val ownerId: Long,
	val name: String,
	val type: CalendarType,
	val color: String,
	val description: String? = null,
) {
	fun update(
		name: String,
		type: CalendarType,
		color: String,
		description: String? = null,
	): Calendar =
		copy(
			name = name,
			type = type,
			color = color,
			description = description,
		)
}