package com.calendar.couple.common

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
	@CreatedDate
	@Column(columnDefinition = "DATETIME(3)")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	lateinit var createdAt: LocalDateTime
		protected set

	@LastModifiedDate
	@Column(columnDefinition = "DATETIME(3)")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	lateinit var updatedAt: LocalDateTime
		protected set
}