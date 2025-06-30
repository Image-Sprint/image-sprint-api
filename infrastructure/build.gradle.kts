plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
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

    // JPA, Redis, S3 등 인프라 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("software.amazon.awssdk:s3:2.25.14") // AWS SDK v2
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // DB 드라이버 예시
    runtimeOnly("io.r2dbc:r2dbc-h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}