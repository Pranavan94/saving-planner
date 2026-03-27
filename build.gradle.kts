plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"
}

group = "com.finance"
version = "0.0.1-SNAPSHOT"
description = "Personal savings planning and budgeting application"

val testcontainersVersion = "1.21.4"
val encoderVersion = "1.3.1"
val swaggerVersion="2.2.27"
val mockitoVersion="5.1.0"
val apchecommonsVersion="1.14.1"

extra["testcontainers.version"] = testcontainersVersion

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.owasp.encoder:encoder:$encoderVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.swagger.core.v3:swagger-annotations:$swaggerVersion")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-oauth2-jose")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
	implementation("org.apache.commons:commons-csv:$apchecommonsVersion")
	
	// Local PostgresSQL database for the application runtime
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.mockito")
	}
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
	testImplementation("org.testcontainers:postgresql")
	testImplementation(kotlin("test"))
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
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
