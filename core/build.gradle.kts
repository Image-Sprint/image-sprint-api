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
    implementation(project(":common"))

    implementation("org.springframework:spring-tx:6.1.4")
    implementation("org.springframework.boot:spring-boot-starter:3.5.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // 필요 시 core 레벨 유틸 등 추가
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // 테스트 런타임
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    // AssertJ (자바/Kotlin 모두 사용 가능)
    testImplementation("org.assertj:assertj-core:3.24.2")

    // MockK (Kotlin 전용 Mock 프레임워크)
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
