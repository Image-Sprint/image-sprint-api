plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3" apply false // JAR 생성 X
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

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common"))

    // R2DBC Core
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // R2DBC MySQL 드라이버
    runtimeOnly("io.asyncer:r2dbc-mysql:1.0.3")

    // Kotlin Coroutine + Reactor 연동
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")

    // JSON 처리
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage") // JUnit4 제외
    }
    testImplementation("io.projectreactor:reactor-test")
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
