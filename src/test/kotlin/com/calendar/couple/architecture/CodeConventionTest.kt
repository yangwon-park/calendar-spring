package com.calendar.couple.architecture

import com.calendar.couple.common.dto.CommonErrorResponse
import com.calendar.couple.common.dto.CommonResponse
import com.calendar.couple.common.dto.CommonStatusResponse
import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaAnnotation
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import io.kotest.core.spec.style.BehaviorSpec
import jakarta.persistence.Entity
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

class CodeConventionTest :
	BehaviorSpec({
		val classes =
			ClassFileImporter()
				.importPackages("com.calendar.couple")

		val handlerMethodAnnotations =
			DescribedPredicate.describe<JavaAnnotation<*>>(
				"Spring MVC Handler Method Annotations",
			) { annotation ->
				annotation.rawType.isAssignableFrom(GetMapping::class.java) ||
					annotation.rawType.isAssignableFrom(PostMapping::class.java) ||
					annotation.rawType.isAssignableFrom(PutMapping::class.java) ||
					annotation.rawType.isAssignableFrom(PatchMapping::class.java) ||
					annotation.rawType.isAssignableFrom(DeleteMapping::class.java) ||
					annotation.rawType.isAssignableFrom(RequestMapping::class.java)
			}

		Context("네이밍 컨벤션, 패키지 위치 등 전반적인 코드 컨벤션 검증 목적 테스트") {
			Given("Controller Naming Convention 테스트") {
				When("@RestController 어노테이션이 붙은 클래스는") {
					Then("Controller로 끝나야 한다") {
						classes()
							.that()
							.areAnnotatedWith(RestController::class.java)
							.should()
							.haveSimpleNameEndingWith("Controller")
							.check(classes)
					}
				}

				When("Controller 클래스의 패키지 위치는") {
					Then("api 패키지여야 한다") {
						classes()
							.that()
							.areAnnotatedWith(RestController::class.java)
							.should()
							.resideInAPackage("..api..")
							.check(classes)
					}
				}

				When("Handler Method 어노테이션이 붙은 메소드들은") {
					Then("@RestController 클래스 내에 선언되어야 한다") {
						methods()
							.that()
							.areAnnotatedWith(handlerMethodAnnotations)
							.should()
							.beDeclaredInClassesThat()
							.areAnnotatedWith(RestController::class.java)
							.check(classes)
					}
				}

				When("Controller Handler Method의 반환 타입은") {
					Then("CommonResponse, CommonErrorResponse, CommonStatusResponse 중 하나여야 한다") {
						methods()
							.that()
							.areDeclaredInClassesThat()
							.areAnnotatedWith(RestController::class.java)
							.and()
							.arePublic()
							.and()
							.areAnnotatedWith(handlerMethodAnnotations)
							.should()
							.haveRawReturnType(CommonResponse::class.java)
							.orShould()
							.haveRawReturnType(CommonErrorResponse::class.java)
							.orShould()
							.haveRawReturnType(CommonStatusResponse::class.java)
							.check(classes)
					}
				}
			}

			Given("Service Naming Convention 테스트") {
				When("@Service 어노테이션이 붙은 클래스는") {
					Then("Service로 끝나야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Service::class.java)
							.should()
							.haveSimpleNameEndingWith("Service")
							.check(classes)
					}
				}

				When("Service 클래스의 패키지 위치는") {
					Then("application, service 또는 security 패키지여야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Service::class.java)
							.should()
							.resideInAnyPackage("..application.service..", "..security..")
							.check(classes)
					}
				}

				When("Service 클래스의 타입은") {
					Then("인터페이스가 아니어야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Service::class.java)
							.should()
							.notBeInterfaces()
							.check(classes)
					}
				}
			}

			Given("Repository Naming Convention 테스트") {
				When("@Repository 어노테이션이 붙은 클래스/인터페이스는") {
					Then("Repository로 끝나야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Repository::class.java)
							.should()
							.haveSimpleNameEndingWith("Repository")
							.check(classes)
					}
				}

				When("JpaRepository를 상속하는 인터페이스의 이름은") {
					Then("Repository로 끝나야 한다") {
						classes()
							.that()
							.areAssignableTo(JpaRepository::class.java)
							.should()
							.haveSimpleNameEndingWith("Repository")
							.check(classes)
					}
				}

				When("Repository의 패키지 위치는") {
					Then("repository 패키지여야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Repository::class.java)
							.or()
							.areAssignableTo(JpaRepository::class.java)
							.should()
							.resideInAPackage("..repository..")
							.check(classes)
					}
				}
			}

			Given("Entity Naming Convention 테스트") {
				When("@Entity 어노테이션이 붙은 클래스는") {
					Then("클래스 이름은 Entity로 끝나야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Entity::class.java)
							.should()
							.haveSimpleNameEndingWith("Entity")
							.check(classes)
					}
				}

				When("Entity 클래스의 패키지 위치는") {
					Then("infrastructure.persistence.entity 패키지에 위치해야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Entity::class.java)
							.should()
							.resideInAPackage("..infrastructure.persistence.entity..")
							.check(classes)
					}
				}
			}

			Given("Dto Naming Convention 테스트") {
				When("Dto로 끝나는 클래스의 패키지 위치는") {
					Then("dto 패키지여야 한다") {
						classes()
							.that()
							.haveSimpleNameEndingWith("Dto")
							.should()
							.resideInAPackage("..dto..")
							.check(classes)
					}
				}
			}

			Given("Exception Naming Convention 테스트") {
				When("Exception으로 끝나는 클래스는") {
					Then("Exception을 상속해야 한다") {
						classes()
							.that()
							.haveSimpleNameEndingWith("Exception")
							.should()
							.beAssignableTo(Exception::class.java)
							.check(classes)
					}
				}

				When("Exception으로 끝나는 클래스의 패키지 위치는") {
					Then("exception 패키지여야 한다") {
						classes()
							.that()
							.haveSimpleNameEndingWith("Exception")
							.should()
							.resideInAPackage("..exception..")
							.check(classes)
					}
				}
			}

			Given("Configuration Naming Convention 테스트") {
				When("@Configuration 어노테이션이 붙은 클래스는") {
					Then("Config로 끝나야 한다") {
						classes()
							.that()
							.areAnnotatedWith(Configuration::class.java)
							.should()
							.haveSimpleNameEndingWith("Config")
							.check(classes)
					}
				}

				When("Config로 끝나는 클래스의 패키지 위치는") {
					Then("config 패키지여야 한다") {
						classes()
							.that()
							.haveSimpleNameEndingWith("Config")
							.should()
							.resideInAPackage("..config..")
							.check(classes)
					}
				}
			}

			Given("Properties Naming Convention 테스트") {
				When("Properties로 끝나는 클래스의 패키지 위치는") {
					Then("properties 패키지여야 한다") {
						classes()
							.that()
							.haveSimpleNameEndingWith("Properties")
							.should()
							.resideInAPackage("..properties..")
							.check(classes)
					}
				}
			}

// 			Given("Test Naming Convention 테스트") {
// 				When("UnitTest로 끝나는 클래스는") {
// 					Then("unit 패키지에 위치해야 한다") {
// 						classes()
// 							.that()
// 							.haveSimpleNameEndingWith("UnitTest")
// 							.should()
// 							.resideInAPackage("..unit..")
// 							.check(classes)
// 					}
// 				}
//
// 				When("IntegrationTest로 끝나는 클래스는") {
// 					Then("integration 패키지에 위치해야 한다") {
// 						classes()
// 							.that()
// 							.haveSimpleNameEndingWith("IntegrationTest")
// 							.should()
// 							.resideInAPackage("..integration..")
// 							.check(classes)
// 					}
// 				}
// 			}
		}
	})