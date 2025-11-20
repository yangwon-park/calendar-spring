package com.calendar.couple.account.infrastructure.persistence.repository

import com.calendar.couple.account.infrastructure.persistence.entity.AdminEntity
import org.springframework.data.jpa.repository.JpaRepository

// 네이밍  논의 필요
interface AdminRepository : JpaRepository<AdminEntity, Long>