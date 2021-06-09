plugins {
    java
    kotlin("jvm") version "1.4.21"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.15.0"
}

group = "io.github.danakuban.docs-gradle-plugin"
version = "1.0.0"

repositories {
    gradlePluginPortal()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("com.beust:klaxon:5.2")

    implementation("org.postgresql:postgresql:42.2.12")

    implementation("io.fabric8:kubernetes-client:5.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    implementation("org.apache.commons:commons-compress:1.20")
    implementation("commons-codec:commons-codec:1.15")
    implementation("net.lingala.zip4j:zip4j:2.6.4")

    implementation("net.sourceforge.plantuml:plantuml:1.2019.7")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("javax.persistence:javax.persistence-api:2.2")

    implementation("org.springframework:spring-context:5.3.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    implementation("net.mayope:deployment-plugin:0.0.40")
}

gradlePlugin {
    plugins {
        create("docsPlugin") {
            id = "io.github.danakuban.docs-gradle-plugin"
            implementationClass = "io.github.danakuban.docsgradleplugin.DocsPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/danakuban/docsGradlePlugin"
    vcsUrl = "https://github.com/danakuban/docsGradlePlugin"
    description = "Docs plugin to support the documentation of a project"

    (plugins) {
        "docsPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Gradle Docs plugin"
            tags = mutableListOf("docs", "documentation")
        }
    }
}
