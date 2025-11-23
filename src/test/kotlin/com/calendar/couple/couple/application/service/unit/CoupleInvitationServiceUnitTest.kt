package com.calendar.couple.couple.application.service.unit

import com.calendar.couple.couple.application.service.CoupleInvitationService
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.*

@OptIn(ExperimentalKotest::class)
class CoupleInvitationServiceUnitTest :
	BehaviorSpec({
		data class CoupleInvitationServiceTestFixture(
			val invitationCodeRepository: InvitationCodeRepository,
			val service: CoupleInvitationService,
		)

		fun createFixture(): CoupleInvitationServiceTestFixture {
			val invitationCodeRepository = mockk<InvitationCodeRepository>()
			val service = CoupleInvitationService(invitationCodeRepository)

			return CoupleInvitationServiceTestFixture(
				invitationCodeRepository,
				service,
			)
		}

		Context("CoupleInvitationService의 초대 코드 생성, 조회, 사용 로직 검증 목적 단위 테스트") {
			Context("초대 코드 생성 성공 플로우") {
				Given("유효한 inviterId가 주어졌을 때") {
					val fixture = createFixture()
					val testInviterId = 1L

					every { fixture.invitationCodeRepository.getInviterIdByCode(any()) } returns null
					every { fixture.invitationCodeRepository.save(any(), testInviterId) } just runs

					When("초대 코드 생성을 요청하면") {
						val result = fixture.service.createInvitationCode(testInviterId)

						Then("초대 코드를 반환한다") {
							result.invitationCode shouldNotBe null
						}

						Then("초대 코드는 6자리 영문 대문자와 숫자 조합이다") {
							result.invitationCode shouldMatch "^[A-Z0-9]{6}$"
						}

						Then("초대 코드가 Redis에 저장된다") {
							verify(exactly = 1) { fixture.invitationCodeRepository.save(any(), testInviterId) }
						}
					}
				}
			}

			Context("초대 코드 생성 - 중복 코드 재시도 플로우") {
				Given("첫 번째 생성된 코드가 이미 존재하는 경우") {
					val fixture = createFixture()
					val testInviterId = 1L
					var callCount = 0

					every { fixture.invitationCodeRepository.getInviterIdByCode(any()) } answers {
						callCount++
						if (callCount == 1) 999L else null
					}
					every { fixture.invitationCodeRepository.save(any(), testInviterId) } just runs

					When("초대 코드 생성을 요청하면") {
						val result = fixture.service.createInvitationCode(testInviterId)

						Then("재시도하여 새로운 코드를 생성한다") {
							result.invitationCode shouldNotBe null
						}

						Then("중복 체크가 2번 이상 호출된다") {
							verify(atLeast = 2) { fixture.invitationCodeRepository.getInviterIdByCode(any()) }
						}
					}
				}
			}

			Context("초대 코드 생성 실패 - 최대 재시도 초과") {
				Given("5번 연속으로 중복 코드가 생성되는 경우") {
					val fixture = createFixture()
					val testInviterId = 1L

					every { fixture.invitationCodeRepository.getInviterIdByCode(any()) } returns 999L

					When("초대 코드 생성을 요청하면") {
						Then("IllegalStateException이 발생한다") {
							val exception = shouldThrow<IllegalStateException> {
								fixture.service.createInvitationCode(testInviterId)
							}
							exception.message shouldBe "초대 코드 생성 실패"
						}

						Then("중복 체크가 정확히 5번 호출된다") {
							verify(exactly = 5) { fixture.invitationCodeRepository.getInviterIdByCode(any()) }
						}

						Then("save는 호출되지 않는다") {
							verify(exactly = 0) { fixture.invitationCodeRepository.save(any(), any()) }
						}
					}
				}
			}

			Context("초대 코드로 inviterId 조회 플로우") {
				Given("유효한 초대 코드가 주어졌을 때") {
					val fixture = createFixture()
					val testCode = "ABC123"
					val testInviterId = 1L

					every { fixture.invitationCodeRepository.getInviterIdByCode(testCode) } returns testInviterId

					When("inviterId를 조회하면") {
						val result = fixture.service.getInviterId(testCode)

						Then("inviterId를 반환한다") {
							result shouldBe testInviterId
						}

						Then("Repository에서 조회가 수행된다") {
							verify(exactly = 1) { fixture.invitationCodeRepository.getInviterIdByCode(testCode) }
						}
					}
				}

				Given("존재하지 않는 초대 코드가 주어졌을 때") {
					val fixture = createFixture()
					val testCode = "INVALID"

					every { fixture.invitationCodeRepository.getInviterIdByCode(testCode) } returns null

					When("inviterId를 조회하면") {
						val result = fixture.service.getInviterId(testCode)

						Then("null을 반환한다") {
							result shouldBe null
						}
					}
				}
			}

			Context("초대 코드 사용(삭제) 플로우") {
				Given("유효한 초대 코드가 주어졌을 때") {
					val fixture = createFixture()
					val testCode = "ABC123"

					every { fixture.invitationCodeRepository.delete(testCode) } returns true

					When("초대 코드를 사용하면") {
						fixture.service.useInvitation(testCode)

						Then("Redis에서 초대 코드가 삭제된다") {
							verify(exactly = 1) { fixture.invitationCodeRepository.delete(testCode) }
						}
					}
				}
			}
		}
	})
