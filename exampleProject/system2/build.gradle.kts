plugins {
    kotlin("jvm")

    id("io.github.danakuban.docs-gradle-plugin")
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("plugin.spring") version "1.3.50"
}

tasks.named("bootJar") {
    enabled = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // docs gradle plugin
    implementation("io.github.danakuban.docs-gradle-plugin:plugin:1.0.0-SNAPSHOT")
}
