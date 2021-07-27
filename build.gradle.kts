plugins {
    kotlin("jvm") version "1.3.50"

    // static code analysis
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    id("com.diffplug.spotless") version "5.12.5"
}

group = "io.github.danakuban"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}
allprojects {

    apply(plugin = "com.diffplug.spotless")
    spotless {
        val ktLintVersion = "0.39.0"
        kotlin {
            ktlint(ktLintVersion)
        }
        kotlinGradle {
            target("*.gradle.kts", "**/*.gradle.kts")

            ktlint(ktLintVersion)
        }
    }
}
