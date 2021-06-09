package io.github.danakuban.docsgradleplugin

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.apache.http.HttpStatus
import java.io.File
import java.net.URL
import javax.net.ssl.HttpsURLConnection


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
            renderTemplate(it)
            renderPumls()
            renderMenu(it)

            copyToDest(it)
        }
    }

    private fun renderMenu(name: String) {
        if (project.parent?.plugins?.hasPlugin("io.github.danakuban.docs-gradle-plugin") == true) {
            logger.info("Parent project has already rendered menu")
            return
        }
        val menu = StringBuilder()
        menu.appendLine("    - [${name}](/systems/${name.toLowerCase()})")
        project.subprojects
            .filter { it.plugins.hasPlugin("io.github.danakuban.docs-gradle-plugin") }
            .forEach {
                menu.appendLine(
                    "      - [${it.name.capitalize()}](/systems/${it.name.toLowerCase()})"
                )
            }
        File(project.buildDir.absolutePath + "/docs/menu").writeText(menu.toString())
    }

    private fun renderEndpoints(): String {
        val client = DefaultKubernetesClient()
        val serviceName =
            project.extensions.findByType(
                net.mayope.deployplugin.DeployExtension::class.java
            )?.serviceName ?: ""
        return client.inAnyNamespace().network().ingresses()
            .withLabels(mapOf("app.kubernetes.io/managed-by" to "Helm", "app.kubernetes.io/instance" to serviceName))
            .list().items.flatMap {
                it.spec.rules
            }.map {
                toBackendOrFrontend(it.host)
            }.joinToString(System.lineSeparator()) {
                "- ${getLinkOpeningInNewTab(it)}"
            }.let {
                if (it.isBlank()) {
                    ""
                } else {
                    "## Endpoints\n$it"
                }
            }
    }

    private fun toBackendOrFrontend(host: String) = when {
        tryUrl(host) == HttpStatus.SC_OK ->
            "https://${host}/swagger-ui.html"
        else -> "https://${host}"
    }

    @Suppress("TooGenericExceptionCaught")
    private fun tryUrl(host: String) = try {
        (URL(
            "https://${host}/swagger-ui.html"
        ).openConnection() as HttpsURLConnection).responseCode
    } catch (e: Throwable) {
        logger.warn("Could not check url: $host", e)
        HttpStatus.SC_NOT_FOUND
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

    private fun renderTemplate(name: String) {
        readDocsTemplate()
            .replace("\${SYSTEM_NAME}", name)
            .replace("\${MODULS}", renderModuls())
            .replace("\${ENDPOINTS}", renderEndpoints())
            .replace("\${SYSTEM_DESCRIPTION}", projectDescription(project))
            .replace("\${ERM}", insertErm())
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
        if (subModule.plugins.hasPlugin("de.otto.salesproduct.buildplugins.DocsPlugin")) {
            "### [${subModule.name}](/systems/${subModule.name.toLowerCase()})\n"
        } else {
            "### ${subModule.name}\n"
        } + projectDescription(subModule) + projectJobs(subModule) + projectEntities(subModule) +
                "\n\n------------\n"

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

    private fun insertErm(): String {
        if (!projectErmFile().exists()) {
            return ""
        }
        return "## Entities\n![erm](erm.png)\n\n"
    }

    private fun projectErmFile() = File(project.projectDir.absolutePath + "/docs/erm.puml")

    private fun projectJobs(it: Project): String {
        projectJobsFile(it).let {
            if (it.exists()) {
                return "\n\nJobs:\n${it.readText().trim()}"
            }
            return ""
        }
    }

    private fun projectJobsFile(it: Project) = File(it.buildDir.absolutePath + "/docs/jobs/jobs.md")

    private fun projectEntities(it: Project): String {
        projectEntitiesFile(it).let {
            if (it.exists()) {
                return "\n\nEntities:\n${it.readText().trim()}"
            }
            return ""
        }
    }

    private fun projectEntitiesFile(it: Project) =
        File(it.buildDir.absolutePath + "/docs/entities/entities.md")

    // we need to remove https from the beginning for target blank to work
    private fun getLinkOpeningInNewTab(link: String): String =
        "<a href=\"$link\" target=\"_blank\">${link.replace("https://", "")}</a>"

    private fun projectDescription(project: Project): String {
        projectDescriptionFile(project).let {
            if (!it.exists()) {
                error("Description for project: ${project.path} is missing please add description.md to project")
            }
            return it.readText().trim()
        }
    }

    private fun footer(): String {
        if (!footerFile().exists()) {
            return ""
        }
        return footerFile().readText(Charsets.UTF_8).trim()
    }

    private fun footerFile() = File(project.projectDir.absolutePath + "/docs/footer.md")

    private fun projectDescriptionFile(project: Project) =
        File(project.projectDir.absolutePath + "/" + systemDescriptionPath)
}


private fun readDocsTemplate() = loadFile("docs_template.md").bufferedReader(Charsets.UTF_8).readText()

private fun loadFile(path: String) = classLoader().getResourceAsStream(path) ?: error(
    "No Resource found in path: $path"
)

private fun classLoader() = Thread.currentThread().contextClassLoader ?: error("Current thread has no classloader")
