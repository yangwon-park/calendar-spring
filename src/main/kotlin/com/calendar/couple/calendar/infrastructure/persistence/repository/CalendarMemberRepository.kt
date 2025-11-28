package com.calendar.couple.calendar.infrastructure.persistence.repository

import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarMemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CalendarMemberRepository : JpaRepository<CalendarMemberEntity, Long>