package io.github.danakuban.docsgradleplugin.render

import io.github.danakuban.docsgradleplugin.DocsPlugin
import org.gradle.api.Project
import java.io.File

internal class DocsTemplateRenderer(private val project: Project, private val systemDescriptionPath: String) {

    private val endpointsWriter = DocsEndpointsRenderer(project)

    fun renderTemplate(name: String) {
        readDocsTemplate()
            .replace("\${SYSTEM_NAME}", name)
            .replace("\${MODULS}", renderModuls())
            .replace("\${ENDPOINTS}", endpointsWriter.writeEndpoints())
            .replace("\${SYSTEM_DESCRIPTION}", projectDescription(project))
            .replace("\${ERM}", insertErm())
            .replace("\${JOBS}", renderJobs())
            .replace("\${FOOTER}", footer()).let {
                File(project.buildDir.absolutePath + "/docs").mkdirs()
                File(project.buildDir.absolutePath + "/docs/_index.md").writeText(it)
            }
    }

    private fun renderModuls() = project.subprojects.filter {
        it.parent == project
    }.joinToString("\n") {
        renderModule(it)
    }.let {
        if (it.isNotBlank()) {
            "## Modules\n${it}"
        } else {
            ""
        }
    }

    private fun renderModule(subModule: Project) =
        if (hasDocsPlugin(subModule)) {
            "### [${subModule.name}](/systems/${subModule.name.toLowerCase()})\n"
        } else {
            "### ${subModule.name}\n"
        } + projectDescription(subModule) + "\n\n------------\n"

    private fun hasDocsPlugin(subModule: Project) = subModule.plugins.hasPlugin(DocsPlugin::class.java)

    private fun insertErm(): String {
        val entities = loadFileContent(projectEntitiesFile(project)) +
            project.subprojects.filter { it.parent == project }
                .filter { !hasDocsPlugin(it) }
                .joinToString { loadFileContent(projectEntitiesFile(it)) }
        if (!projectErmFile().exists() && entities.isNotBlank()) {
            return "## Entities\n\n$entities\n\n"
        } else if (!projectErmFile().exists()) {
            return ""
        }
        return "## Entities\n![erm](erm.png)\n\n$entities\n\n"
    }

    private fun renderJobs(): String {
        return loadFileContent(projectJobsFile(project), "## Jobs\n\n") + "\n\n"
    }

    private fun footer() = loadFileContent(footerFile())

    private fun projectDescription(project: Project): String {
        projectDescriptionFile(project).let {
            if (!it.exists()) {
                error("Description for project: ${project.path} is missing please add description.md to project")
            }
            return it.readText().trim()
        }
    }

    private fun loadFileContent(file: File, prefix: String = ""): String {
        if (file.exists()) {
            return prefix + file.readText().trim()
        }
        return ""
    }

    private fun projectEntitiesFile(it: Project) =
        File(it.buildDir.absolutePath + "/docs/entities/entities.md")

    private fun projectJobsFile(it: Project) = File(it.buildDir.absolutePath + "/docs/jobs/jobs.md")

    private fun projectDescriptionFile(project: Project) =
        File(project.projectDir.absolutePath + "/" + systemDescriptionPath)

    private fun projectErmFile() = File(project.projectDir.absolutePath + "/docs/erm.puml")

    private fun footerFile() = File(project.projectDir.absolutePath + "/docs/footer.md")
}

private fun readDocsTemplate() = loadFile("docs_template.md").bufferedReader(Charsets.UTF_8).readText()

private fun loadFile(path: String) = classLoader().getResourceAsStream(path) ?: error(
    "No Resource found in path: $path"
)

private fun classLoader() = Thread.currentThread().contextClassLoader ?: error("Current thread has no classloader")
