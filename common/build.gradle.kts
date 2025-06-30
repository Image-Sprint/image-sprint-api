plugins {
    kotlin("jvm") version "1.9.25"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // 필요 시 Jackson 등 공통 의존성 추가 가능
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<Test> {
    useJUnitPlatform()
}