package com.calendar.couple.auth.application.service.unit

import com.calendar.couple.account.domain.enums.AccountRole
import com.calendar.couple.account.infrastructure.persistence.repository.AccountProviderRepository
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.auth.application.service.AuthService
import com.calendar.couple.auth.exception.JwtAuthException
import com.calendar.couple.auth.infrastructure.JwtProvider
import com.calendar.couple.auth.infrastructure.RefreshTokenInfo
import com.calendar.couple.auth.infrastructure.oauth2.OAuth2ClientFactory
import com.calendar.couple.auth.infrastructure.persistence.repository.TokenRepository
import com.calendar.couple.calendar.infrastructure.persistence.repository.CalendarRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalKotest::class)
class AuthServiceUnitTest :
	BehaviorSpec({
		data class AuthServiceTestFixture(
			val oauth2ClientFactory: OAuth2ClientFactory,
			val accountRepository: AccountRepository,
			val accountProviderRepository: AccountProviderRepository,
			val calendarRepository: CalendarRepository,
			val tokenRepository: TokenRepository,
			val jwtProvider: JwtProvider,
			val service: AuthService,
		)

		fun createAuthServiceFixture(): AuthServiceTestFixture {
			val oAuth2ClientFactory = mockk<OAuth2ClientFactory>()
			val accountRepository = mockk<AccountRepository>()
			val accountProviderRepository = mockk<AccountProviderRepository>()
			val calendarRepository = mockk<CalendarRepository>()
			val tokenRepository = mockk<TokenRepository>()
			val jwtProvider = mockk<JwtProvider>()
			val service =
				AuthService(
					oAuth2ClientFactory,
					accountRepository,
					accountProviderRepository,
					calendarRepository,
					tokenRepository,
					jwtProvider,
				)

			return AuthServiceTestFixture(
				oAuth2ClientFactory,
				accountRepository,
				accountProviderRepository,
				calendarRepository,
				tokenRepository,
				jwtProvider,
				service,
			)
		}

		Context("AuthService의 로그인, 로그아웃, 토큰 재발급 로직 검증 목적 단위 테스트") {
			Context("OAuth2 로그인 성공 플로우 - 신규 회원") {
				Given("OAuth2 인증 코드가 주어지고, 기존에 가입하지 않은 사용자인 경우") {
					val fixture = createAuthServiceFixture()

					val testAccessToken = "valid-oauth2-access-token"
					val testProvider = "GOOGLE"
					val testUserInfo =
						com.calendar.couple.auth.infrastructure.oauth2.OAuth2UserInfo(
							id = "google-user-123",
							email = "newuser@example.com",
							name = "New User",
						)
					val testExpiration = Instant.now().plus(Duration.ofDays(7))
					val mockOAuth2Client = mockk<com.calendar.couple.auth.infrastructure.oauth2.OAuth2Client>()

					// OAuth2 Client 모킹
					every { fixture.oauth2ClientFactory.getClient(testProvider) } returns mockOAuth2Client
					every { mockOAuth2Client.getUserInfo(testAccessToken) } returns testUserInfo

					// 기존 AccountProvider 없음 (신규 회원)
					every {
						fixture.accountProviderRepository.findByProviderAndProviderUserId(testProvider, testUserInfo.id)
					} returns null

					val savedAccountEntity =
						com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity(
							email = testUserInfo.email,
							name = testUserInfo.name,
							role = "USER",
							provider = testProvider,
						)
					// ID를 설정하기 위해 리플렉션 사용
					val idField = savedAccountEntity.javaClass.getDeclaredField("id")
					idField.isAccessible = true
					idField.set(savedAccountEntity, 1L)

					every { fixture.accountRepository.save(any()) } returns savedAccountEntity

					// AccountProvider 저장
					every { fixture.accountProviderRepository.save(any()) } returns
						com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity.create(
							1L,
							testProvider,
							testUserInfo.id,
						)

					// 개인 캘린더 자동 생성
					every { fixture.calendarRepository.save(any()) } returns mockk()

					// JWT 생성
					every { fixture.jwtProvider.generateAccessToken(1L, AccountRole.USER.name) } returns "ACCESS-TOKEN-USER"
					every { fixture.jwtProvider.generateRefreshToken(1L, AccountRole.USER.name) } returns
						RefreshTokenInfo("REFRESH-TOKEN", testExpiration)

					// RefreshToken 저장
					every { fixture.tokenRepository.saveRefreshToken(any()) } just runs

					When("OAuth2 로그인을 수행하면") {
						val result = fixture.service.signIn(testAccessToken, testProvider)

						Then("액세스 토큰과 리프레시 토큰을 반환한다") {
							result.accessToken shouldBe "ACCESS-TOKEN-USER"
							result.refreshToken shouldBe "REFRESH-TOKEN"
						}

						Then("OAuth2 Client로 인증을 수행한다") {
							verify(exactly = 1) { fixture.oauth2ClientFactory.getClient(testProvider) }
							verify(exactly = 1) { mockOAuth2Client.getUserInfo(testAccessToken) }
						}

						Then("새 Account와 AccountProvider를 생성한다") {
							verify(exactly = 1) { fixture.accountRepository.save(any()) }
							verify(exactly = 1) { fixture.accountProviderRepository.save(any()) }
						}

						Then("JWT 토큰을 생성하고 RefreshToken을 저장한다") {
							verify(exactly = 1) { fixture.jwtProvider.generateAccessToken(1L, "USER") }
							verify(exactly = 1) { fixture.jwtProvider.generateRefreshToken(1L, "USER") }
							verify(exactly = 1) { fixture.tokenRepository.saveRefreshToken(any()) }
						}
					}
				}
			}

			Context("OAuth2 로그인 성공 플로우 - 기존 회원") {
				Given("OAuth2 인증 코드가 주어지고, 이미 가입된 사용자인 경우") {
					val fixture = createAuthServiceFixture()

					val testCode = "valid-oauth2-code"
					val testProvider = "GOOGLE"
					val testUserInfo =
						com.calendar.couple.auth.infrastructure.oauth2.OAuth2UserInfo(
							id = "google-user-456",
							email = "existinguser@example.com",
							name = "Existing User",
						)
					val testExpiration = Instant.now().plus(Duration.ofDays(7))
					val mockOAuth2Client = mockk<com.calendar.couple.auth.infrastructure.oauth2.OAuth2Client>()

					// OAuth2 Client 모킹
					every { fixture.oauth2ClientFactory.getClient(testProvider) } returns mockOAuth2Client
					every { mockOAuth2Client.getUserInfo(testCode) } returns testUserInfo

					// 기존 AccountProvider 존재
					val existingAccountProvider =
						com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity.create(
							accountId = 2L,
							provider = testProvider,
							providerUserId = testUserInfo.id,
						)
					every {
						fixture.accountProviderRepository.findByProviderAndProviderUserId(testProvider, testUserInfo.id)
					} returns existingAccountProvider

					// 기존 Account 조회
					val existingAccount =
						com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity(
							email = testUserInfo.email,
							name = testUserInfo.name,
							role = "USER",
							provider = testProvider,
						)
					val idField = existingAccount.javaClass.getDeclaredField("id")
					idField.isAccessible = true
					idField.set(existingAccount, 2L)

					every { fixture.accountRepository.findByIdOrNull(2L) } returns existingAccount

					// JWT 생성
					every { fixture.jwtProvider.generateAccessToken(2L, AccountRole.USER.name) } returns "ACCESS-TOKEN-EXISTING"
					every { fixture.jwtProvider.generateRefreshToken(2L, AccountRole.USER.name) } returns
						RefreshTokenInfo("REFRESH-TOKEN-EXISTING", testExpiration)

					// RefreshToken 저장
					every { fixture.tokenRepository.saveRefreshToken(any()) } just runs

					When("OAuth2 로그인을 수행하면") {
						val result = fixture.service.signIn(testCode, testProvider)

						Then("액세스 토큰과 리프레시 토큰을 반환한다") {
							result.accessToken shouldBe "ACCESS-TOKEN-EXISTING"
							result.refreshToken shouldBe "REFRESH-TOKEN-EXISTING"
						}

						Then("OAuth2 Client로 인증을 수행한다") {
							verify(exactly = 1) { fixture.oauth2ClientFactory.getClient(testProvider) }
							verify(exactly = 1) { mockOAuth2Client.getUserInfo(testCode) }
						}

						Then("기존 Account를 조회하고 새로운 Account는 생성하지 않는다") {
							verify(exactly = 1) { fixture.accountRepository.findByIdOrNull(2L) }
							verify(exactly = 0) { fixture.accountRepository.save(any()) }
						}

						Then("JWT 토큰을 생성하고 RefreshToken을 저장한다") {
							verify(exactly = 1) { fixture.jwtProvider.generateAccessToken(2L, AccountRole.USER.name) }
							verify(exactly = 1) { fixture.jwtProvider.generateRefreshToken(2L, AccountRole.USER.name) }
							verify(exactly = 1) { fixture.tokenRepository.saveRefreshToken(any()) }
						}
					}
				}
			}

			Context("OAuth2 로그인 실패 - 계정을 찾을 수 없음") {
				Given("기존 AccountProvider는 존재하지만 Account가 삭제된 경우") {
					val fixture = createAuthServiceFixture()

					val testCode = "valid-oauth2-code"
					val testProvider = "GOOGLE"
					val testUserInfo =
						com.calendar.couple.auth.infrastructure.oauth2.OAuth2UserInfo(
							id = "google-user-deleted",
							email = "deleted@example.com",
							name = "Deleted User",
						)
					val mockOAuth2Client = mockk<com.calendar.couple.auth.infrastructure.oauth2.OAuth2Client>()

					// OAuth2 Client 모킹
					every { fixture.oauth2ClientFactory.getClient(testProvider) } returns mockOAuth2Client
					every { mockOAuth2Client.getUserInfo(testCode) } returns testUserInfo

					// 기존 AccountProvider 존재
					val existingAccountProvider =
						com.calendar.couple.account.infrastructure.persistence.entity.AccountProviderEntity.create(
							accountId = 999L,
							provider = testProvider,
							providerUserId = testUserInfo.id,
						)
					every {
						fixture.accountProviderRepository.findByProviderAndProviderUserId(testProvider, testUserInfo.id)
					} returns existingAccountProvider

					// Account를 찾을 수 없음
					every { fixture.accountRepository.findByIdOrNull(999L) } returns null

					When("OAuth2 로그인을 시도하면") {
						Then("AccountNotFoundException이 발생한다") {
							shouldThrow<com.calendar.couple.auth.exception.AuthException.AccountNotFoundException> {
								fixture.service.signIn(testCode, testProvider)
							}
						}
					}
				}
			}

			Context("OAuth2 로그인 실패 - Account 저장 후 ID가 null") {
				Given("Account 저장은 성공했지만 ID가 null인 경우") {
					val fixture = createAuthServiceFixture()

					val testCode = "valid-oauth2-code"
					val testProvider = "GOOGLE"
					val testUserInfo =
						com.calendar.couple.auth.infrastructure.oauth2.OAuth2UserInfo(
							id = "google-user-no-id",
							email = "noId@example.com",
							name = "No ID User",
						)
					val mockOAuth2Client = mockk<com.calendar.couple.auth.infrastructure.oauth2.OAuth2Client>()

					// OAuth2 Client 모킹
					every { fixture.oauth2ClientFactory.getClient(testProvider) } returns mockOAuth2Client
					every { mockOAuth2Client.getUserInfo(testCode) } returns testUserInfo

					// 기존 AccountProvider 없음
					every {
						fixture.accountProviderRepository.findByProviderAndProviderUserId(testProvider, testUserInfo.id)
					} returns null

					// Account 저장 시 ID가 null인 엔티티 반환
					val accountWithoutId =
						com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity.createUserAccount(
							testUserInfo.email,
							testUserInfo.name,
							testProvider,
						)
					every { fixture.accountRepository.save(any()) } returns accountWithoutId

					When("OAuth2 로그인을 시도하면") {
						Then("IllegalStateException이 발생한다") {
							shouldThrow<IllegalStateException> {
								fixture.service.signIn(testCode, testProvider)
							}
						}
					}
				}
			}

			Context("OAuth2 로그인 실패 - OAuth2 Provider 통신 실패") {
				Given("OAuth2 Provider에서 유저 정보를 가져오지 못한 경우") {
					val fixture = createAuthServiceFixture()

					val testCode = "invalid-code"
					val testProvider = "GOOGLE"
					val mockOAuth2Client = mockk<com.calendar.couple.auth.infrastructure.oauth2.OAuth2Client>()

					// OAuth2 Client 모킹
					every { fixture.oauth2ClientFactory.getClient(testProvider) } returns mockOAuth2Client
					every { mockOAuth2Client.getUserInfo(testCode) } throws RuntimeException("OAuth2 통신 실패")

					When("OAuth2 로그인을 시도하면") {
						Then("예외가 전파된다") {
							shouldThrow<RuntimeException> {
								fixture.service.signIn(testCode, testProvider)
							}
						}
					}
				}
			}

			Context("토큰 재발급 성공 플로우") {
				Given("유효한 리프레시 토큰이 주어졌을 때") {
					val fixture = createAuthServiceFixture()

					val testAccountId = 1L
					val testRole = AccountRole.ADMIN.name
					val testExpiration = Instant.now().plus(Duration.ofDays(7))

					val testAccount =
						com.calendar.couple.account.infrastructure.persistence.entity.AccountEntity(
							email = "test@example.com",
							name = "Test User",
							role = testRole,
							provider = "GOOGLE",
						)
					val idField = testAccount.javaClass.getDeclaredField("id")
					idField.isAccessible = true
					idField.set(testAccount, testAccountId)

					every { fixture.jwtProvider.validateRefreshToken("VALID-REFRESH") } just runs
					every { fixture.jwtProvider.getAccountIdFromToken("VALID-REFRESH") } returns testAccountId
					every { fixture.tokenRepository.getRefreshTokenByAccountId(testAccountId) } returns "VALID-REFRESH"
					every { fixture.jwtProvider.getRoleFromToken("VALID-REFRESH") } returns testRole
					every { fixture.jwtProvider.generateAccessToken(testAccountId, testRole) } returns "NEW-ACCESS"
					every { fixture.jwtProvider.generateRefreshToken(testAccountId, testRole) } returns
						RefreshTokenInfo("NEW-REFRESH", testExpiration)
					every { fixture.tokenRepository.saveRefreshToken(any()) } just runs

					When("토큰 재발급을 요청하면") {
						val token = fixture.service.renewToken("VALID-REFRESH")

						Then("새 액세스 토큰과 리프레시 토큰을 반환한다") {
							token.accessToken shouldBe "NEW-ACCESS"
							token.refreshToken shouldBe "NEW-REFRESH"
						}

						Then("필요한 메서드들이 정확히 호출된다") {
							verify(exactly = 1) { fixture.jwtProvider.validateRefreshToken("VALID-REFRESH") }
							verify(exactly = 1) { fixture.jwtProvider.getAccountIdFromToken("VALID-REFRESH") }
							verify(exactly = 1) { fixture.tokenRepository.getRefreshTokenByAccountId(testAccountId) }
							verify(exactly = 1) { fixture.jwtProvider.getRoleFromToken("VALID-REFRESH") }
							verify(exactly = 1) { fixture.jwtProvider.generateAccessToken(testAccountId, testRole) }
							verify(exactly = 1) { fixture.jwtProvider.generateRefreshToken(testAccountId, testRole) }
							verify(exactly = 1) { fixture.tokenRepository.saveRefreshToken(any()) }
						}
					}
				}
			}

			Context("토큰 재발급 실패") {
				Given("저장된 리프레시 토큰이 존재하지 않는 경우") {
					val fixture = createAuthServiceFixture()

					val testAccountId = 1L

					every { fixture.jwtProvider.validateRefreshToken("VALID-TOKEN") } just runs
					every { fixture.jwtProvider.getAccountIdFromToken("VALID-TOKEN") } returns testAccountId
					every { fixture.tokenRepository.getRefreshTokenByAccountId(testAccountId) } returns null

					When("토큰 재발급을 요청하면") {
						Then("ExpiredTokenException이 발생한다") {
							shouldThrow<JwtAuthException.ExpiredTokenException> {
								fixture.service.renewToken("VALID-TOKEN")
							}
						}
					}
				}

				Given("리프레시 토큰이 일치하지 않는 경우") {
					val fixture = createAuthServiceFixture()

					val testAccountId = 1L

					every { fixture.jwtProvider.validateRefreshToken("WRONG-TOKEN") } just runs
					every { fixture.jwtProvider.getAccountIdFromToken("WRONG-TOKEN") } returns testAccountId
					every { fixture.tokenRepository.getRefreshTokenByAccountId(testAccountId) } returns "STORED-TOKEN"
					every { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) } returns true

					When("토큰 재발급을 요청하면") {
						Then("InvalidTokenException이 발생한다") {
							shouldThrow<JwtAuthException.InvalidTokenException> {
								fixture.service.renewToken("WRONG-TOKEN")
							}
						}

						Then("저장된 토큰이 삭제된다") {
							verify(exactly = 1) { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) }
						}
					}
				}
			}

			Context("로그아웃 플로우") {
				Given("유효한 accessToken과 accountId가 주어졌을 때") {
					val fixture = createAuthServiceFixture()
					val testAccessToken = "valid-access-token"
					val testAccountId = 1L
					val testRemainingTtl = Duration.ofMinutes(30)

					every { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) } returns true
					every { fixture.jwtProvider.validateAccessToken(testAccessToken) } just runs
					every { fixture.jwtProvider.getRemainingTtl(testAccessToken) } returns testRemainingTtl
					every { fixture.tokenRepository.saveAccessTokenInBlacklist(testAccessToken, testRemainingTtl) } just runs

					When("로그아웃을 수행하면") {
						fixture.service.logout(testAccountId, testAccessToken)

						Then("리프레시 토큰이 삭제된다") {
							verify(exactly = 1) { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) }
						}

						Then("액세스 토큰이 검증된다") {
							verify(exactly = 1) { fixture.jwtProvider.validateAccessToken(testAccessToken) }
						}

						Then("액세스 토큰의 남은 TTL이 계산된다") {
							verify(exactly = 1) { fixture.jwtProvider.getRemainingTtl(testAccessToken) }
						}

						Then("액세스 토큰이 블랙리스트에 추가된다") {
							verify(exactly = 1) {
								fixture.tokenRepository.saveAccessTokenInBlacklist(testAccessToken, testRemainingTtl)
							}
						}
					}
				}

				Given("유효하지 않은 accessToken이 주어졌을 때") {
					val fixture = createAuthServiceFixture()
					val testAccessToken = "invalid-access-token"
					val testAccountId = 1L

					every { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) } returns true
					every { fixture.jwtProvider.validateAccessToken(testAccessToken) } throws
						JwtAuthException.InvalidTokenException()

					When("로그아웃을 시도하면") {
						Then("InvalidTokenException이 발생한다") {
							shouldThrow<JwtAuthException.InvalidTokenException> {
								fixture.service.logout(testAccountId, testAccessToken)
							}
						}

						Then("리프레시 토큰은 이미 삭제되어 있다") {
							verify(exactly = 1) { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) }
						}
					}
				}

				Given("만료된 accessToken이 주어졌을 때") {
					val fixture = createAuthServiceFixture()
					val testAccessToken = "expired-access-token"
					val testAccountId = 1L

					every { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) } returns true
					every { fixture.jwtProvider.validateAccessToken(testAccessToken) } throws
						JwtAuthException.ExpiredTokenException()

					When("로그아웃을 시도하면") {
						Then("ExpiredTokenException이 발생한다") {
							shouldThrow<JwtAuthException.ExpiredTokenException> {
								fixture.service.logout(testAccountId, testAccessToken)
							}
						}

						Then("리프레시 토큰은 이미 삭제되어 있다") {
							verify(exactly = 1) { fixture.tokenRepository.deleteRefreshTokenByAccountId(testAccountId) }
						}
					}
				}
			}
		}
	})