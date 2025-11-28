package com.calendar.couple.event.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event")
@Suppress("ProtectedInFinal")
class EventEntity(
	val accountId: Long,
	calendarId: Long,
	categoryId: Long,
	val title: String,
	val description: String? = null,
	val eventAt: LocalDateTime,
) {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null
}