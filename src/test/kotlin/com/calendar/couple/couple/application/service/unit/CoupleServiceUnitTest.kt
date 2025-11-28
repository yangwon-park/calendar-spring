package com.calendar.couple.couple.application.service.unit

import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarEntity
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarMemberEntity
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarMemberRepository
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarRepository
import com.calendar.couple.couple.application.service.CoupleService
import com.calendar.couple.couple.exception.CoupleException
import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity
import com.calendar.couple.couple.infrastructure.persistence.repository.CoupleRepository
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

@OptIn(ExperimentalKotest::class)
class CoupleServiceUnitTest :
	BehaviorSpec({
		data class CoupleServiceTestFixture(
			val accountRepository: AccountRepository,
			val coupleRepository: CoupleRepository,
			val calendarRepository: CalendarRepository,
			val calendarMemberRepository: CalendarMemberRepository,
			val invitationCodeRepository: InvitationCodeRepository,
			val service: CoupleService,
		)

		fun createFixture(): CoupleServiceTestFixture {
			val accountRepository = mockk<AccountRepository>()
			val coupleRepository = mockk<CoupleRepository>()
			val calendarRepository = mockk<CalendarRepository>()
			val calendarMemberRepository = mockk<CalendarMemberRepository>()
			val invitationCodeRepository = mockk<InvitationCodeRepository>()
			val service =
				CoupleService(
					accountRepository,
					coupleRepository,
					calendarRepository,
					calendarMemberRepository,
					invitationCodeRepository,
				)

			return CoupleServiceTestFixture(
				accountRepository,
				coupleRepository,
				calendarRepository,
				calendarMemberRepository,
				invitationCodeRepository,
				service,
			)
		}

		Context("CoupleService의 커플 연결 및 해제 로직 검증 목적 단위 테스트") {
			Context("커플 연결 성공 플로우") {
				Given("유효한 초대 코드와 계정 정보가 주어졌을 때") {
					val fixture = createFixture()
					val accountId = 2L
					val inviterAccountId = 1L
					val invitationCode = "ABC123"
					val inviterName = "초대한사람"
					val calendarId = 100L

					val mockInviterAccount =
						mockk<AccountEntity> {
							every { id } returns inviterAccountId
							every { name } returns inviterName
						}

					val mockCoupleEntity =
						mockk<CoupleEntity> {
							every { id } returns 1L
							every { account1Id } returns inviterAccountId
							every { account2Id } returns accountId
							every { startDate } returns LocalDate.now()
							every { createdAt } returns java.time.LocalDateTime.now()
						}

					val mockCalendarEntity =
						mockk<CalendarEntity> {
							every { id } returns calendarId
						}

					val mockCalendarMemberEntity = mockk<CalendarMemberEntity>()

					every { fixture.invitationCodeRepository.getInviterAccountIdByCode(invitationCode) } returns inviterAccountId
					every { fixture.coupleRepository.existsByAccount1IdOrAccount2Id(inviterAccountId, inviterAccountId) } returns false
					every { fixture.coupleRepository.existsByAccount1IdOrAccount2Id(accountId, accountId) } returns false
					every { fixture.accountRepository.findByIdOrNull(inviterAccountId) } returns mockInviterAccount
					every { fixture.accountRepository.existsById(accountId) } returns true
					every { fixture.coupleRepository.save(any()) } returns mockCoupleEntity
					every { fixture.calendarRepository.save(any()) } returns mockCalendarEntity
					every { fixture.calendarMemberRepository.save(any()) } returns mockCalendarMemberEntity
					every { fixture.invitationCodeRepository.delete(invitationCode) } returns true

					When("커플 연결을 요청하면") {
						val result = fixture.service.linkCouple(accountId, invitationCode)

						Then("커플 연결 결과를 반환한다") {
							result.coupleId shouldBe 1L
							result.partnerId shouldBe inviterAccountId
							result.partnerName shouldBe inviterName
							result.startDate shouldNotBe null
							result.linkedAt shouldNotBe null
						}

						Then("커플 엔티티가 저장된다") {
							verify(exactly = 1) { fixture.coupleRepository.save(any()) }
						}

						Then("커플 캘린더가 생성된다") {
							verify(exactly = 1) { fixture.calendarRepository.save(any()) }
						}

						Then("초대한 사람이 캘린더 멤버로 등록된다") {
							verify(exactly = 1) {
								fixture.calendarMemberRepository.save(
									match {
										it.calendarId == calendarId && it.accountId == inviterAccountId
									},
								)
							}
						}

						Then("초대 코드가 삭제된다") {
							verify(exactly = 1) { fixture.invitationCodeRepository.delete(invitationCode) }
						}
					}
				}
			}

			Context("커플 연결 실패 - 유효하지 않은 초대 코드") {
				Given("존재하지 않는 초대 코드가 주어졌을 때") {
					val fixture = createFixture()
					val accountId = 2L
					val invalidCode = "INVALID"

					every { fixture.invitationCodeRepository.getInviterAccountIdByCode(invalidCode) } returns null

					When("커플 연결을 요청하면") {
						Then("InvalidInvitationCodeException이 발생한다") {
							shouldThrow<CoupleException.InvalidInvitationCodeException> {
								fixture.service.linkCouple(accountId, invalidCode)
							}
						}
					}
				}
			}

			Context("커플 연결 실패 - 자기 자신 초대") {
				Given("초대한 사람과 초대받은 사람이 동일할 때") {
					val fixture = createFixture()
					val accountId = 1L
					val invitationCode = "ABC123"

					every { fixture.invitationCodeRepository.getInviterAccountIdByCode(invitationCode) } returns accountId

					When("커플 연결을 요청하면") {
						Then("SelfInvitationException이 발생한다") {
							shouldThrow<CoupleException.SelfInvitationException> {
								fixture.service.linkCouple(accountId, invitationCode)
							}
						}
					}
				}
			}

			Context("커플 연결 실패 - 초대한 사람이 이미 커플인 경우") {
				Given("초대한 사람이 이미 커플로 연결되어 있을 때") {
					val fixture = createFixture()
					val accountId = 2L
					val inviterAccountId = 1L
					val invitationCode = "ABC123"

					every { fixture.invitationCodeRepository.getInviterAccountIdByCode(invitationCode) } returns inviterAccountId
					every { fixture.coupleRepository.existsByAccount1IdOrAccount2Id(inviterAccountId, inviterAccountId) } returns true

					When("커플 연결을 요청하면") {
						Then("AlreadyCoupledInviterException이 발생한다") {
							shouldThrow<CoupleException.AlreadyCoupledInviterException> {
								fixture.service.linkCouple(accountId, invitationCode)
							}
						}
					}
				}
			}

			Context("커플 연결 실패 - 초대받은 사람이 이미 커플인 경우") {
				Given("초대받은 사람이 이미 커플로 연결되어 있을 때") {
					val fixture = createFixture()
					val accountId = 2L
					val inviterAccountId = 1L
					val invitationCode = "ABC123"

					every { fixture.invitationCodeRepository.getInviterAccountIdByCode(invitationCode) } returns inviterAccountId
					every { fixture.coupleRepository.existsByAccount1IdOrAccount2Id(inviterAccountId, inviterAccountId) } returns false
					every { fixture.coupleRepository.existsByAccount1IdOrAccount2Id(accountId, accountId) } returns true

					When("커플 연결을 요청하면") {
						Then("AlreadyCoupledInviteeException이 발생한다") {
							shouldThrow<CoupleException.AlreadyCoupledInviteeException> {
								fixture.service.linkCouple(accountId, invitationCode)
							}
						}
					}
				}
			}

			Context("커플 해제 성공 플로우") {
				Given("유효한 계정 ID가 주어졌을 때") {
					val fixture = createFixture()
					val accountId = 1L

					every { fixture.coupleRepository.deleteByAccount1IdOrAccount2Id(accountId, accountId) } just runs

					When("커플 해제를 요청하면") {
						fixture.service.unlinkCouple(accountId)

						Then("해당 계정과 연결된 커플 데이터가 삭제된다") {
							verify(exactly = 1) {
								fixture.coupleRepository.deleteByAccount1IdOrAccount2Id(accountId, accountId)
							}
						}
					}
				}
			}

			Context("커플 해제 - 여러 계정에 대한 검증") {
				Given("다양한 계정 ID들이 주어졌을 때") {
					val fixture = createFixture()

					every { fixture.coupleRepository.deleteByAccount1IdOrAccount2Id(any(), any()) } just runs

					When("각 계정별로 커플 해제를 요청하면") {
						val accountIds = listOf(1L, 2L, 100L, 999L)

						accountIds.forEach { accountId ->
							fixture.service.unlinkCouple(accountId)
						}

						Then("각 계정에 대해 deleteByAccountId가 호출된다") {
							accountIds.forEach { accountId ->
								verify(exactly = 1) {
									fixture.coupleRepository.deleteByAccount1IdOrAccount2Id(accountId, accountId)
								}
							}
						}
					}
				}
			}

			Context("커플 시작일 업데이트 성공 플로우") {
				Given("커플이 연결된 계정과 새로운 시작일이 주어졌을 때") {
					val fixture = createFixture()
					val accountId = 1L
					val coupleId = 100L
					val originalStartDate = LocalDate.of(2024, 1, 1)
					val newStartDate = LocalDate.of(2024, 12, 25)

					val mockCoupleEntity =
						mockk<CoupleEntity> {
							every { id } returns coupleId
							every { account1Id } returns accountId
							every { account2Id } returns 2L
							every { startDate } returns originalStartDate
						}

					every { fixture.coupleRepository.findByAccount1IdOrAccount2Id(accountId, accountId) } returns mockCoupleEntity
					every { fixture.coupleRepository.updateStartDate(coupleId, newStartDate) } just runs

					When("커플 시작일 업데이트를 요청하면") {
						fixture.service.updateAdditionalInfo(newStartDate, accountId)

						Then("새로운 시작일로 업데이트된다") {
							verify(exactly = 1) {
								fixture.coupleRepository.updateStartDate(coupleId, newStartDate)
							}
						}
					}
				}
			}

			Context("커플 시작일 업데이트 실패 - 커플이 존재하지 않는 경우") {
				Given("커플이 연결되지 않은 계정 ID가 주어졌을 때") {
					val fixture = createFixture()
					val accountId = 1L
					val newStartDate = LocalDate.of(2024, 12, 25)

					every { fixture.coupleRepository.findByAccount1IdOrAccount2Id(accountId, accountId) } returns null

					When("커플 시작일 업데이트를 요청하면") {
						Then("IllegalStateException이 발생한다") {
							shouldThrow<IllegalStateException> {
								fixture.service.updateAdditionalInfo(newStartDate, accountId)
							}
						}
					}
				}
			}
		}
	})