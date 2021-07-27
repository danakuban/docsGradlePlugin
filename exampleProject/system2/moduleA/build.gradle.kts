plugins {
    kotlin("jvm")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
}

tasks.named("build") {
    dependsOn(gradle.includedBuild("plugin").task(":build"))
}
