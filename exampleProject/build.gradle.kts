plugins {
    kotlin("jvm") version "1.4.10"
    id("io.github.danakuban.docs-gradle-plugin") version "1.1.4" apply(false)
}

group = "exampleProject"

repositories {
    gradlePluginPortal()
}
