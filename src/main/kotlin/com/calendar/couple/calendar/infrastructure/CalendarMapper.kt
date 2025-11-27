package com.calendar.couple.calendar.infrastructure

import com.calendar.couple.calendar.domain.Calendar
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarEntity

object CalendarMapper {
	fun Calendar.toEntity() = CalendarEntity(ownerId, name, type.name, color, "기본으로 제공되는 캘린더입니다.")
}