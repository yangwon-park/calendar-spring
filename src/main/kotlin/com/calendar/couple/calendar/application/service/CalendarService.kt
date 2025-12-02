package com.calendar.couple.calendar.application.service

import com.calendar.couple.calendar.api.dto.CalendarResponse
import com.calendar.couple.calendar.api.dto.CalendarUpdateRequest
import com.calendar.couple.calendar.domain.CalendarType
import com.calendar.couple.calendar.infrastructure.CalendarMapper.toDomain
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = mu.KotlinLogging.logger {}

@Service
class CalendarService(
	private val calendarRepository: CalendarRepository,
) {
	fun getCalendars(accountId: Long): List<CalendarResponse> {
		val calendars =
			calendarRepository.findAllById(accountId).map {
				it.toDomain()
			}

		return calendars.map {
			CalendarResponse(
				calendarId = it.calendarId ?: throw IllegalStateException("Calendar Id is null"),
				name = it.name,
				description = it.description,
				color = it.color,
				type = it.type.name,
			)
		}
	}

	fun getCalendarByCalendarId(calendarId: Long): CalendarResponse {
		val calendar =
			calendarRepository.findByIdOrNull(calendarId)?.toDomain()
				?: throw IllegalStateException("Calendar not found")
		
		return CalendarResponse(
			calendarId = calendar.calendarId ?: throw IllegalStateException("Calendar Id is null"),
			name = calendar.name,
			description = calendar.description,
			color = calendar.color,
			type = calendar.type.name,
		)
	}

	@Transactional
	fun updateCalendar(
		calendarId: Long,
		request: CalendarUpdateRequest,
	) {
		log.info { "Update calendar: $request" }
		
		val calendar =
			calendarRepository.findByIdOrNull(calendarId)?.toDomain()
				?: throw IllegalStateException("Calendar not found")
		
		val newCalendar =
			calendar.update(
				name = request.name,
				type = CalendarType.valueOf(request.type),
				color = request.color,
				description = request.description,
			)
		
		val updatedRows =
			calendarRepository.update(
				newCalendar.name,
				newCalendar.type.name,
				newCalendar.color,
				newCalendar.description,
				calendarId,
			)
		
		if (updatedRows == 0) {
			log.warn { "Calendar not found: $calendarId" }
			throw IllegalStateException("Calendar not found")
		}
	}
}