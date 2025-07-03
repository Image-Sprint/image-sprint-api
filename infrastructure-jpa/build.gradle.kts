plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    // JPA 어노테이션에 자동으로 protected no-arg constructor가 생성
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }
}

group = "com"
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
    implementation(project(":core"))
    implementation(project(":common"))

    // reactive 관련
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Gradle Kotlin DSL (build.gradle.kts)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // or jjwt-gson

    // JPA, Redis, S3 등 인프라 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("software.amazon.awssdk:s3:2.25.14") // AWS SDK v2
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
//    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // DB 드라이버 예시
    runtimeOnly ("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
