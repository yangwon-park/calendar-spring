package com.calendar.couple.couple.application.service

import com.calendar.couple.couple.api.dto.CoupleInvitationResponse
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import org.springframework.stereotype.Service

@Service
class CoupleInvitationService(
	private val invitationCodeRepository: InvitationCodeRepository,
) {
	fun createInvitationCode(inviterId: Long): CoupleInvitationResponse {
		val code = generateUniqueInvitationCode()
		invitationCodeRepository.save(code, inviterId)

		return CoupleInvitationResponse(code)
	}

	fun getInviterId(code: String): Long? = invitationCodeRepository.getInviterIdByCode(code)

	fun useInvitation(code: String) {
		invitationCodeRepository.delete(code)
	}

	private fun generateUniqueInvitationCode(): String {
		repeat(5) {
			val code = generateInvitationCode()
			if (invitationCodeRepository.getInviterIdByCode(code) == null) {
				return code
			}
		}
		
		throw IllegalStateException("초대 코드 생성 실패")
	}

	private fun generateInvitationCode(): String {
		val chars = ('A'..'Z') + ('0'..'9')

		return (1..6).map { chars.random() }.joinToString("")
	}
}