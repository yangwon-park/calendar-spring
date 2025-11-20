package com.calendar.couple.security.jwt

import com.calendar.couple.auth.exception.JwtAuthException
import com.calendar.couple.auth.infrastructure.JwtProvider
import com.calendar.couple.auth.infrastructure.persistence.repository.TokenRepository
import com.calendar.couple.security.userdetails.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.AccountStatusException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.text.startsWith
import kotlin.text.substring

@Component
class JwtAuthenticationFilter(
	private val jwtProvider: JwtProvider,
	private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
	private val customUserDetailsService: CustomUserDetailsService,
	private val tokenRepository: TokenRepository,
) : OncePerRequestFilter() {
	private val log = KotlinLogging.logger {}

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		try {
			val token = extractToken(request)

			if (token != null) {
				jwtProvider.validateAccessToken(token)

				// 블랙리스트 체크
				if (isTokenInBlacklist(token)) {
					throw JwtAuthException.InvalidTokenException("로그아웃된 토큰입니다")
				}

				val accountId = jwtProvider.getAccountIdFromToken(token)
				val userDetails = customUserDetailsService.loadUserByAccountId(accountId)
				
				validateAccountStatus(userDetails)

				val context = SecurityContextHolder.createEmptyContext()
				val authentication = JwtAuthenticationToken(userDetails)
				context.authentication = authentication
				SecurityContextHolder.setContext(context)
			}
		} catch (e: JwtAuthException) {
			log.warn(e) { "JWT 인증 실패" }
			SecurityContextHolder.clearContext()
			jwtAuthenticationEntryPoint.commence(request, response, e)
			return
		} catch (e: AccountStatusException) {
			log.warn(e) { "계정 상태 검증 실패" }
			SecurityContextHolder.clearContext()
			jwtAuthenticationEntryPoint.commence(request, response, e)

			return
		}

		filterChain.doFilter(request, response)
	}

	private fun isTokenInBlacklist(token: String): Boolean = tokenRepository.isAccessTokenInBlacklist(token)

	private fun extractToken(request: HttpServletRequest): String? {
		val header = request.getHeader(AUTHORIZATION_HEADER)

		return if (header != null && header.startsWith(BEARER_PREFIX)) {
			header.substring(7)
		} else {
			null
		}
	}

	private fun validateAccountStatus(userDetails: UserDetails) {
		when {
			!userDetails.isAccountNonLocked ->
				throw LockedException("정지된 계정입니다")

			!userDetails.isAccountNonExpired ->
				throw AccountExpiredException("계정이 만료되었습니다")

			!userDetails.isCredentialsNonExpired ->
				throw CredentialsExpiredException("인증 정보가 만료되었습니다")

			!userDetails.isEnabled ->
				throw DisabledException("탈퇴한 계정입니다")
		}
	}

	private companion object {
		const val AUTHORIZATION_HEADER = "Authorization"
		const val BEARER_PREFIX = "Bearer "
	}
}