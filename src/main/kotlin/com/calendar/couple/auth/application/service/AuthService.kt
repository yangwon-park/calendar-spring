package com.calendar.couple.auth.application.service

import com.calendar.couple.account.infrastructure.AccountMapper.toDomain
import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity
import com.calendar.couple.account.infrastructure.persistence.repository.AccountProviderRepository
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.auth.api.dto.SignInResponse
import com.calendar.couple.auth.domain.RefreshToken
import com.calendar.couple.auth.exception.AuthException
import com.calendar.couple.auth.exception.JwtAuthException
import com.calendar.couple.auth.infrastructure.JwtProvider
import com.calendar.couple.auth.infrastructure.oauth2.OAuth2ClientFactory
import com.calendar.couple.auth.infrastructure.oauth2.OAuth2UserInfo
import com.calendar.couple.auth.infrastructure.persistence.repository.TokenRepository
import com.calendar.couple.calendar.domain.Calendar
import com.calendar.couple.calendar.domain.CalendarType
import com.calendar.couple.calendar.infrastructure.CalendarMapper.toEntity
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarRepository
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class AuthService(
	private val oAuth2ClientFactory: OAuth2ClientFactory,
	private val accountRepository: AccountRepository,
	private val accountProviderRepository: AccountProviderRepository,
	private val calendarRepository: CalendarRepository,
	private val tokenRepository: TokenRepository,
	private val jwtProvider: JwtProvider,
) {
	fun signIn(
		code: String,
		provider: String,
	): SignInResponse {
		val provider = provider.uppercase()

		val client = oAuth2ClientFactory.getClient(provider)
		val userInfo = client.getUserInfo(code)

		val accountEntity = getOrCreateAccount(provider, userInfo)
		val accountId = accountEntity.id ?: throw IllegalStateException("Account ID is null")

		return generateJwtTokens(accountId, accountEntity.role)
	}

	fun renewToken(refreshToken: String): SignInResponse {
		jwtProvider.validateRefreshToken(refreshToken)

		val accountId = jwtProvider.getAccountIdFromToken(refreshToken)

		val storedToken =
			tokenRepository.getRefreshTokenByAccountId(accountId)
				?: throw JwtAuthException.ExpiredTokenException("Refresh Token이 만료되었습니다.")

		if (storedToken != refreshToken) {
			tokenRepository.deleteRefreshTokenByAccountId(accountId)
			throw JwtAuthException.InvalidTokenException()
		}

		val role = jwtProvider.getRoleFromToken(refreshToken)

		return generateJwtTokens(accountId, role)
	}

	fun logout(
		accountId: Long,
		accessToken: String,
	) {
		tokenRepository.deleteRefreshTokenByAccountId(accountId)

		jwtProvider.validateAccessToken(accessToken)
		val remainingTtl = jwtProvider.getRemainingTtl(accessToken)

		tokenRepository.saveAccessTokenInBlacklist(accessToken, remainingTtl)
	}

	@Transactional
	private fun getOrCreateAccount(
		provider: String,
		userInfo: OAuth2UserInfo,
	): AccountEntity {
		val existingAccountProvider =
			accountProviderRepository.findByProviderAndProviderUserId(provider, userInfo.id)?.toDomain()

		val accountEntity =
			if (existingAccountProvider == null) {
				val newAccountEntity =
					accountRepository.save(
						AccountEntity.createUserAccount(
							userInfo.email,
							userInfo.name,
							provider,
						),
					)

				accountProviderRepository.save(
					AccountProviderEntity.create(
						newAccountEntity.id ?: throw IllegalStateException("Account ID is null"),
						provider,
						userInfo.id,
					),
				)

				val personalCalendar =
					Calendar(
						ownerId = newAccountEntity.id!!,
						name = "",
						type = CalendarType.PERSONAL,
						color = "#3788D8",
						description = "기본 제공되는 캘린더입니다",
					)
				
				calendarRepository.save(personalCalendar.toEntity())

				newAccountEntity
			} else {
				// TODO -> 최근 접속 기록 내역 업데이트

				accountRepository.findByIdOrNull(existingAccountProvider.accountId)
					?: throw AuthException.AccountNotFoundException()
			}

		return accountEntity
	}

	private fun generateJwtTokens(
		accountId: Long,
		role: String,
	): SignInResponse {
		val accessToken = jwtProvider.generateAccessToken(accountId, role)
		val refreshTokenInfo = jwtProvider.generateRefreshToken(accountId, role)

		tokenRepository.saveRefreshToken(
			RefreshToken(
				accountId,
				refreshTokenInfo.token,
				refreshTokenInfo.expirationTime,
			),
		)

		return SignInResponse(
			accessToken = accessToken,
			refreshToken = refreshTokenInfo.token,
		)
	}
}