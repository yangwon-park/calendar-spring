package com.calendar.couple.couple.application.service

import com.calendar.couple.couple.api.dto.CoupleInvitationDto
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import org.springframework.stereotype.Service

@Service
class CoupleInvitationService(
	private val invitationCodeRepository: InvitationCodeRepository,
) {
	fun createInvitationCode(inviterId: Long): CoupleInvitationDto {
		val code = generateUniqueInvitationCode()
		invitationCodeRepository.save(code, inviterId)

		return CoupleInvitationDto(code)
	}

	fun getInviterId(code: String): Long? = invitationCodeRepository.getInviterAccountIdByCode(code)

	fun useInvitation(code: String) {
		invitationCodeRepository.delete(code)
	}

	private fun generateUniqueInvitationCode(): String {
		repeat(5) {
			val code = generateInvitationCode()
			if (invitationCodeRepository.getInviterAccountIdByCode(code) == null) {
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