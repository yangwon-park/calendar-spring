package com.calendar.couple.calendar.domain

data class Calendar(
	val ownerId: Long,
	val name: String,
	val type: CalendarType,
	val color: String,
)