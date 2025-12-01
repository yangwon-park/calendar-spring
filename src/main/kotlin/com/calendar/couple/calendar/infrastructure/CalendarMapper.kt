package com.calendar.couple.calendar.infrastructure

import com.calendar.couple.calendar.domain.Calendar
import com.calendar.couple.calendar.domain.CalendarType
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarEntity

object CalendarMapper {
	fun Calendar.toEntity() = CalendarEntity(ownerId, name, type.name, color, description)

	fun CalendarEntity.toDomain() =
		Calendar(
			calendarId = id,
			ownerId = ownerId,
			name = name,
			type = CalendarType.valueOf(type),
			color = color,
			description = description,
		)
}