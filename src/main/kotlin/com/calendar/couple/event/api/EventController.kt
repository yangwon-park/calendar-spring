package com.calendar.couple.event.api

import com.calendar.couple.common.dto.CommonStatusResponse
import com.calendar.couple.event.api.dto.CreateEventRequest
import com.calendar.couple.event.application.service.EventService
import com.calendar.couple.security.userdetails.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/events")
class EventController(
	private val eventService: EventService,
) {
	@PostMapping
	fun creatEvent(
		@Valid @RequestBody request: CreateEventRequest,
		@AuthenticationPrincipal userDetails: CustomUserDetails,
	): CommonStatusResponse {
		eventService.createEvent(request, userDetails.accountId)
		
		return CommonStatusResponse.success()
	}
}