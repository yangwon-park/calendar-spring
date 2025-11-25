package com.calendar.couple.couple.api

import com.calendar.couple.common.dto.CommonStatusResponse
import com.calendar.couple.couple.api.dto.LinkCoupleRequest
import com.calendar.couple.couple.application.service.CoupleService
import com.calendar.couple.security.userdetails.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/couples")
class CoupleController(
	private val coupleService: CoupleService,
) {
	@PostMapping
	fun linkCouple(
		@Valid @RequestBody request: LinkCoupleRequest,
		@AuthenticationPrincipal userDetails: CustomUserDetails,
	): CommonStatusResponse {
		coupleService.linkCouple(userDetails.accountId, request.invitationCode)

		return CommonStatusResponse.success()
	}
}