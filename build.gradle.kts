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