package com.calendar.couple.event.infrastructure

import com.calendar.couple.event.domain.Event
import com.calendar.couple.event.infrastructure.persistence.entity.EventEntity

object EventMapper {
	fun Event.toEntity() = EventEntity(accountId, calendarId, categoryId, title, description, eventAt)
}