package com.calendar.couple.home.api

import com.calendar.couple.common.dto.CommonResponse
import com.calendar.couple.home.api.dto.HomeCoupleInfo
import com.calendar.couple.home.api.dto.HomeResponse
import com.calendar.couple.home.application.service.HomeService
import com.calendar.couple.security.userdetails.CustomUserDetails
import mu.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/home")
class HomeController(
	private val homeService: HomeService,
) {
	@GetMapping
	fun getHomeInfo(
		@AuthenticationPrincipal userDetails: CustomUserDetails,
	): CommonResponse<HomeResponse> {
		val result = homeService.getHomeInfo(userDetails.accountId)
		
		log.info { "result: $result" }
		
		return CommonResponse.success(result)
	}

	@GetMapping("/couples")
	fun getCoupleInfo(
		@AuthenticationPrincipal userDetails: CustomUserDetails,
	): CommonResponse<HomeCoupleInfo> = CommonResponse.success(homeService.getCoupleInfo(userDetails.accountId))
}