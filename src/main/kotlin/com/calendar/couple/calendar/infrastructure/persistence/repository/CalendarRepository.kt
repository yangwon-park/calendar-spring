package com.calendar.couple.calendar.infrastructure.persistence.repository

import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CalendarRepository : JpaRepository<CalendarEntity, Long>