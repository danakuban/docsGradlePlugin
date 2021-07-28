plugins {
    kotlin("jvm")

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
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

tasks.named("build") {
    dependsOn(gradle.includedBuild("plugin").task(":build"))
}
