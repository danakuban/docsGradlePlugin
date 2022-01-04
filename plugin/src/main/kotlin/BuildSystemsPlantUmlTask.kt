package io.github.danakuban.docsgradleplugin

import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

data class PumlSystemView(val system: String, val dependencies: String)

/**
 * Builds complete salesproduct system view from all separate system views.
 */
open class BuildSystemsPlantUmlTask : DefaultTask() {

    @Input
    var systemsPath: String = "${project.buildDir.absolutePath}/docs/system"

    init {
        inputs.dir(systemsPath)
        outputs.dir(project.buildDir.absolutePath + "/plantuml")
    }

    @TaskAction
    fun build() {
        println("building aggregated systems view")
        File(systemsPath).listFiles().filter {
            it.isDirectory
        }.map {
            File(it.absolutePath + "/system.puml")
        }.filter {
            it.exists()
        }.let {
            renderPumlsTogether(
                it, File(project.buildDir.absolutePath + "/plantuml/system.png"),
                File(project.buildDir.absolutePath + "/plantuml/system.puml")
            )
        }
    }
}

fun renderPumlsTogether(pumls: List<File>, outputFile: File, outputPumlFile: File) {
    val systemsViews = pumls.map {
        extractView(it)
    }
    val finalPuml = StringBuilder().run {
        appendln("@startuml")
        systemsViews.forEach {
            append(it.system)
        }
        appendln("'Dependencies")
        val dependencies = systemsViews.map { it.dependencies }
            .flatMap { it.split("\n") }
            .filterNot { it.isBlank() }
            .distinct()
            .joinToString("\n")
        append(dependencies)
        appendln()
        appendln("@enduml")
        toString()
    }

    outputPumlFile.writeText(finalPuml)
    val reader = SourceStringReader(finalPuml)

    reader.outputImage(outputFile)
}

private fun extractView(contentFile: File): PumlSystemView {
    removeHeaderAndFooter(contentFile.readText()).let {
        val parts = it.split("'Dependencies")
        return PumlSystemView(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
    }
}

private fun removeHeaderAndFooter(content: String) = content
    .replace("@startuml", "")
    .replace("@enduml", "")
