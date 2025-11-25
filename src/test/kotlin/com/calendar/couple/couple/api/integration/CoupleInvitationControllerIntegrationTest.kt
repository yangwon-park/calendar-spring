package com.calendar.couple.couple.api.integration

import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.auth.infrastructure.JwtProvider
import com.calendar.couple.couple.infrastructure.persistence.repository.InvitationCodeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.ExperimentalKotest
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container

@OptIn(ExperimentalKotest::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ApplyExtension(SpringExtension::class)
class CoupleInvitationControllerIntegrationTest(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val accountRepository: AccountRepository,
	private val jwtProvider: JwtProvider,
	private val invitationCodeRepository: InvitationCodeRepository,
) : BehaviorSpec({
		var testAccountId = 0L
		var testAccessToken = ""

		beforeSpec {
			val testAccount =
				AccountEntity(
					email = TEST_USER_EMAIL,
					password = null,
					name = "Test User",
					role = "USER",
					provider = TEST_PROVIDER,
				)

			val savedAccount = accountRepository.save(testAccount)
			testAccountId = savedAccount.id!!

			testAccessToken = jwtProvider.generateAccessToken(testAccountId, "USER")
		}

		afterSpec {
			accountRepository.deleteAll()
		}

		Context("커플 초대 코드 생성 요청 시, 인증 및 코드 생성 검증 목적 통합 테스트") {
			Given("[ 초대 코드 생성 ] - /api/couple/invitations (POST)") {
				Context("유효한 Request에 대한 검증") {
					When("인증된 사용자가 초대 코드 생성을 요청하면") {
						Then("성공 응답과 함께 6자리 초대 코드를 반환한다") {
							val result =
								mockMvc
									.perform(
										MockMvcRequestBuilders
											.post(CREATE_INVITATION_ENDPOINT)
											.header("Authorization", "Bearer $testAccessToken"),
									).andExpect(MockMvcResultMatchers.status().isOk)
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.invitationCode").exists())
									.andReturn()

							val response = objectMapper.readTree(result.response.contentAsString)
							val invitationCode = response.get("data").get("invitationCode").asText()

							invitationCode shouldMatch "^[A-Z0-9]{6}$"
						}
					}

					When("인증된 사용자가 초대 코드를 생성하면") {
						Then("Redis에 초대 코드가 저장된다") {
							val result =
								mockMvc
									.perform(
										MockMvcRequestBuilders
											.post(CREATE_INVITATION_ENDPOINT)
											.header("Authorization", "Bearer $testAccessToken"),
									).andExpect(MockMvcResultMatchers.status().isOk)
									.andReturn()

							val response = objectMapper.readTree(result.response.contentAsString)
							val invitationCode = response.get("data").get("invitationCode").asText()

							val storedInviterId = invitationCodeRepository.getInviterAccountIdByCode(invitationCode)
							storedInviterId shouldNotBe null
							storedInviterId shouldBe testAccountId
						}
					}
				}

				Context("인증되지 않은 Request에 대한 검증") {
					When("Authorization Header 없이 요청하면") {
						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(CREATE_INVITATION_ENDPOINT),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}

					When("유효하지 않은 Access Token으로 요청하면") {
						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(CREATE_INVITATION_ENDPOINT)
										.header("Authorization", "Bearer invalid-token"),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}

					When("Bearer 형식이 아닌 토큰으로 요청하면") {
						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(CREATE_INVITATION_ENDPOINT)
										.header("Authorization", "InvalidFormat $testAccessToken"),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}
				}
			}
		}
	}) {
	companion object {
		private const val TEST_USER_EMAIL = "invitation-test@example.com"
		private const val TEST_PROVIDER = "GOOGLE"

		private const val CREATE_INVITATION_ENDPOINT = "/api/couple/invitations"

		@Container
		@JvmStatic
		val redisContainer =
			GenericContainer("redis:7-alpine")
				.withExposedPorts(6379)

		@DynamicPropertySource
		@JvmStatic
		fun configureProperties(registry: DynamicPropertyRegistry) {
			if (!redisContainer.isRunning) redisContainer.start()

			registry.add("spring.data.redis.host") { redisContainer.host }
			registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
		}
	}
}