plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	kotlin("plugin.jpa")
	kotlin("plugin.allopen")
	kotlin("plugin.noarg")
	kotlin("kapt")

	id("org.springframework.boot")
	id("io.spring.dependency-management")
	id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
	id("jacoco")
}

val applicationGroup: String by project
val applicationVersion: String by project

group = applicationGroup
version = applicationVersion
description = "Calendar Couple Project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

	// Logging
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")

	// Spring Boot Docker Compose Support
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Test Container
	testImplementation("org.testcontainers:testcontainers:1.21.3")
	testImplementation("org.testcontainers:junit-jupiter:1.21.3")

	// Kotest
	testImplementation("io.kotest:kotest-runner-junit5:6.0.4")
	testImplementation("io.kotest:kotest-assertions-core:6.0.4")
	testImplementation("io.kotest:kotest-extensions-spring:6.0.4")
	testImplementation("io.kotest:kotest-extensions-testcontainers:6.0.4")

	// MockK
	testImplementation("io.mockk:mockk:1.13.12")
	testImplementation("com.ninja-squad:springmockk:4.0.2")

	// ArchUnit
	testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("test") {
	finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
	// jacoco 실행 데이터 파일 경로 지정
	executionData.setFrom(fileTree(project.layout.buildDirectory).include("jacoco/test.exec"))

	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}

	finalizedBy(tasks.named("jacocoTestCoverageVerification"))
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
	// jacoco 실행 데이터 파일 경로 지정
	executionData.setFrom(fileTree(project.layout.buildDirectory).include("jacoco/test.exec"))

	violationRules {
		rule {
			limit {
				minimum = "0.70".toBigDecimal() // 커버리지 70%
			}
		}
	}
}

// 단위테스트만 실행하는 태스크 (통합테스트 제외)
tasks.register<Test>("unitTest") {
	description = "Run unit tests only (exclude integration tests)"
	group = "verification"
	useJUnitPlatform()

	// integration 패키지 제외
	exclude("**/integration/**")
}

// unitTest 전용 jacoco 리포트 태스크
tasks.register<JacocoReport>("jacocoUnitTestReport") {
	description = "Generate Jacoco coverage report for unit tests"
	group = "verification"

	executionData(files("${project.layout.buildDirectory.get()}/jacoco/unitTest.exec"))
	sourceSets(sourceSets.main.get())

	// 커버리지 측정 대상에서 제외
	classDirectories.setFrom(
		files(sourceSets.main.get().output).asFileTree.matching {
			exclude(
				"**/*Controller*",
				"**/config/**",
				"**/exception/**",
				"**/properties/**",
				"**/infrastructure/persistence/**",
				"**/infrastructure/oauth2/**",
				"**/common/dto/**",
				"**/common/BaseTimeEntity*",
			)
		},
	)

	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
		xml.outputLocation.set(file("${project.layout.buildDirectory.get()}/reports/jacoco/unitTest/jacocoTestReport.xml"))
		html.outputLocation.set(file("${project.layout.buildDirectory.get()}/reports/jacoco/unitTest/html"))
	}

	dependsOn(tasks.named("unitTest"))
}

// unitTest 전용 커버리지 검증 태스크
tasks.register<JacocoCoverageVerification>("jacocoUnitTestCoverageVerification") {
	description = "Verify unit test coverage meets minimum threshold"
	group = "verification"

	executionData(files("${project.layout.buildDirectory.get()}/jacoco/unitTest.exec"))
	sourceSets(sourceSets.main.get())

	// 커버리지 측정 대상에서 제외
	classDirectories.setFrom(
		files(sourceSets.main.get().output).asFileTree.matching {
			exclude(
				"**/*Controller*",
				"**/config/**",
				"**/exception/**",
				"**/properties/**",
				"**/infrastructure/persistence/**",
				"**/infrastructure/oauth2/**",
				"**/common/dto/**",
				"**/common/BaseTimeEntity*",
			)
		},
	)

	violationRules {
		rule {
			limit {
				minimum = "0.70".toBigDecimal()
			}
		}
	}

	// 리포트 파일이 필요하므로 의존성 추가
	dependsOn(tasks.named("jacocoUnitTestReport"))
}

// unitTest 실행 시 리포트와 검증 자동 실행
tasks.named("unitTest") {
	finalizedBy(tasks.named("jacocoUnitTestReport"))
}

tasks.named("jacocoUnitTestReport") {
	finalizedBy(tasks.named("jacocoUnitTestCoverageVerification"))
}