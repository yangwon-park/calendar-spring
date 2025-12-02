package com.calendar.couple.event.infrastructure.persistence.repository

import com.calendar.couple.event.infrastructure.persistence.entity.EventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<EventEntity, Long> {
	fun findAllByAccountId(accountId: Long): List<EventEntity>
}