plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.imagesprint"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Core와 Common 모듈에 대한 의존성
    implementation(project(":core"))
    implementation(project(":common"))

    // Spring R2DBC 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // MySQL R2DBC 드라이버 (DB에 맞춰 변경 가능)
    runtimeOnly("dev.miku:r2dbc-mysql:0.8.2.RELEASE")

    // Jackson for JSON serialization/deserialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    // Kotlin reflection (선택)
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    runtimeOnly("io.r2dbc:r2dbc-h2")

    // 테스트 관련
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage") // JUnit4 제외
    }
    testImplementation("io.projectreactor:reactor-test")
}
