package com.calendar.couple.calendar.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "calendar_member")
@Suppress("ProtectedInFinal")
class CalendarMemberEntity(
	val calendarId: Long,
	val accountId: Long,
	role: String,
	status: String,
): BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null
	var role: String = role
		protected set
	var status: String = status
		protected set
}