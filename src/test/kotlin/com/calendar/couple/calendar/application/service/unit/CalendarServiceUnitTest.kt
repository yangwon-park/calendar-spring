package com.calendar.couple.calendar.application.service.unit

import com.calendar.couple.calendar.api.dto.CalendarUpdateRequest
import com.calendar.couple.calendar.application.service.CalendarService
import com.calendar.couple.calendar.infrastructure.persistence.entity.CalendarEntity
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

@OptIn(ExperimentalKotest::class)
class CalendarServiceUnitTest :
	BehaviorSpec({
		data class CalendarServiceTestFixture(
			val calendarRepository: CalendarRepository,
			val service: CalendarService,
		)

		fun createFixture(): CalendarServiceTestFixture {
			val calendarRepository = mockk<CalendarRepository>()
			val service = CalendarService(calendarRepository)

			return CalendarServiceTestFixture(
				calendarRepository,
				service,
			)
		}

		Context("CalendarService의 캘린더 조회 로직 검증 목적 단위 테스트") {
			Given("사용자의 캘린더 목록이 존재할 때") {
				val fixture = createFixture()
				val testAccountId = 1L

				val calendar1 = mockk<CalendarEntity>(relaxed = true)
				every { calendar1.id } returns 1L
				every { calendar1.ownerId } returns testAccountId
				every { calendar1.name } returns "개인"
				every { calendar1.type } returns "PERSONAL"
				every { calendar1.color } returns "#3788D8"
				every { calendar1.description } returns "개인 캘린더"

				val calendar2 = mockk<CalendarEntity>(relaxed = true)
				every { calendar2.id } returns 2L
				every { calendar2.ownerId } returns testAccountId
				every { calendar2.name } returns "우리 둘"
				every { calendar2.type } returns "COUPLE"
				every { calendar2.color } returns "#FF6B9D"
				every { calendar2.description } returns "커플 캘린더"

				val calendarEntities = listOf(calendar1, calendar2)

				every { fixture.calendarRepository.findAllById(testAccountId) } returns calendarEntities

				When("캘린더 목록을 조회하면") {
					val result = fixture.service.getCalendars(testAccountId)

					Then("모든 캘린더가 반환된다") {
						result.size shouldBe 2
						result[0].calendarId shouldBe 1L
						result[0].name shouldBe "개인"
						result[0].type shouldBe "PERSONAL"
						result[0].color shouldBe "#3788D8"
						result[1].calendarId shouldBe 2L
						result[1].name shouldBe "우리 둘"
						result[1].type shouldBe "COUPLE"
					}

					Then("Repository 조회 메서드가 호출된다") {
						verify(exactly = 1) { fixture.calendarRepository.findAllById(testAccountId) }
					}
				}
			}

			Given("특정 캘린더 ID로 조회할 때") {
				val fixture = createFixture()
				val testCalendarId = 1L

				val calendarEntity = mockk<CalendarEntity>(relaxed = true)
				every { calendarEntity.id } returns testCalendarId
				every { calendarEntity.ownerId } returns 1L
				every { calendarEntity.name } returns "개인"
				every { calendarEntity.type } returns "PERSONAL"
				every { calendarEntity.color } returns "#3788D8"
				every { calendarEntity.description } returns "개인 캘린더"

				every { fixture.calendarRepository.findByIdOrNull(testCalendarId) } returns calendarEntity

				When("캘린더를 조회하면") {
					val result = fixture.service.getCalendarByCalendarId(testCalendarId)

					Then("해당 캘린더가 반환된다") {
						result shouldNotBe null
						result.calendarId shouldBe testCalendarId
						result.name shouldBe "개인"
						result.type shouldBe "PERSONAL"
						result.color shouldBe "#3788D8"
						result.description shouldBe "개인 캘린더"
					}

					Then("Repository 조회 메서드가 호출된다") {
						verify(exactly = 1) { fixture.calendarRepository.findByIdOrNull(testCalendarId) }
					}
				}
			}

			Given("존재하지 않는 캘린더 ID로 조회할 때") {
				val fixture = createFixture()
				val invalidCalendarId = 999L

				every { fixture.calendarRepository.findByIdOrNull(invalidCalendarId) } returns null

				When("캘린더를 조회하면") {
					Then("IllegalStateException이 발생한다") {
						val exception =
							shouldThrow<IllegalStateException> {
								fixture.service.getCalendarByCalendarId(invalidCalendarId)
							}
						exception.message shouldBe "Calendar not found"
					}
				}
			}
		}

		Context("CalendarService의 캘린더 업데이트 로직 검증 목적 단위 테스트") {
			Given("유효한 캘린더 업데이트 요청이 주어졌을 때") {
				val fixture = createFixture()
				val testCalendarId = 1L

				val existingCalendar = mockk<CalendarEntity>(relaxed = true)
				every { existingCalendar.id } returns testCalendarId
				every { existingCalendar.ownerId } returns 1L
				every { existingCalendar.name } returns "개인"
				every { existingCalendar.type } returns "PERSONAL"
				every { existingCalendar.color } returns "#3788D8"
				every { existingCalendar.description } returns "개인 캘린더"

				val updateRequest =
					CalendarUpdateRequest(
						name = "내 캘린더",
						type = "PERSONAL",
						color = "#FF0000",
						description = "수정된 캘린더",
					)

				every { fixture.calendarRepository.findByIdOrNull(testCalendarId) } returns existingCalendar
				every {
					fixture.calendarRepository.update(
						"내 캘린더",
						"PERSONAL",
						"#FF0000",
						"수정된 캘린더",
						testCalendarId,
					)
				} returns 1

				When("캘린더를 업데이트하면") {
					fixture.service.updateCalendar(testCalendarId, updateRequest)

					Then("Repository 업데이트 메서드가 호출된다") {
						verify(exactly = 1) { fixture.calendarRepository.findByIdOrNull(testCalendarId) }
						verify(exactly = 1) {
							fixture.calendarRepository.update(
								"내 캘린더",
								"PERSONAL",
								"#FF0000",
								"수정된 캘린더",
								testCalendarId,
							)
						}
					}
				}
			}

			Given("존재하지 않는 캘린더를 업데이트하려 할 때") {
				val fixture = createFixture()
				val invalidCalendarId = 999L

				val updateRequest =
					CalendarUpdateRequest(
						name = "내 캘린더",
						type = "PERSONAL",
						color = "#FF0000",
						description = "수정된 캘린더",
					)

				every { fixture.calendarRepository.findByIdOrNull(invalidCalendarId) } returns null

				When("캘린더를 업데이트하면") {
					Then("IllegalStateException이 발생한다") {
						val exception =
							shouldThrow<IllegalStateException> {
								fixture.service.updateCalendar(invalidCalendarId, updateRequest)
							}
						exception.message shouldBe "Calendar not found"
					}
				}
			}

			Given("업데이트가 실패한 경우") {
				val fixture = createFixture()
				val testCalendarId = 1L

				val existingCalendar = mockk<CalendarEntity>(relaxed = true)
				every { existingCalendar.id } returns testCalendarId
				every { existingCalendar.ownerId } returns 1L
				every { existingCalendar.name } returns "개인"
				every { existingCalendar.type } returns "PERSONAL"
				every { existingCalendar.color } returns "#3788D8"
				every { existingCalendar.description } returns "개인 캘린더"

				val updateRequest =
					CalendarUpdateRequest(
						name = "내 캘린더",
						type = "PERSONAL",
						color = "#FF0000",
						description = "수정된 캘린더",
					)

				every { fixture.calendarRepository.findByIdOrNull(testCalendarId) } returns existingCalendar
				every {
					fixture.calendarRepository.update(
						any(),
						any(),
						any(),
						any(),
						testCalendarId,
					)
				} returns 0

				When("캘린더를 업데이트하면") {
					Then("IllegalStateException이 발생한다") {
						val exception =
							shouldThrow<IllegalStateException> {
								fixture.service.updateCalendar(testCalendarId, updateRequest)
							}
						exception.message shouldBe "Calendar not found"
					}
				}
			}
		}
	})