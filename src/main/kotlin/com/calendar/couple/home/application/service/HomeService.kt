package com.calendar.couple.home.application.service

import com.calendar.couple.account.infrastructure.AccountMapper.toDomain
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.auth.exception.AuthException
import com.calendar.couple.couple.infrastructure.persistence.repository.CoupleRepository
import com.calendar.couple.couple.infrastructure.persistence.repository.findByAccountId
import com.calendar.couple.event.infrastructure.EventMapper.toDomain
import com.calendar.couple.event.infrastructure.persistence.repository.EventRepository
import com.calendar.couple.home.api.dto.AccountInfo
import com.calendar.couple.home.api.dto.CoupleInfo
import com.calendar.couple.home.api.dto.EventInfo
import com.calendar.couple.home.api.dto.HomeCoupleInfo
import com.calendar.couple.home.api.dto.HomeResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

private val log = mu.KotlinLogging.logger {}

@Service
class HomeService(
	private val accountRepository: AccountRepository,
	private val eventRepository: EventRepository,
	private val coupleRepository: CoupleRepository,
) {
	fun getHomeInfo(accountId: Long): HomeResponse {
		val eventDomains =
			eventRepository.findAllByAccountId(accountId).map {
				it.toDomain()
			}

		val eventInfos =
			eventDomains.map {
				EventInfo(
					it.calendarId,
					it.categoryId,
					it.eventAt,
				)
			}

		return HomeResponse(eventInfos)
	}

	fun getCoupleInfo(accountId: Long): HomeCoupleInfo {
		val account =
			accountRepository.findByIdOrNull(accountId)?.toDomain()
				?: throw IllegalStateException("존재하지 않는 회원")

		val accountInfo =
			AccountInfo(
				name = account.name,
			)

		// 2. 커플 정보 조회 (nullable)
		val coupleInfo =
			coupleRepository.findByAccountId(accountId)?.let { couple ->
				val partnerId =
					if (couple.account1Id == accountId) {
						couple.account2Id
					} else {
						couple.account1Id
					}

				val partner =
					accountRepository
						.findById(partnerId)
						.orElseThrow { AuthException.AccountNotFoundException() }

				CoupleInfo(
					partnerId = partnerId,
					partnerName = partner.name,
					startDate = couple.startDate,
				)
			}

		log.info { "getCoupleInfo: $coupleInfo" }

		return HomeCoupleInfo(
			accountInfo = accountInfo,
			coupleInfo = coupleInfo,
		)
	}
}