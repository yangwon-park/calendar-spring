package com.calendar.couple.calendar.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "calendar")
@Suppress("ProtectedInFinal")
class CalendarEntity(
	val ownerId: Long,
	name: String,
	type: String,
	color: String,
	description: String? = null,
) : BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	var name: String = name
		protected set
	var type: String = type
		protected set
	var color: String = color
		protected set
	var description: String? = description
		protected set
}