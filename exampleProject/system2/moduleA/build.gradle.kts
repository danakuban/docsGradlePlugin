plugins {
    kotlin("jvm")

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

group = "exampleProject.system2"
subprojects {
    group = "exampleProject.system2"
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

tasks.named("bootJar") {
    enabled = false
}
