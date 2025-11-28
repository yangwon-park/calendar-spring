package com.calendar.couple.calendar.domain

data class CalendarMember(
	val calendarId: Long,
	val accountId: Long,
	val role: CalendarMemberRole,
	val status: CalendarMemberStatus = CalendarMemberStatus.PENDING,
)