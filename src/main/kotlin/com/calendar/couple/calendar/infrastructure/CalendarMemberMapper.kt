package com.calendar.couple.calendar.infrastructure

import com.calendar.couple.calendar.domain.CalendarMember
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarMemberEntity

object CalendarMemberMapper {
	fun CalendarMember.toEntity() = CalendarMemberEntity(calendarId, accountId, role.name, status.name)
}