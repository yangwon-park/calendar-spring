package com.calendar.couple.calendar.api

import com.calendar.couple.calendar.application.service.CalendarService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/calendars")
class CalendarController(
	private val calendarService: CalendarService,
)