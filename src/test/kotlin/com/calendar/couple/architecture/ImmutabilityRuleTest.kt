package com.calendar.couple.architecture

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec

@OptIn(ExperimentalKotest::class)
class ImmutabilityRuleTest :
	BehaviorSpec({
		val classes =
			ClassFileImporter()
				.withImportOption(ImportOption.DoNotIncludeTests())
				.importPackages("com.calendar.couple")

		Context("DTO/Domain 클래스의 불변성 검증 목적 테스트") {
			Context("Domain 클래스 불변성 테스트") {
				Given("domain 패지지 하위의") {
					When("모든 Domain 클래스를 검증하면") {
						Then("모든 프로퍼티는 final(val)이어야 한다") {
							classes()
								.that()
								.resideInAPackage("..domain..")
								.and()
								.haveSimpleNameNotContaining("$")
								.and()
								.areNotAnonymousClasses()
								.and()
								.areNotEnums()
								.should(haveOnlyFinalFields())
								.check(classes)
						}
					}
				}
			}

			Context("DTO 불변성 테스트") {
				Given("dto 패키지 하위의") {
					When("모든 DTO 클래스를 검증하면") {
						Then("모든 프로퍼티는 final(val)이어야 한다") {
							classes()
								.that()
								.resideInAPackage("..dto..")
								.and()
								.haveSimpleNameNotContaining("$")
								.and()
								.areNotAnonymousClasses()
								.and()
								.areNotEnums()
								.should(haveOnlyFinalFields())
								.check(classes)
						}
					}
				}
			}
		}
	}) {
	companion object {
		/**
		 * 모든 프로퍼티가 final(val)인지 검증하는 ArchCondition
		 *
		 * Kotlin에서 val로 선언된 필드는 바이트코드 레벨에서 final modifier를 가짐
		 *
		 * 테스트 실패 시, 출력 로그 포함
		 */
		private fun haveOnlyFinalFields() =
			object : ArchCondition<JavaClass>("모든 프로퍼티가 불변 프로퍼티여야 함") {
				override fun check(
					item: JavaClass,
					events: ConditionEvents,
				) {
					item.allFields
						.filter { !it.modifiers.contains(JavaModifier.STATIC) } // static 필드 제외
						.forEach { field ->
							if (!field.modifiers.contains(JavaModifier.FINAL)) {
								events.add(
									SimpleConditionEvent.violated(
										field,
										"${field.fullName}가 불변이 아닙니다. ('val' in Kotlin)",
									),
								)
							}
						}
				}
			}
	}
}