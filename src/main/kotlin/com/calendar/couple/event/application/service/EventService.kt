package com.calendar.couple.event.application.service

import com.calendar.couple.event.api.dto.CreateEventRequest
import com.calendar.couple.event.domain.Event
import com.calendar.couple.event.infrastructure.EventMapper.toEntity
import com.calendar.couple.event.infrastructure.persistence.repository.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventService(
	private val eventRepository: EventRepository,
) {
	@Transactional
	fun createEvent(
		request: CreateEventRequest,
		accountId: Long,
	) {
		val event =
			Event(
				accountId = accountId,
				calendarId = request.calendarId,
				categoryId = request.categoryId,
				title = request.title,
				eventAt = request.eventAt,
				description = request.description,
			)
		
		eventRepository.save(event.toEntity())
	}
}