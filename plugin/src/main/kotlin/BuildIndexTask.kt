package io.github.danakuban.docsgradleplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


/**
 * Task to aggregate the documentation of a system
 */
open class BuildIndexTask : DefaultTask() {

    @Input
    var systemsPath: String = project.buildDir.absolutePath + "/docs/system"

    init {
        outputs.file(project.buildDir.absolutePath + "/docs/_index.md")
        outputs.file(project.buildDir.absolutePath + "/docs/menu.md")
    }

    fun systemName(systemDir: File): String =
        File("${systemDir.absolutePath}/name").readText(Charsets.UTF_8)

    fun menu(systemDir: File): String? =
        File("${systemDir.absolutePath}/menu").let {
            if (it.exists()) {
                it.readText(Charsets.UTF_8)
            } else {
                null
            }
        }

    private fun systemDescription(systemDir: File): String =
        File("${systemDir.absolutePath}/description.md").readText(Charsets.UTF_8)

    private fun buildSystemIndex() =
        File("$systemsPath").listFiles().filter {
            it.isDirectory
        }.joinToString("\n") {
            "- [${systemName(it)}](${it.name}): ${systemDescription(it)}"
        }

    private fun buildSystemIndexForMenu() =
        File("$systemsPath").listFiles().filter {
            it.isDirectory
        }.mapNotNull {
            menu(it)
        }.joinToString("")


    @TaskAction
    fun build() {
        println(project.buildDir.absolutePath + "/docs/_index.md")
        File(project.buildDir.absolutePath + "/docs/_index.md").writeText(buildSystemIndex())
        File(project.buildDir.absolutePath + "/docs/menu.md").writeText(buildSystemIndexForMenu())
    }
}


