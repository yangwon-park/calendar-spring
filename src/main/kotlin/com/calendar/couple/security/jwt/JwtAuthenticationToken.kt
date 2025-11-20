package com.calendar.couple.security.jwt

import com.calendar.couple.security.userdetails.CustomUserDetails
import org.springframework.security.authentication.AbstractAuthenticationToken

class JwtAuthenticationToken(
	private val userDetails: CustomUserDetails,
) : AbstractAuthenticationToken(userDetails.authorities) {
	init {
		isAuthenticated = true
	}

	override fun getCredentials(): Any? = null // JWT 인증이므로 자격 증명 불필요

	override fun getPrincipal(): Any = userDetails
}