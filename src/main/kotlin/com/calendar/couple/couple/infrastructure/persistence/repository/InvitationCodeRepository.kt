package com.calendar.couple.couple.infrastructure.persistence.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class InvitationCodeRepository(
	private val redisTemplate: RedisTemplate<String, String>,
) {
	fun save(
		code: String,
		inviterId: Long,
	) {
		redisTemplate.opsForValue().set("${INVITATION_PREFIX}$code", inviterId.toString(), INVITATION_TTL)
	}

	fun getInviterAccountIdByCode(code: String): Long? = 
		redisTemplate.opsForValue().get("${INVITATION_PREFIX}$code")?.toLong()

	fun delete(code: String) = redisTemplate.delete("${INVITATION_PREFIX}$code")

	private companion object {
		private const val INVITATION_PREFIX = "couple:invitation:"
		private val INVITATION_TTL = Duration.ofHours(24)
	}
}