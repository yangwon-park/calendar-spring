package com.calendar.couple.couple.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

data class LinkCoupleRequest(
	@field:NotBlank(message = "초대 코드는 필수입니다")
	@field:Size(min = 6, max = 6, message = "초대 코드는 6자리입니다")
	val invitationCode: String,
)

data class LinkCoupleResponse(
	val coupleId: Long,
	val partnerId: Long,
	val partnerName: String,
	val startDate: LocalDate,
	val linkedAt: LocalDateTime,
)

data class CoupleAdditionalRequest(
	val startDate: LocalDate,
)