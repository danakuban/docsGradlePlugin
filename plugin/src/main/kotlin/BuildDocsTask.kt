package io.github.danakuban.docsgradleplugin

import io.github.danakuban.docsgradleplugin.render.DocsTemplateRenderer
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


/**
 * Task to aggregate the documentation of a system
 */
open class BuildDocsTask : DefaultTask() {

    @Input
    var systemDescriptionPath: String = "description.md"

    init {
        checkForNeededDoc()

        if (File(project.projectDir.absolutePath + "/docs").exists()) {
            inputs.dir(project.projectDir.absolutePath + "/docs")
        }
        if (File(project.projectDir.absolutePath + "/docs/system.puml").exists()) {
            inputs.file(project.projectDir.absolutePath + "/docs/system.puml")
        }
        project.subprojects {
            if (File(it.buildDir.absolutePath + "/docs/jobs/").exists()) {
                inputs.dir(File(it.buildDir.absolutePath + "/docs/jobs/"))
            }
        }
        outputs.dir(project.buildDir.absolutePath + "/docs")
        outputs.dir(project.rootProject.buildDir.absolutePath + "/docs/system/${project.name.toLowerCase()}")
    }

    private fun checkForNeededDoc() {
        if (!File(project.projectDir.absolutePath + "/" + systemDescriptionPath).exists()) {
            error("System Description file: $systemDescriptionPath not found.")
        }
        inputs.file(project.projectDir.absolutePath + "/" + systemDescriptionPath)
    }

    @TaskAction
    fun build() {
        project.name.capitalize().let {
            DocsTemplateRenderer(project, systemDescriptionPath).renderTemplate(it)
            renderPumls()
            renderMenu(it)

            copyToDest(it)
        }
    }

    private fun renderMenu(name: String) {
        if (project.parent?.let { hasDocsPlugin(it) } == true) {
            logger.info("Parent project has already rendered menu")
            return
        }
        val menu = StringBuilder()
        menu.appendLine("    - [${name}](/systems/${name.toLowerCase()})")
        project.subprojects
            .filter { hasDocsPlugin(it) }
            .forEach {
                menu.appendLine(
                    "      - [${it.name.capitalize()}](/systems/${it.name.toLowerCase()})"
                )
            }
        File(project.buildDir.absolutePath + "/docs/menu").writeText(menu.toString())
    }

    private fun copyToDest(name: String) {
        project.copy {
            it.from(project.buildDir.absolutePath + "/docs")
            it.from(project.projectDir.absolutePath + "/docs")
            it.from(systemDescriptionPath)
            it.include("**")
            it.into(project.rootProject.buildDir.absolutePath + "/docs/system/${name.toLowerCase()}")
        }
        File(project.rootProject.buildDir.absolutePath + "/docs/system/${name.toLowerCase()}/name").writeText(name)
    }

    private fun renderPumls() {
        if (!File(project.projectDir.absolutePath + "/docs/system.puml").exists()) {
            renderPumlsFromSubprojects()
        } else {
            renderSinglePuml()
        }
    }

    private fun renderPumlsFromSubprojects() {
        project.subprojects.map {
            File(it.projectDir.absolutePath + "/docs/system.puml")
        }.filter {
            it.exists()
        }.let {
            renderPumlsTogether(
                it, File(project.buildDir.absolutePath + "/docs/system.png")
            )
        }
    }

    private fun renderSinglePuml() {
        docsDir().listFiles()?.filter {
            it.name.endsWith(".puml")
        }?.forEach {
            val systemView = it.readText()
            val reader = SourceStringReader(systemView)
            val png = File(project.buildDir.absolutePath + "/docs/${it.nameWithoutExtension}.png")
            reader.outputImage(png)
        }
    }

    private fun docsDir(): File = File(project.projectDir.absolutePath + "/docs")

    private fun hasDocsPlugin(subModule: Project) = subModule.plugins.hasPlugin(DocsPlugin::class.java)
}
