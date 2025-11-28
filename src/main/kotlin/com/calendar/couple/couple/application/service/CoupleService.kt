package com.calendar.couple.couple.application.service

import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.calendar.domain.Calendar
import com.calendar.couple.calendar.domain.CalendarMember
import com.calendar.couple.calendar.domain.CalendarMemberRole
import com.calendar.couple.calendar.domain.CalendarMemberStatus
import com.calendar.couple.calendar.domain.CalendarType
import com.calendar.couple.calendar.infrastructure.CalendarMapper.toEntity
import com.calendar.couple.calendar.infrastructure.CalendarMemberMapper.toEntity
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarMemberRepository
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarRepository
import com.calendar.couple.couple.api.dto.LinkCoupleResponse
import com.calendar.couple.couple.domain.Couple
import com.calendar.couple.couple.exception.CoupleException
import com.calendar.couple.couple.infrastructure.CoupleMapper.toEntity
import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity
import com.calendar.couple.couple.infrastructure.persistence.repository.CoupleRepository
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import com.calendar.couple.couple.infrastructure.persistence.repository.deleteByAccountId
import com.calendar.couple.couple.infrastructure.persistence.repository.existsByAccountId
import com.calendar.couple.couple.infrastructure.persistence.repository.findByAccountId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CoupleService(
	private val accountRepository: AccountRepository,
	private val coupleRepository: CoupleRepository,
	private val calendarRepository: CalendarRepository,
	private val calendarMemberRepository: CalendarMemberRepository,
	private val invitationCodeRepository: InvitationCodeRepository,
) {
	@Transactional
	fun linkCouple(
		accountId: Long,
		invitationCode: String,
	): LinkCoupleResponse {
		val inviterAccountId =
			invitationCodeRepository.getInviterAccountIdByCode(invitationCode)
				?: throw CoupleException.InvalidInvitationCodeException()

		val inviterName = validateCoupleInvitation(accountId, inviterAccountId)

		val savedCoupleEntity = saveCouple(inviterAccountId, accountId)

		val coupleCalendar =
			Calendar(
				ownerId = accountId,
				name = "",
				type = CalendarType.COUPLE,
				color = "#ed3b3b",
			)

		val savedCalendarEntity = calendarRepository.save(coupleCalendar.toEntity())

		val calendarMember =
			CalendarMember(
				calendarId = savedCalendarEntity.id!!,
				accountId = inviterAccountId,
				role = CalendarMemberRole.MEMBER,
				status = CalendarMemberStatus.ACTIVE,
			)

		calendarMemberRepository.save(calendarMember.toEntity())

		invitationCodeRepository.delete(invitationCode)

		return LinkCoupleResponse(
			coupleId = savedCoupleEntity.id!!,
			partnerId = inviterAccountId,
			partnerName = inviterName,
			startDate = savedCoupleEntity.startDate,
			linkedAt = savedCoupleEntity.createdAt,
		)
	}

	@Transactional
	fun updateAdditionalInfo(
		startDate: LocalDate,
		accountId: Long,
	) {
		val couple =
			coupleRepository.findByAccountId(accountId)
				?: throw IllegalStateException("커플없음")

		val updatedCouple = couple.updateStartDate(startDate)

		coupleRepository.updateStartDate(updatedCouple.id!!, updatedCouple.startDate)
	}

	@Transactional
	fun unlinkCouple(accountId: Long) {
		coupleRepository.deleteByAccountId(accountId)
		// TODO -> 커플에 관련된 데이터 모두 soft delete
	}

	private fun validateCoupleInvitation(
		accountId: Long,
		inviterAccountId: Long,
	): String {
		if (accountId == inviterAccountId) throw CoupleException.SelfInvitationException()

		// 3. 이미 커플인지 확인 (초대한 사람)
		if (coupleRepository.existsByAccountId(inviterAccountId)) {
			throw CoupleException.AlreadyCoupledInviterException()
		}

		// 4. 이미 커플인지 확인 (초대받은 사람)
		if (coupleRepository.existsByAccountId(accountId)) {
			throw CoupleException.AlreadyCoupledInviteeException()
		}

		// 5. 두 계정이 모두 존재하는지 확인
		val inviterName =
			accountRepository.findByIdOrNull(inviterAccountId)?.name
				?: throw IllegalStateException("No Account")

		val isExistingAccount = accountRepository.existsById(accountId)

		if (!isExistingAccount) throw IllegalStateException("No Account")

		return inviterName
	}

	private fun saveCouple(
		inviterAccountId: Long,
		accountId: Long,
	): CoupleEntity {
		val couple =
			Couple(
				account1Id = inviterAccountId,
				account2Id = accountId,
				startDate = LocalDate.now(),
			)

		val savedCoupleEntity = coupleRepository.save(couple.toEntity())
		return savedCoupleEntity
	}
}