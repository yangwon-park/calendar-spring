package com.calendar.couple.couple.application.service

import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.couple.api.dto.LinkCoupleResponse
import com.calendar.couple.couple.domain.Couple
import com.calendar.couple.couple.exception.CoupleException
import com.calendar.couple.couple.infrastructure.CoupleMapper.toEntity
import com.calendar.couple.couple.infrastructure.persistence.repository.CoupleRepository
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CoupleService(
	private val accountRepository: AccountRepository,
	private val coupleRepository: CoupleRepository,
	private val invitationCodeRepository: InvitationCodeRepository,
) {
	@Transactional
	fun linkCouple(
		accountId: Long,
		invitationCode: String,
	): LinkCoupleResponse {
		val inviterAccountId = invitationCodeRepository.getInviterAccountIdByCode(invitationCode)
			?: throw CoupleException.InvalidInvitationCodeException()
		
		val inviterName = validateCoupleInvitation(accountId, inviterAccountId)

		val couple = Couple(
			account1Id = inviterAccountId,
			account2Id = accountId,
			starteDate = LocalDate.now()
		)
		
		val savedCoupleEntity = coupleRepository.save(couple.toEntity())
		
		invitationCodeRepository.delete(invitationCode)
		
		return LinkCoupleResponse(
			coupleId = savedCoupleEntity.id!!,
			partnerId = inviterAccountId,
			partnerName = inviterName,
			startDate = savedCoupleEntity.startDate,
			linkedAt = savedCoupleEntity.createdAt
		)
	}

	private fun validateCoupleInvitation(
		accountId: Long,
		inviterAccountId: Long,
	): String {
		if (accountId == inviterAccountId) throw CoupleException.SelfInvitationException()

		// 3. 이미 커플인지 확인 (초대한 사람)
		if (coupleRepository.existsById(inviterAccountId)) {
			throw CoupleException.AlreadyCoupledInviterException()
		}

		// 4. 이미 커플인지 확인 (초대받은 사람)
		if (coupleRepository.existsById(accountId)) {
			throw CoupleException.AlreadyCoupledInviteeException()
		}

		// 5. 두 계정이 모두 존재하는지 확인
		val inviterName = accountRepository.findByIdOrNull(inviterAccountId)?.name
			?: throw IllegalStateException("No Account")

		val isExistingAccount = accountRepository.existsById(accountId)
		
		if (!isExistingAccount) throw IllegalStateException("No Account")
		
		return inviterName
	}
}