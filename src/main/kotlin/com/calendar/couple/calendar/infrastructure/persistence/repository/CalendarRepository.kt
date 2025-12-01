package com.calendar.couple.calendar.infrastructure.persistence.repository

import com.calendar.couple.calendar.domain.CalendarType
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CalendarRepository : JpaRepository<CalendarEntity, Long> {
	fun findAllById(id: Long): List<CalendarEntity>

	@Modifying(clearAutomatically = true)
	@Query(
		"UPDATE CalendarEntity c " +
			"SET " +
			"c.name = :name, " +
			"c.type = :type, " +
			"c.color = :color, " +
			"c.description = :description " +
			"WHERE c.id = :id",
	)
	fun update(
		name: String,
		type: String,
		color: String,
		description: String?,
		id: Long,
	): Int
}