package com.calendar.couple.event.application.service.unit

import com.calendar.couple.event.api.dto.CreateEventRequest
import com.calendar.couple.event.application.service.EventService
import com.calendar.couple.event.infrastructure.persistence.entity.EventEntity
import com.calendar.couple.event.infrastructure.persistence.repository.EventRepository
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

@OptIn(ExperimentalKotest::class)
class EventServiceUnitTest :
	BehaviorSpec({
		data class EventServiceTestFixture(
			val eventRepository: EventRepository,
			val service: EventService,
		)

		fun createFixture(): EventServiceTestFixture {
			val eventRepository = mockk<EventRepository>()
			val service = EventService(eventRepository)

			return EventServiceTestFixture(
				eventRepository,
				service,
			)
		}

		Context("EventService의 이벤트 생성 로직 검증 목적 단위 테스트") {
			Given("유효한 이벤트 생성 요청이 주어졌을 때") {
				val fixture = createFixture()
				val testAccountId = 1L
				val testCalendarId = 1L
				val testCategoryId = 1L
				val testStartAt = LocalDateTime.of(2025, 1, 1, 10, 0)
				val testEndAt = LocalDateTime.of(2025, 1, 1, 12, 0)

				val request =
					CreateEventRequest(
						calendarId = testCalendarId,
						categoryId = testCategoryId,
						title = "회의",
						description = "팀 회의",
						isAllDay = false,
						startAt = testStartAt,
						endAt = testEndAt,
					)

				val entitySlot = slot<EventEntity>()
				val savedEntity =
					EventEntity(
						accountId = testAccountId,
						calendarId = testCalendarId,
						categoryId = testCategoryId,
						title = "회의",
						description = "팀 회의",
						isAllDay = false,
						startAt = testStartAt,
						endAt = testEndAt,
					)

				every { fixture.eventRepository.save(capture(entitySlot)) } returns savedEntity

				When("이벤트를 생성하면") {
					fixture.service.createEvent(request, testAccountId)

					Then("EventRepository의 save 메서드가 호출된다") {
						verify(exactly = 1) { fixture.eventRepository.save(any()) }
					}

					Then("저장된 엔티티의 정보가 올바르다") {
						val capturedEntity = entitySlot.captured
						capturedEntity.accountId shouldBe testAccountId
						capturedEntity.calendarId shouldBe testCalendarId
						capturedEntity.categoryId shouldBe testCategoryId
						capturedEntity.title shouldBe "회의"
						capturedEntity.description shouldBe "팀 회의"
						capturedEntity.isAllDay shouldBe false
						capturedEntity.startAt shouldBe testStartAt
						capturedEntity.endAt shouldBe testEndAt
					}
				}
			}

			Given("하루 종일 이벤트 생성 요청이 주어졌을 때") {
				val fixture = createFixture()
				val testAccountId = 1L
				val testCalendarId = 1L
				val testCategoryId = 1L
				val testStartAt = LocalDateTime.of(2025, 1, 1, 0, 0)

				val request =
					CreateEventRequest(
						calendarId = testCalendarId,
						categoryId = testCategoryId,
						title = "휴가",
						description = "연차",
						isAllDay = true,
						startAt = testStartAt,
						endAt = null,
					)

				val entitySlot = slot<EventEntity>()
				val savedEntity =
					EventEntity(
						accountId = testAccountId,
						calendarId = testCalendarId,
						categoryId = testCategoryId,
						title = "휴가",
						description = "연차",
						isAllDay = true,
						startAt = testStartAt,
						endAt = null,
					)

				every { fixture.eventRepository.save(capture(entitySlot)) } returns savedEntity

				When("하루 종일 이벤트를 생성하면") {
					fixture.service.createEvent(request, testAccountId)

					Then("EventRepository의 save 메서드가 호출된다") {
						verify(exactly = 1) { fixture.eventRepository.save(any()) }
					}

					Then("저장된 엔티티가 하루 종일 이벤트로 저장된다") {
						val capturedEntity = entitySlot.captured
						capturedEntity.accountId shouldBe testAccountId
						capturedEntity.calendarId shouldBe testCalendarId
						capturedEntity.categoryId shouldBe testCategoryId
						capturedEntity.title shouldBe "휴가"
						capturedEntity.description shouldBe "연차"
						capturedEntity.isAllDay shouldBe true
						capturedEntity.startAt shouldBe testStartAt
						capturedEntity.endAt shouldBe null
					}
				}
			}

			Given("설명이 없는 이벤트 생성 요청이 주어졌을 때") {
				val fixture = createFixture()
				val testAccountId = 1L
				val testCalendarId = 1L
				val testCategoryId = 1L
				val testStartAt = LocalDateTime.of(2025, 1, 1, 14, 0)
				val testEndAt = LocalDateTime.of(2025, 1, 1, 15, 0)

				val request =
					CreateEventRequest(
						calendarId = testCalendarId,
						categoryId = testCategoryId,
						title = "미팅",
						description = null,
						isAllDay = false,
						startAt = testStartAt,
						endAt = testEndAt,
					)

				val entitySlot = slot<EventEntity>()
				val savedEntity =
					EventEntity(
						accountId = testAccountId,
						calendarId = testCalendarId,
						categoryId = testCategoryId,
						title = "미팅",
						description = null,
						isAllDay = false,
						startAt = testStartAt,
						endAt = testEndAt,
					)

				every { fixture.eventRepository.save(capture(entitySlot)) } returns savedEntity

				When("이벤트를 생성하면") {
					fixture.service.createEvent(request, testAccountId)

					Then("설명 없이 이벤트가 저장된다") {
						val capturedEntity = entitySlot.captured
						capturedEntity.accountId shouldBe testAccountId
						capturedEntity.calendarId shouldBe testCalendarId
						capturedEntity.categoryId shouldBe testCategoryId
						capturedEntity.title shouldBe "미팅"
						capturedEntity.description shouldBe null
						capturedEntity.isAllDay shouldBe false
						capturedEntity.startAt shouldBe testStartAt
						capturedEntity.endAt shouldBe testEndAt
					}
				}
			}
		}
	})