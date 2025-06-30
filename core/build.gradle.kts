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
	// 필요 시 core 레벨 유틸 등 추가
	testImplementation("org.jetbrains.kotlin:kotlin-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<Test> {
	useJUnitPlatform()
}