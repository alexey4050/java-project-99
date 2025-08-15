plugins {
	application
	java
	checkstyle
	jacoco
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.freefair.lombok") version "8.6"
	id("org.sonarqube") version "6.2.0.5505"
	id("io.sentry.jvm.gradle") version "5.9.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application {
	mainClass.set("hexlet.code.AppApplication")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
	testImplementation("org.instancio:instancio-junit:3.3.0")

	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required = true
		csv.required.set(false)
		html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}

sonar {
	properties {
		property("sonar.projectKey", "alexey4050_java-project-99")
		property("sonar.organization", "alexey4050")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.ce.timeout", "600000")
		property("sonar.ws.timeout", "600000")
	}
}

sentry {
	includeSourceContext.set(true)
	org = "alexeycom"
	projectName = "java-project-99"
	includeDependenciesReport.set(true)
	authToken = System.getenv("SENTRY_AUTH_TOKEN") ?: ""
	//authToken = providers.environmentVariable("SENTRY_AUTH_TOKEN").getOrElse("")

	telemetry = false

	tracingInstrumentation {
		enabled = true
	}
}