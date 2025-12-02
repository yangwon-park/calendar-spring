package com.calendar.couple.calendar.api

import com.calendar.couple.calendar.api.dto.CalendarResponse
import com.calendar.couple.calendar.api.dto.CalendarUpdateRequest
import com.calendar.couple.calendar.application.service.CalendarService
import com.calendar.couple.common.dto.CommonResponse
import com.calendar.couple.common.dto.CommonResponse.Companion.success
import com.calendar.couple.common.dto.CommonStatusResponse
import com.calendar.couple.common.dto.CommonStatusResponse.Companion.success
import com.calendar.couple.security.userdetails.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = mu.KotlinLogging.logger {}

@RestController
@RequestMapping("/api/calendars")
class CalendarController(
	private val calendarService: CalendarService,
) {
	@GetMapping
	fun getCalendars(
		@AuthenticationPrincipal userDetails: CustomUserDetails,
	): CommonResponse<List<CalendarResponse>> =
		success(
			calendarService.getCalendars(userDetails.accountId),
		)

	@GetMapping("/{calendarId}")
	fun getCalendarByCalendarId(
		@PathVariable calendarId: Long,
	): CommonResponse<CalendarResponse> = success(calendarService.getCalendarByCalendarId(calendarId))

	@PutMapping("/{calendarId}")
	fun updateCalendar(
		@PathVariable calendarId: Long,
		@RequestBody request: CalendarUpdateRequest,
	): CommonStatusResponse {
		calendarService.updateCalendar(
			calendarId,
			request,
		)
		
		return success()
	}
}