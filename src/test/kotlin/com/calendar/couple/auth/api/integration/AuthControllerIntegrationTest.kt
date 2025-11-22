package com.calendar.couple.auth.api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity
import com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity
import com.calendar.couple.account.infrastructure.persistence.repository.AccountProviderRepository
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.auth.api.dto.RefreshTokenRequest
import com.calendar.couple.auth.api.dto.SignInRequest
import com.calendar.couple.auth.infrastructure.oauth2.OAuth2Client
import com.calendar.couple.auth.infrastructure.oauth2.OAuth2ClientFactory
import com.calendar.couple.auth.infrastructure.oauth2.OAuth2UserInfo
import com.calendar.couple.auth.infrastructure.persistence.repository.TokenRepository
import io.kotest.common.ExperimentalKotest
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
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
class AuthControllerIntegrationTest(
	private val mockMvc: MockMvc,
	private val objectMapper: ObjectMapper,
	private val accountRepository: AccountRepository,
	private val accountProviderRepository: AccountProviderRepository,
	private val tokenRepository: TokenRepository,
	@MockkBean private val oauth2ClientFactory: OAuth2ClientFactory,
) : BehaviorSpec({
		var testAccountId = 0L

		beforeSpec {
			// OAuth2 사용자를 위한 테스트 계정 생성
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

			// AccountProvider 생성
			accountProviderRepository.save(
				AccountProviderEntity.create(
					accountId = testAccountId,
					provider = TEST_PROVIDER,
					providerUserId = TEST_PROVIDER_USER_ID,
				),
			)
		}

		Context("OAuth2 로그인 요청 시, Request 및 OAuth2 인증 정보 검증 목적 통합 테스트") {
			Given("[ OAuth2 로그인 ] - /api/auth/sign-in (POST)") {
				Context("유효한 Request에 대한 검증 - 기존 회원") {
					When("유효한 OAuth2 code와 provider로 로그인 요청시") {
						val mockOAuth2Client = io.mockk.mockk<OAuth2Client>()
						val testUserInfo =
							OAuth2UserInfo(
								id = TEST_PROVIDER_USER_ID,
								email = TEST_USER_EMAIL,
								name = "Test User",
							)

						// OAuth2Client 모킹
						every { oauth2ClientFactory.getClient(TEST_PROVIDER) } returns mockOAuth2Client
						every { mockOAuth2Client.getAccessToken(TEST_OAUTH_CODE) } returns "provider-access-token"
						every { mockOAuth2Client.getUserInfo("provider-access-token") } returns testUserInfo

						val request =
							SignInRequest(
								code = TEST_OAUTH_CODE,
								provider = TEST_PROVIDER,
							)

						Then("성공 응답을 반환하고 Redis에 Refresh Token이 저장된다") {
							val result =
								mockMvc
									.perform(
										MockMvcRequestBuilders
											.post(SIGN_IN_ENDPOINT)
											.contentType(MediaType.APPLICATION_JSON)
											.content(objectMapper.writeValueAsString(request)),
									).andExpect(MockMvcResultMatchers.status().isOk)
									.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists())
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.refreshToken").exists())
									.andReturn()

							val response = objectMapper.readTree(result.response.contentAsString)
							val refreshToken = response.get("data").get("refreshToken").asText()

							val storedRefreshToken = tokenRepository.getRefreshTokenByAccountId(testAccountId)
							storedRefreshToken shouldNotBe null
							storedRefreshToken shouldBe refreshToken
						}
					}
				}

				Context("유효한 Request에 대한 검증 - 신규 회원") {
					When("유효한 OAuth2 code와 provider로 첫 로그인 요청시") {
						val mockOAuth2Client = io.mockk.mockk<OAuth2Client>()
						val newUserInfo =
							OAuth2UserInfo(
								id = "new-user-123",
								email = "newuser@example.com",
								name = "New User",
							)

						// OAuth2Client 모킹
						every { oauth2ClientFactory.getClient(TEST_PROVIDER) } returns mockOAuth2Client
						every { mockOAuth2Client.getAccessToken("new-user-code") } returns "new-provider-access-token"
						every { mockOAuth2Client.getUserInfo("new-provider-access-token") } returns newUserInfo

						val request =
							SignInRequest(
								code = "new-user-code",
								provider = TEST_PROVIDER,
							)

						Then("새 계정이 생성되고 성공 응답을 반환한다") {
							val result =
								mockMvc
									.perform(
										MockMvcRequestBuilders
											.post(SIGN_IN_ENDPOINT)
											.contentType(MediaType.APPLICATION_JSON)
											.content(objectMapper.writeValueAsString(request)),
									).andExpect(MockMvcResultMatchers.status().isOk)
									.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists())
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.refreshToken").exists())
									.andReturn()

							// 새 Account가 생성되었는지 확인
							val newAccount = accountRepository.findAll().find { it.email == "newuser@example.com" }
							newAccount shouldNotBe null
							newAccount?.role shouldBe "USER"

							// AccountProvider도 생성되었는지 확인
							val newAccountProvider =
								accountProviderRepository.findByProviderAndProviderUserId(
									TEST_PROVIDER,
									"new-user-123",
								)
							newAccountProvider shouldNotBe null
							newAccountProvider?.accountId shouldBe newAccount?.id
						}
					}
				}

				Context("유효하지 않은 Request에 대한 검증") {
					When("code가 빈 문자열로 요청시") {
						val request =
							SignInRequest(
								code = "",
								provider = TEST_PROVIDER,
							)

						Then("400 Bad Request를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(SIGN_IN_ENDPOINT)
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request)),
								).andExpect(MockMvcResultMatchers.status().isBadRequest)
								.andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
						}
					}

					When("provider가 빈 문자열로 요청시") {
						val request =
							SignInRequest(
								code = TEST_OAUTH_CODE,
								provider = "",
							)

						Then("400 Bad Request를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(SIGN_IN_ENDPOINT)
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(request)),
								).andExpect(MockMvcResultMatchers.status().isBadRequest)
								.andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
						}
					}
				}
			}
		}

		Context("Access Token이 만료되어 토큰 재요청 시, 토큰 유효성 및 인증 정보 유무 검증 목적 통합 테스트") {
			Given("[ 토큰 갱신 ] - /api/auth/refresh (POST)") {
				Context("유효한 Request에 대한 검증") {
					When("유효한 Access Token과 Refresh Token으로 요청시") {
						val mockOAuth2Client = io.mockk.mockk<OAuth2Client>()
						val testUserInfo =
							OAuth2UserInfo(
								id = TEST_PROVIDER_USER_ID,
								email = TEST_USER_EMAIL,
								name = "Test User",
							)

						every { oauth2ClientFactory.getClient(TEST_PROVIDER) } returns mockOAuth2Client
						every { mockOAuth2Client.getAccessToken(TEST_OAUTH_CODE) } returns "provider-access-token"
						every { mockOAuth2Client.getUserInfo("provider-access-token") } returns testUserInfo

						val signInRequest =
							SignInRequest(
								code = TEST_OAUTH_CODE,
								provider = TEST_PROVIDER,
							)

						val signInResult =
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(SIGN_IN_ENDPOINT)
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(signInRequest)),
								).andReturn()

						val signInResponse = objectMapper.readTree(signInResult.response.contentAsString)

						val refreshToken = signInResponse.get("data").get("refreshToken").asText()

						val refreshRequest = RefreshTokenRequest(refreshToken = refreshToken)

						Then("새로운 토큰을 반환하고 Redis에 새 Refresh Token이 저장된다") {
							// 이전 Refresh Token 확인
							val oldStoredToken = tokenRepository.getRefreshTokenByAccountId(testAccountId)
							oldStoredToken shouldBe refreshToken

							val result =
								mockMvc
									.perform(
										MockMvcRequestBuilders
											.post(REFRESH_ENDPOINT)
											.contentType(MediaType.APPLICATION_JSON)
											.content(objectMapper.writeValueAsString(refreshRequest)),
									).andExpect(MockMvcResultMatchers.status().isOk)
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists())
									.andExpect(MockMvcResultMatchers.jsonPath("$.data.refreshToken").exists())
									.andReturn()

							// 새로운 Refresh Token이 Redis에 저장되었는지 검증
							val refreshResponse = objectMapper.readTree(result.response.contentAsString)
							val newRefreshToken = refreshResponse.get("data").get("refreshToken").asText()

							val newStoredToken = tokenRepository.getRefreshTokenByAccountId(testAccountId)
							newStoredToken shouldNotBe null
							newStoredToken shouldBe newRefreshToken
							newStoredToken shouldNotBe refreshToken
						}
					}
				}

				Context("유효하지 않은 Request에 대한 검증") {
					When("유효하지 않은 Refresh Token으로 요청시") {
						val invalidRefreshRequest = RefreshTokenRequest(refreshToken = "invalid-refresh-token")

						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(REFRESH_ENDPOINT)
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(invalidRefreshRequest)),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
								.andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
						}
					}
				}
			}
		}

		Context("로그아웃 요청 시, 인증 정보 및 RefreshToken 삭제 검증 목적 통합 테스트") {
			Given("[ 로그아웃 ] - /api/auth/logout (DELETE)") {
				Context("유효한 Request에 대한 검증") {
					When("유효한 Access Token으로 로그아웃 요청시") {
						val mockOAuth2Client = io.mockk.mockk<OAuth2Client>()
						val testUserInfo =
							OAuth2UserInfo(
								id = TEST_PROVIDER_USER_ID,
								email = TEST_USER_EMAIL,
								name = "Test User",
							)

						every { oauth2ClientFactory.getClient(TEST_PROVIDER) } returns mockOAuth2Client
						every { mockOAuth2Client.getAccessToken(TEST_OAUTH_CODE) } returns "provider-access-token"
						every { mockOAuth2Client.getUserInfo("provider-access-token") } returns testUserInfo

						val signInRequest =
							SignInRequest(
								code = TEST_OAUTH_CODE,
								provider = TEST_PROVIDER,
							)

						val signInResult =
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(SIGN_IN_ENDPOINT)
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(signInRequest)),
								).andReturn()

						val signInResponse = objectMapper.readTree(signInResult.response.contentAsString)
						val accessToken = signInResponse.get("data").get("accessToken").asText()
						val refreshToken = signInResponse.get("data").get("refreshToken").asText()

						Then("성공 응답을 반환하고 Redis의 Refresh Token이 삭제된다") {
							// 로그아웃 전 Refresh Token 존재 확인
							val storedTokenBeforeLogout = tokenRepository.getRefreshTokenByAccountId(testAccountId)
							storedTokenBeforeLogout shouldBe refreshToken

							// 로그아웃 요청
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.delete(LOGOUT_ENDPOINT)
										.header("Authorization", "Bearer $accessToken"),
								).andExpect(MockMvcResultMatchers.status().isOk)

							// 로그아웃 후 Refresh Token 삭제 확인
							val storedTokenAfterLogout = tokenRepository.getRefreshTokenByAccountId(testAccountId)
							storedTokenAfterLogout shouldBe null
						}
					}
				}

				Context("유효하지 않은 Request에 대한 검증") {
					When("유효하지 않은 Access Token으로 요청시") {
						val invalidAccessToken = "invalid-access-token"

						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.delete(LOGOUT_ENDPOINT)
										.header("Authorization", "Bearer $invalidAccessToken"),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}

					When("만료된 Access Token으로 요청시") {
						// JWT Provider를 이용해 만료된 토큰을 생성하거나, 잘못된 서명의 토큰 사용
						val expiredAccessToken = "expired.access.token"

						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.delete(LOGOUT_ENDPOINT)
										.header("Authorization", "Bearer $expiredAccessToken"),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}
				}

				Context("인증 정보가 없는 사용자에 대한 검증") {
					When("Authorization Header 없이 요청시") {
						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.delete(LOGOUT_ENDPOINT),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}

					When("Bearer 토큰 형식이 아닌 경우") {
						Then("401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.delete(LOGOUT_ENDPOINT)
										.header("Authorization", "InvalidFormat some-token"),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						}
					}
				}

				Context("로그아웃 후 블랙리스트 토큰 재사용 차단 검증") {
					When("로그아웃 후 동일한 Access Token으로 API 접근 시도시") {
						// 먼저 OAuth2 로그인으로 토큰 발급 - 다른 사용자 사용
						val mockOAuth2Client = io.mockk.mockk<OAuth2Client>()
						val blacklistTestUserInfo =
							OAuth2UserInfo(
								id = "logout-user-id",
								email = "logout@example.com",
								name = "Logout Test User",
							)

						val blacklistTestCode = "logout-test-code"

						every { oauth2ClientFactory.getClient(TEST_PROVIDER) } returns mockOAuth2Client
						every { mockOAuth2Client.getAccessToken(blacklistTestCode) } returns "provider-access-token-logout"
						every { mockOAuth2Client.getUserInfo("provider-access-token-logout") } returns blacklistTestUserInfo

						val signInRequest =
							SignInRequest(
								code = blacklistTestCode,
								provider = TEST_PROVIDER,
							)

						val signInResult =
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.post(SIGN_IN_ENDPOINT)
										.contentType(MediaType.APPLICATION_JSON)
										.content(objectMapper.writeValueAsString(signInRequest)),
								).andReturn()

						val signInResponse = objectMapper.readTree(signInResult.response.contentAsString)
						val accessToken = signInResponse.get("data").get("accessToken").asText()

						// 로그아웃 수행 - 로그인 직후에는 토큰이 유효해야 함
						mockMvc
							.perform(
								MockMvcRequestBuilders
									.delete(LOGOUT_ENDPOINT)
									.header("Authorization", "Bearer $accessToken"),
							).andExpect(MockMvcResultMatchers.status().isOk)

						// 블랙리스트에 추가된 토큰이 저장되었는지 확인
						val isBlacklisted = tokenRepository.isAccessTokenInBlacklist(accessToken)
						isBlacklisted shouldBe true

						Then("로그아웃된 Access Token으로 로그아웃 재시도 시 401 Unauthorized를 반환한다") {
							mockMvc
								.perform(
									MockMvcRequestBuilders
										.delete(LOGOUT_ENDPOINT)
										.header("Authorization", "Bearer $accessToken"),
								).andExpect(MockMvcResultMatchers.status().isUnauthorized)
								.andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
						}
					}
				}
			}
		}
	}) {
	companion object {
		private const val TEST_USER_EMAIL = "test@whoflex.com"
		private const val TEST_PROVIDER = "GOOGLE"
		private const val TEST_PROVIDER_USER_ID = "google-user-test-123"
		private const val TEST_OAUTH_CODE = "valid-oauth2-code"

		private const val SIGN_IN_ENDPOINT = "/api/auth/sign-in"
		private const val REFRESH_ENDPOINT = "/api/auth/refresh"
		private const val LOGOUT_ENDPOINT = "/api/auth/logout"

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