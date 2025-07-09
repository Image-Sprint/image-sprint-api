plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 내부 모듈
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":infrastructure-r2dbc"))

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Spring Reactive Stack
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Reactive Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Reactor 확장
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // S3
    implementation("software.amazon.awssdk:s3:2.31.77")

    // 이미지 처리
    implementation("net.coobird:thumbnailator:0.4.19")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage") // JUnit4 제거
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:1.13.8")
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
