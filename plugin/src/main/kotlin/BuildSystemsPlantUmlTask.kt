package io.github.danakuban.docsgradleplugin

import java.io.File
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

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
            renderPumlsTogether(it, File(project.buildDir.absolutePath + "/plantuml/system.png"))
        }
    }
}

fun renderPumlsTogether(pumls: List<File>, outputFile: File) {
    val systemsViews = pumls.map {
        extractView(it)
    }
    val finalPuml = StringBuilder().run {
        appendln("@startuml")
        systemsViews.forEach {
            append(it.system)
        }
        systemsViews.forEach {
            append(it.dependencies)
        }
        appendln("@enduml")
        toString()
    }
    val reader = SourceStringReader(finalPuml)

    reader.outputImage(outputFile)
}

private fun extractView(contentFile: File): PumlSystemView {
    contentFile.readText().let {
        removeHeaderAndFooter(it)
    }
        .let {
            if ("'Dependencies" !in it) {
                error("No section \"'Dependencies\" found in ${contentFile.absolutePath}")
            }
            val (system, dependencies) = it.split("'Dependencies")
            return PumlSystemView(system, dependencies)
        }


}

private fun removeHeaderAndFooter(content: String) = content
    .replace("@startuml", "")
    .replace("@enduml", "")
