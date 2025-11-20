package com.calendar.couple.auth.infrastructure.persistence.repository

import com.calendar.couple.auth.domain.RefreshToken
import com.calendar.couple.auth.exception.JwtAuthException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.Instant

@Repository
class TokenRepository(
	private val redisTemplate: RedisTemplate<String, String>,
) {
	fun saveAccessTokenInBlacklist(
		token: String,
		remainingTtl: Duration,
	) {
		redisTemplate.opsForValue().set(getBlacklistKey(token), "logout", remainingTtl)
	}

	fun isAccessTokenInBlacklist(token: String): Boolean = redisTemplate.hasKey(getBlacklistKey(token))

	fun saveRefreshToken(refreshToken: RefreshToken) {
		val ttl = Duration.between(Instant.now(), refreshToken.expirationTime)

		if (ttl.toMillis() <= 0) {
			throw JwtAuthException.ExpiredTokenException("RefreshToken이 이미 만료되었습니다")
		}

		redisTemplate.opsForValue().set(
			getKey(refreshToken.accountId),
			refreshToken.token,
			ttl,
		)
	}

	fun getRefreshTokenByAccountId(accountId: Long): String? = redisTemplate.opsForValue().get(getKey(accountId))

	fun deleteRefreshTokenByAccountId(accountId: Long): Boolean = redisTemplate.delete(getKey(accountId))

	private fun getKey(accountId: Long): String = "${REFRESH_TOKEN_PREFIX}$accountId"

	private fun getBlacklistKey(token: String): String = "${BLACKLIST_PREFIX}$token"

	private companion object {
		private const val REFRESH_TOKEN_PREFIX = "refresh:account:"
		private const val BLACKLIST_PREFIX = "blacklist:"
	}
}