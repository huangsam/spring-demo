import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Spring Plugins
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"

    // Kotlin Plugins
    val kotlinVersion = "2.3.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion

    // Build Tool Plugins
    id("com.ncorti.ktfmt.gradle") version "0.26.0"
    jacoco
}

group = "com.huangsam"

version = "0.0.1-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Observability
    implementation("io.micrometer:micrometer-registry-prometheus")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    // Rate Limiting & External Libraries
    implementation("com.bucket4j:bucket4j-core:8.10.1")
    implementation("com.rometools:rome:2.1.0")
    implementation("org.commonmark:commonmark:0.27.1")

    // Kotlin Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Runtime
    runtimeOnly("com.h2database:h2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin { jvmToolchain(25) }

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        freeCompilerArgs.add("-Xexplicit-api=warning")
        freeCompilerArgs.add("-Werror")
    }
}

ktfmt { kotlinLangStyle() }

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Exec>("generateSeedData") {
    group = "application"
    description = "Generates random seed data for the application into seed-data.json"
    commandLine("./scripts/generate-seed-data.main.kts")
}
