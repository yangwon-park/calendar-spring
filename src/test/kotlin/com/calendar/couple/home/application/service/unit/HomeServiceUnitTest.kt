package com.calendar.couple.home.application.service.unit

import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.auth.exception.AuthException
import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity
import com.calendar.couple.couple.infrastructure.persistence.repository.CoupleRepository
import com.calendar.couple.home.application.service.HomeService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.util.Optional

@OptIn(ExperimentalKotest::class)
class HomeServiceUnitTest :
	BehaviorSpec({
		data class HomeServiceTestFixture(
			val accountRepository: AccountRepository,
			val coupleRepository: CoupleRepository,
			val service: HomeService,
		)

		fun createFixture(): HomeServiceTestFixture {
			val accountRepository = mockk<AccountRepository>()
			val coupleRepository = mockk<CoupleRepository>()
			val service = HomeService(accountRepository, coupleRepository)

			return HomeServiceTestFixture(
				accountRepository,
				coupleRepository,
				service,
			)
		}

		Context("HomeService의 커플 홈 화면 정보 조회 로직 검증 목적 단위 테스트") {
			Context("커플 정보가 존재하는 경우") {
				Given("계정 정보와 커플 정보가 모두 존재할 때") {
					val fixture = createFixture()
					val testAccountId = 1L
					val testPartnerId = 2L
					val testStartDate = LocalDate.of(2024, 1, 1)

					val account = mockk<AccountEntity>(relaxed = true)
					every { account.id } returns testAccountId
					every { account.name } returns "테스트 유저"
					every { account.email } returns "test@example.com"
					every { account.password } returns null
					every { account.role } returns "USER"
					every { account.provider } returns "LOCAL"
					every { account.isDeleted } returns false
					every { account.isBanned } returns false
					every { account.isWithdraw } returns false

					val partner = mockk<AccountEntity>(relaxed = true)
					every { partner.id } returns testPartnerId
					every { partner.name } returns "파트너 유저"
					every { partner.email } returns "partner@example.com"
					every { partner.password } returns null
					every { partner.role } returns "USER"
					every { partner.provider } returns "LOCAL"
					every { partner.isDeleted } returns false
					every { partner.isBanned } returns false
					every { partner.isWithdraw } returns false

					val couple =
						CoupleEntity(
							account1Id = testAccountId,
							account2Id = testPartnerId,
							startDate = testStartDate,
						)

					every { fixture.accountRepository.findByIdOrNull(testAccountId) } returns account
					every { fixture.coupleRepository.findByAccount1IdOrAccount2Id(testAccountId, testAccountId) } returns couple
					every { fixture.accountRepository.findById(testPartnerId) } returns Optional.of(partner)

					When("커플 홈 정보를 조회하면") {
						val result = fixture.service.getCoupleInfo(testAccountId)

						Then("계정 정보가 포함된다") {
							result.accountInfo shouldNotBe null
							result.accountInfo.name shouldBe "테스트 유저"
						}

						Then("커플 정보가 포함된다") {
							result.coupleInfo shouldNotBe null
							result.coupleInfo?.partnerId shouldBe testPartnerId
							result.coupleInfo?.partnerName shouldBe "파트너 유저"
							result.coupleInfo?.startDate shouldBe testStartDate
						}

						Then("Repository 조회 메서드가 호출된다") {
							verify(exactly = 1) { fixture.accountRepository.findByIdOrNull(testAccountId) }
							verify(exactly = 1) { fixture.coupleRepository.findByAccount1IdOrAccount2Id(testAccountId, testAccountId) }
							verify(exactly = 1) { fixture.accountRepository.findById(testPartnerId) }
						}
					}
				}

				Given("본인이 account2Id인 커플 정보가 존재할 때") {
					val fixture = createFixture()
					val testAccountId = 2L
					val testPartnerId = 1L
					val testStartDate = LocalDate.of(2024, 6, 15)

					val account = mockk<AccountEntity>(relaxed = true)
					every { account.id } returns testAccountId
					every { account.name } returns "테스트 유저"
					every { account.email } returns "test@example.com"
					every { account.password } returns null
					every { account.role } returns "USER"
					every { account.provider } returns "LOCAL"
					every { account.isDeleted } returns false
					every { account.isBanned } returns false
					every { account.isWithdraw } returns false

					val partner = mockk<AccountEntity>(relaxed = true)
					every { partner.id } returns testPartnerId
					every { partner.name } returns "파트너 유저"
					every { partner.email } returns "partner@example.com"
					every { partner.password } returns null
					every { partner.role } returns "USER"
					every { partner.provider } returns "LOCAL"
					every { partner.isDeleted } returns false
					every { partner.isBanned } returns false
					every { partner.isWithdraw } returns false

					val couple =
						CoupleEntity(
							account1Id = testPartnerId,
							account2Id = testAccountId,
							startDate = testStartDate,
						)

					every { fixture.accountRepository.findByIdOrNull(testAccountId) } returns account
					every { fixture.coupleRepository.findByAccount1IdOrAccount2Id(testAccountId, testAccountId) } returns couple
					every { fixture.accountRepository.findById(testPartnerId) } returns Optional.of(partner)

					When("커플 홈 정보를 조회하면") {
						val result = fixture.service.getCoupleInfo(testAccountId)

						Then("올바른 파트너 정보가 포함된다") {
							result.coupleInfo shouldNotBe null
							result.coupleInfo?.partnerId shouldBe testPartnerId
							result.coupleInfo?.partnerName shouldBe "파트너 유저"
							result.coupleInfo?.startDate shouldBe testStartDate
						}
					}
				}
			}

			Context("커플 정보가 존재하지 않는 경우") {
				Given("계정만 존재하고 커플 정보가 없을 때") {
					val fixture = createFixture()
					val testAccountId = 1L

					val account = mockk<AccountEntity>(relaxed = true)
					every { account.id } returns testAccountId
					every { account.name } returns "테스트 유저"
					every { account.email } returns "test@example.com"
					every { account.password } returns null
					every { account.role } returns "USER"
					every { account.provider } returns "LOCAL"
					every { account.isDeleted } returns false
					every { account.isBanned } returns false
					every { account.isWithdraw } returns false

					every { fixture.accountRepository.findByIdOrNull(testAccountId) } returns account
					every { fixture.coupleRepository.findByAccount1IdOrAccount2Id(testAccountId, testAccountId) } returns null

					When("커플 홈 정보를 조회하면") {
						val result = fixture.service.getCoupleInfo(testAccountId)

						Then("계정 정보만 포함된다") {
							result.accountInfo shouldNotBe null
							result.accountInfo.name shouldBe "테스트 유저"
						}

						Then("커플 정보는 null이다") {
							result.coupleInfo shouldBe null
						}
					}
				}
			}

			Context("예외 발생 케이스") {
				Given("존재하지 않는 계정 ID로 조회 시") {
					val fixture = createFixture()
					val invalidAccountId = 999L

					every { fixture.accountRepository.findByIdOrNull(invalidAccountId) } returns null

					When("커플 홈 정보를 조회하면") {
						Then("IllegalStateException이 발생한다") {
							val exception =
								shouldThrow<IllegalStateException> {
									fixture.service.getCoupleInfo(invalidAccountId)
								}
							exception.message shouldBe "존재하지 않는 회원"
						}
					}
				}

				Given("커플 정보는 존재하나 파트너 계정이 존재하지 않을 때") {
					val fixture = createFixture()
					val testAccountId = 1L
					val testPartnerId = 2L

					val account = mockk<AccountEntity>(relaxed = true)
					every { account.id } returns testAccountId
					every { account.name } returns "테스트 유저"
					every { account.email } returns "test@example.com"
					every { account.password } returns null
					every { account.role } returns "USER"
					every { account.provider } returns "LOCAL"
					every { account.isDeleted } returns false
					every { account.isBanned } returns false
					every { account.isWithdraw } returns false

					val couple =
						CoupleEntity(
							account1Id = testAccountId,
							account2Id = testPartnerId,
							startDate = LocalDate.of(2024, 1, 1),
						)

					every { fixture.accountRepository.findByIdOrNull(testAccountId) } returns account
					every { fixture.coupleRepository.findByAccount1IdOrAccount2Id(testAccountId, testAccountId) } returns couple
					every { fixture.accountRepository.findById(testPartnerId) } returns Optional.empty()

					When("커플 홈 정보를 조회하면") {
						Then("AccountNotFoundException이 발생한다") {
							shouldThrow<AuthException.AccountNotFoundException> {
								fixture.service.getCoupleInfo(testAccountId)
							}
						}
					}
				}
			}
		}
	})