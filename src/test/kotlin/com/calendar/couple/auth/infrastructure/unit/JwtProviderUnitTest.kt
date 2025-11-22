package com.calendar.couple.auth.infrastructure.unit

import com.calendar.couple.account.domain.enums.AccountRole
import com.calendar.couple.auth.exception.JwtAuthException
import com.calendar.couple.auth.infrastructure.JwtProvider
import com.calendar.couple.common.properties.JwtProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.jvm.java
import kotlin.text.split
import kotlin.text.toByteArray

@OptIn(ExperimentalKotest::class)
class JwtProviderUnitTest :
	BehaviorSpec({
		val jwtProperties =
			JwtProperties(
				secret =
					Base64.getEncoder().encodeToString(
						"test-secret-key-for-jwt-provider-unit-test-minimum-256-bits".toByteArray(),
					),
				issuer = "whoflex-test",
				audience = "whoflex-api-test",
				accessTokenExpiration = Duration.ofMinutes(30),
				refreshTokenExpiration = Duration.ofDays(7),
			)

		val fixedClock =
			Clock.fixed(
				Instant.parse("2025-10-26T00:00:00Z"),
				ZoneId.of("UTC"),
			)

		val jwtProvider = JwtProvider(jwtProperties, fixedClock)

		Context("JwtProvider의 AccessToken, RefreshToken 생성, 검증, 필드 추출 및 만료 처리 동작 검증 목적 단위 테스트") {
			Given("JWT Provider가 주어졌을 때") {
				When("Access Token을 생성하면") {
					val accountId = 1L
					val role = "ROLE_USER"

					val token = jwtProvider.generateAccessToken(accountId, role)

					Then("유효한 JWT 형식의 토큰이 생성된다") {
						token.shouldNotBeBlank()
						token.split(".").size shouldBe 3 // header.payload.signature
					}
				}

				When("Refresh Token을 생성하면") {
					val accountId = 1L
					val refreshTokenInfo = jwtProvider.generateRefreshToken(accountId, AccountRole.USER.name)

					Then("유효한 JWT 형식의 토큰이 생성된다") {
						refreshTokenInfo.token.shouldNotBeBlank()
						refreshTokenInfo.token.split(".").size shouldBe 3
					}

					Then("만료 시간이 현재 시각 + 7일로 설정된다") {
						val expectedExpiration = Instant.parse("2025-10-26T00:00:00Z").plus(Duration.ofDays(7))
						refreshTokenInfo.expirationTime shouldBe expectedExpiration
					}
				}
			}

			Given("유효한 Access Token이 주어졌을 때") {
				val token = jwtProvider.generateAccessToken(1L, "ROLE_USER")

				Context("토큰 유효성 검증") {
					When("토큰을 검증하면") {
						Then("예외가 발생하지 않는다") {
							jwtProvider.validateAccessToken(token)
						}
					}
				}

				Context("토큰에서 추출한 값에 대한 검증") {
					When("accountId를 추출하면") {
						val accountId = 1L
						val tokenWithId = jwtProvider.generateAccessToken(accountId, "ROLE_USER")

						val extractedAccountId = jwtProvider.getAccountIdFromToken(tokenWithId)

						Then("올바른 accountId가 반환된다") {
							extractedAccountId shouldBe accountId
						}
					}
				}
			}

			Given("유효한 Refresh Token이 주어졌을 때") {
				val accountId = 1L
				val refreshTokenInfo = jwtProvider.generateRefreshToken(accountId, AccountRole.USER.name)

				Context("토큰 유효성 검증") {
					When("토큰을 검증하면") {
						Then("예외가 발생하지 않는다") {
							jwtProvider.validateRefreshToken(refreshTokenInfo.token)
						}
					}
				}

				Context("토큰에서 추출한 값에 대한 검증") {
					When("accountId를 추출하면") {
						val extractedAccountId = jwtProvider.getAccountIdFromToken(refreshTokenInfo.token)

						Then("올바른 accountId가 반환된다") {
							extractedAccountId shouldBe accountId
						}
					}
				}
			}

			Given("Access Token 전체 플로우") {
				val accountId = 1L
				val role = "ROLE_INSTRUCTOR"

				When("토큰을 생성하고") {
					val token = jwtProvider.generateAccessToken(accountId, role)

					And("검증을 수행하고") {
						jwtProvider.validateAccessToken(token)

						And("필드를 추출하면") {
							val extractedAccountId = jwtProvider.getAccountIdFromToken(token)

							Then("모든 값이 정확하게 일치한다") {
								extractedAccountId shouldBe accountId
							}
						}
					}
				}
			}

			Given("Refresh Token 전체 플로우") {
				val accountId = 1L

				When("토큰을 생성하고") {
					val refreshTokenInfo = jwtProvider.generateRefreshToken(accountId, AccountRole.USER.name)

					And("검증을 수행하고") {
						jwtProvider.validateRefreshToken(refreshTokenInfo.token)

						And("accountId를 추출하면") {
							val extractedAccountId = jwtProvider.getAccountIdFromToken(refreshTokenInfo.token)

							Then("올바른 accountId가 반환된다") {
								extractedAccountId shouldBe accountId
							}
						}
					}
				}
			}

			Given("서로 다른 accountId가 주어졌을 때") {
				val accountId1 = 1L
				val accountId2 = 2L

				When("각각 토큰을 생성하면") {
					val token1 = jwtProvider.generateAccessToken(accountId1, "ROLE_USER")
					val token2 = jwtProvider.generateAccessToken(accountId2, "ROLE_USER")

					Then("서로 다른 토큰이 생성된다") {
						token1 shouldNotBe token2
					}

					Then("각 토큰에서 올바른 accountId를 추출할 수 있다") {
						jwtProvider.getAccountIdFromToken(token1) shouldBe accountId1
						jwtProvider.getAccountIdFromToken(token2) shouldBe accountId2
					}
				}
			}

			Given("만료된 Access Token이 주어졌을 때") {
				val accountId = 1L
				val role = "ROLE_USER"

				val token = jwtProvider.generateAccessToken(accountId, role)

				When("만료 시간(30분) 이후에 검증하면") {
					val expiredClock =
						Clock.fixed(
							Instant.parse("2025-10-26T00:31:00Z"), // 31분 후
							ZoneId.of("UTC"),
						)
					val expiredJwtProvider = JwtProvider(jwtProperties, expiredClock)

					Then("ExpiredTokenException이 발생한다") {
						shouldThrow<JwtAuthException.ExpiredTokenException> {
							expiredJwtProvider.validateAccessToken(token)
						}
					}
				}
			}

			Given("만료된 Refresh Token이 주어졌을 때") {
				val accountId = 1L
				val refreshTokenInfo = jwtProvider.generateRefreshToken(accountId, AccountRole.USER.name)

				When("만료 시간(7일) 이후에 검증하면") {
					val expiredClock =
						Clock.fixed(
							Instant.parse("2025-11-02T00:00:01Z"), // 7일 + 1초 후
							ZoneId.of("UTC"),
						)
					val expiredJwtProvider = JwtProvider(jwtProperties, expiredClock)

					Then("ExpiredTokenException이 발생한다") {
						shouldThrow<JwtAuthException.ExpiredTokenException> {
							expiredJwtProvider.validateRefreshToken(refreshTokenInfo.token)
						}
					}
				}
			}

			Given("RefreshTokenInfo를 생성했을 때") {
				val accountId = 1L

				When("generateRefreshToken을 호출하면") {
					val refreshTokenInfo = jwtProvider.generateRefreshToken(accountId, AccountRole.USER.name)

					Then("토큰과 만료 시간이 함께 반환된다") {
						refreshTokenInfo.token.shouldNotBeBlank()
						refreshTokenInfo.expirationTime shouldBe Instant.parse("2025-11-02T00:00:00Z")
					}

					Then("만료 시간은 Instant 타입이다") {
						refreshTokenInfo.expirationTime shouldBe
							Instant::class.java.cast(refreshTokenInfo.expirationTime)
					}
				}
			}

			Given("유효한 Access Token이 주어졌을 때") {
				val accountId = 1L
				val role = "ROLE_USER"
				val token = jwtProvider.generateAccessToken(accountId, role)

				Context("남은 TTL 계산") {
					When("getRemainingTtl을 호출하면") {
						val remainingTtl = jwtProvider.getRemainingTtl(token)

						Then("남은 유효시간이 30분으로 반환된다") {
							remainingTtl shouldBe Duration.ofMinutes(30)
						}
					}
				}
			}

			Given("만료된 Access Token이 주어졌을 때") {
				val accountId = 1L
				val role = "ROLE_USER"
				val token = jwtProvider.generateAccessToken(accountId, role)

				Context("남은 TTL 계산") {
					When("만료 이후에 getRemainingTtl을 호출하면") {
						val expiredClock =
							Clock.fixed(
								Instant.parse("2025-10-26T00:31:00Z"), // 31분 후
								ZoneId.of("UTC"),
							)
						val expiredJwtProvider = JwtProvider(jwtProperties, expiredClock)

						Then("ExpiredTokenException이 발생한다") {
							shouldThrow<JwtAuthException.ExpiredTokenException> {
								expiredJwtProvider.getRemainingTtl(token)
							}
						}
					}
				}
			}
		}
	})