package com.calendar.couple.auth.api.dto.unit

import com.calendar.couple.auth.api.dto.SignInRequest
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import jakarta.validation.Validation

@OptIn(ExperimentalKotest::class)
class SignInRequestValidationUnitTest :
	BehaviorSpec({
		val validator = Validation.buildDefaultValidatorFactory().validator

		Context("SignInRequest Validation 검증 목적 단위 테스트") {
			Context("유효한 요청 검증") {
				Given("모든 필드가 유효한 값으로 주어졌을 때") {
					val dto =
						SignInRequest(
							code = "user@example.com",
							provider = "password123",
						)

					When("검증을 수행하면") {
						val violations = validator.validate(dto)

						Then("검증 에러가 없다") {
							violations.shouldBeEmpty()
						}
					}
				}
			}
		}
	})