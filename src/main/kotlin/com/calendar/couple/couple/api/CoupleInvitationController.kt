package com.calendar.couple.couple.api

import com.calendar.couple.common.dto.CommonResponse
import com.calendar.couple.common.dto.CommonResponse.Companion.success
import com.calendar.couple.couple.api.dto.CoupleInvitationResponse
import com.calendar.couple.couple.application.service.CoupleInvitationService
import com.calendar.couple.security.userdetails.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/couple/invitations")
class CoupleInvitationController(
	private val coupleInvitationService: CoupleInvitationService,
) {
	@PostMapping
	fun createInvitationCode(
		@AuthenticationPrincipal userDetails: CustomUserDetails,
	): CommonResponse<CoupleInvitationResponse> =
		success(coupleInvitationService.createInvitationCode(userDetails.accountId))
}