package com.calendar.couple.event.infrastructure

import com.calendar.couple.event.domain.Event
import com.calendar.couple.event.infrastructure.persistence.entity.EventEntity

object EventMapper {
	fun Event.toEntity() = EventEntity(accountId, calendarId, categoryId, title, description, isAllDay, startAt, endAt)

	fun EventEntity.toDomain() =
		Event(
			id = id,
			accountId = accountId,
			calendarId = calendarId,
			categoryId = categoryId,
			title = title,
			description = description,
			isAllDay = isAllDay,
			startAt = startAt,
			endAt = endAt,
		)
}