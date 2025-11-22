package com.calendar.couple.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

class LayeredArchitectureTest :
	BehaviorSpec({
		val classes =
			ClassFileImporter()
				.withImportOption(ImportOption.DoNotIncludeTests())
				.importPackages("com.calendar.couple")

		Context("계층 구조 설계 규칙(Layered Architecture Rule) 검증 목적 테스트") {
			Given("Layered Architecture 테스트") {
				When("계층 간 의존성 규칙은") {
					Then("정의된 계층 구조를 따라야 한다") {
						layeredArchitecture()
							.consideringAllDependencies()
							.layer("API")
							.definedBy("..api..")
							.layer("Application")
							.definedBy("..application..")
							.layer("Domain")
							.definedBy("..domain..")
							.layer("Infrastructure")
							.definedBy("..infrastructure..")
							.layer("Security")
							.definedBy("..security..")
							.layer("Common")
							.definedBy("..common..")
							.whereLayer("API") // API 레이어는 DTO를 포함 -> 현재 DTO는 Application, Security, Common 등 많은 곳에서 쓰임 -> 필요 시점에 고도화
							.mayOnlyBeAccessedByLayers("Application", "Security", "Common")
							.whereLayer("Application")
							.mayOnlyBeAccessedByLayers("API", "Security")
							.whereLayer("Domain") // Domain 레이어는 infrastructure 레이어에 있는 ObjectMapper를 의존하고 있음 -> 필요 시점에 고도화
							.mayOnlyBeAccessedByLayers("API", "Application", "Infrastructure", "Security", "Common")
							.whereLayer("Infrastructure")
							.mayOnlyBeAccessedByLayers("API", "Application", "Security", "Common")
							.whereLayer("Security")
							.mayOnlyBeAccessedByLayers("API", "Application", "Common")
							.check(classes)
					}
				}

				// 도메인이 Controller에 접근할 경우가 오면 그때 완화시킬 예정
				When("Controller는") {
					Then("다른 레이어에서 접근할 수 없다") {
						noClasses()
							.that()
							.resideOutsideOfPackage("..api..")
							.should()
							.dependOnClassesThat()
							.areAnnotatedWith(RestController::class.java)
							.check(classes)
					}
				}

				When("Controller 계층의 의존성은") {
					Then("Repository에 의존하면 안 된다)") {
						noClasses()
							.that()
							.areAnnotatedWith(RestController::class.java)
							.should()
							.dependOnClassesThat()
							.areAnnotatedWith(Repository::class.java)
							.check(classes)
					}
				}

				When("Service 계층의 의존성은") {
					// Service 간의 의존이 가능한가에 대한 논의 필요
					Then("Controller에 의존하면 안 된다") {
						noClasses()
							.that()
							.areAnnotatedWith(Service::class.java)
							.should()
							.dependOnClassesThat()
							.areAnnotatedWith(RestController::class.java)
							.check(classes)
					}
				}

				When("Repository 계층의 의존성은") {
					Then("Service나 Controller에 의존하면 안 된다") {
						noClasses()
							.that()
							.areAnnotatedWith(Repository::class.java)
							.should()
							.dependOnClassesThat()
							.areAnnotatedWith(Service::class.java)
							.orShould()
							.dependOnClassesThat()
							.areAnnotatedWith(RestController::class.java)
							.check(classes)
					}
				}

				// 추후 Controller 의존 X -> API Layer 의존 X로 고도화 (현재: DTO 전달 가능)
				When("Infrastructure 계층은") {
					Then("Controller에 의존하면 안 된다") {
						noClasses()
							.that()
							.resideInAPackage("..infrastructure..")
							.should()
							.dependOnClassesThat()
							.areAnnotatedWith(RestController::class.java)
							.check(classes)
					}
				}

				When("Domain 계층은") {
					Then("Spring Framework나 Persistence 어노테이션에 의존하면 안 된다") {
						noClasses()
							.that()
							.resideInAPackage("..domain..")
							.should()
							.dependOnClassesThat()
							.resideInAnyPackage(
								"org.springframework..",
								"jakarta.persistence..",
							).check(classes)
					}
				}
			}
		}
	})