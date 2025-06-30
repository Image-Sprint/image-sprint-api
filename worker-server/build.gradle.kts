plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 내부 모듈
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":infrastructure"))

    // 기본 Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // 만약 직접 구현이 필요하다면 AWS SDK v2
//    implementation("software.amazon.awssdk:s3:2.25.14")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
