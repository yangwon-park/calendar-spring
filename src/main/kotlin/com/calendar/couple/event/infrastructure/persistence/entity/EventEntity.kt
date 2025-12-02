package com.calendar.couple.event.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event")
@Suppress("ProtectedInFinal")
class EventEntity(
	val accountId: Long,
	calendarId: Long,
	categoryId: Long,
	title: String,
	description: String? = null,
	eventAt: LocalDateTime,
) : BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	var calendarId: Long = calendarId
		protected set

	var categoryId: Long = categoryId
		protected set

	var title: String = title
		protected set

	var description: String? = description
		protected set

	var eventAt: LocalDateTime = eventAt
		protected set
}