package com.calendar.couple.auth.infrastructure

import com.calendar.couple.auth.exception.JwtAuthException
import com.calendar.couple.common.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import kotlin.jvm.java
import kotlin.text.toLong

private val log = KotlinLogging.logger {}

@Component
class JwtProvider(
	private val jwtProperties: JwtProperties,
	private val clock: Clock,
) {
	private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

	fun generateAccessToken(
		accountId: Long,
		role: String,
	): String =
		Jwts
			.builder()
			.issuer(jwtProperties.issuer)
			.subject(accountId.toString())
			.audience()
			.add(jwtProperties.audience)
			.and()
			.claim(CLAIM_ROLE, role)
			.issuedAt(Date.from(Instant.now(clock)))
			.expiration(Date.from(Instant.now(clock).plus(jwtProperties.accessTokenExpiration)))
			.signWith(secretKey)
			.compact()

	fun generateRefreshToken(
		accountId: Long,
		roleType: String,
	): RefreshTokenInfo {
		val expirationInstant = Instant.now(clock).plus(jwtProperties.refreshTokenExpiration)

		val token =
			Jwts
				.builder()
				.issuer(jwtProperties.issuer)
				.subject(accountId.toString())
				.claim(CLAIM_ROLE, roleType)
				.id(UUID.randomUUID().toString())
				.issuedAt(Date.from(Instant.now(clock)))
				.expiration(Date.from(expirationInstant))
				.signWith(secretKey)
				.compact()

		return RefreshTokenInfo(
			token,
			expirationInstant,
		)
	}

	fun validateAccessToken(token: String) {
		parseToken(token, requireAudience = true)
	}

	fun validateRefreshToken(token: String) {
		parseToken(token, requireAudience = false)
	}

	fun getAccountIdFromToken(token: String): Long = parseClaims(token).subject.toLong()

	fun getRoleFromToken(token: String): String = parseClaims(token)[CLAIM_ROLE, String::class.java]

	fun getRemainingTtl(token: String): Duration {
		try {
			val claims = parseClaims(token)
			val expiration = claims.expiration.toInstant()
			val now = Instant.now(clock)

			val ttl = Duration.between(now, expiration)

			if (ttl.isNegative || ttl.isZero) {
				throw JwtAuthException.ExpiredTokenException()
			}

			return ttl
		} catch (e: ExpiredJwtException) {
			log.warn { "Expired JWT token in getRemainingTtl: ${e.message}" }
			throw JwtAuthException.ExpiredTokenException()
		}
	}

	private fun parseToken(
		token: String,
		requireAudience: Boolean = true,
	) {
		try {
			val parser =
				Jwts
					.parser()
					.verifyWith(secretKey)
					.clock { Date.from(Instant.now(clock)) }
					.requireIssuer(jwtProperties.issuer)

			if (requireAudience) {
				parser.requireAudience(jwtProperties.audience)
			}

			parser.build().parseSignedClaims(token).payload
		} catch (e: ExpiredJwtException) {
			log.warn { "Expired JWT token: ${e.message}" }
			throw JwtAuthException.ExpiredTokenException()
		} catch (e: SignatureException) {
			log.warn { "Invalid JWT signature: ${e.message}" }
			throw JwtAuthException.SignatureException()
		} catch (e: MalformedJwtException) {
			log.warn { "Malformed JWT token: ${e.message}" }
			throw JwtAuthException.MalformedTokenException()
		} catch (e: UnsupportedJwtException) {
			log.warn { "Unsupported JWT token: ${e.message}" }
			throw JwtAuthException.UnsupportedTokenException()
		} catch (e: IllegalArgumentException) {
			log.warn { "JWT claims string is empty: ${e.message}" }
			throw JwtAuthException.InvalidTokenException()
		}
	}

	/**
	 * ⚠️ WARNING: 해당 메소드는 기본적인 서명 검증만 수행
	 * issuer와 audience는 검증 X
	 *
	 * AccessToken 검증 시, 반드시 validateAccessToken() 선 호출 후 사용
	 */
	private fun parseClaims(token: String): Claims =
		Jwts
			.parser()
			.verifyWith(secretKey)
			.clock { Date.from(Instant.now(clock)) }
			.build()
			.parseSignedClaims(token)
			.payload

	private companion object {
		private const val CLAIM_ROLE = "role"
	}
}

data class RefreshTokenInfo(
	val token: String,
	val expirationTime: Instant,
)