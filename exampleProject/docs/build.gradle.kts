import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime

tasks {

    register("prepareHugo") {
        dependsOn(":buildSystemView", ":buildDocsIndex")

        inputs.dir(Paths.get(project.projectDir.absolutePath, "hugo"))
        inputs.dir(Paths.get(rootProject.buildDir.absolutePath, "docs", "system"))
        outputs.dir(Paths.get(project.buildDir.absolutePath, "docs/hugo"))
        doFirst {
            copy {
                from(file("hugo"))
                into(file("build/docs/hugo"))
            }
            val index = file(rootProject.buildDir.absolutePath + "/docs/_index.md").readText(Charsets.UTF_8)
            file("$buildDir/docs/hugo/content/systems/_index.md")
                .readText(Charsets.UTF_8).replace("\${SYSTEMS}", index).let {
                    file("$buildDir/docs/hugo/content/systems/_index.md").writeText(it, Charsets.UTF_8)
                }
            val menu = file(rootProject.buildDir.absolutePath + "/docs/menu.md").readText(Charsets.UTF_8)
            file("$buildDir/docs/hugo/content/menu/index.md")
                .readText(Charsets.UTF_8).replace("\${SYSTEMS}", menu).let {
                    file("$buildDir/docs/hugo/content/menu/index.md").writeText(it, Charsets.UTF_8)
                }

            copy {
                from(file(rootProject.buildDir.absolutePath + "/docs/system"))
                into(file("build/docs/hugo/content/systems"))
            }
            copy {
                from(file(rootProject.buildDir.absolutePath + "/plantuml"))
                from(file("build/plantuml"))
                into(file("build/docs/hugo/content"))
                include("*.png")
            }
        }
    }


    register<Exec>("hugo") {
        val publicPath = Paths.get(project.buildDir.absolutePath, "docs/hugoPublic")
        inputs.dir(Paths.get(project.buildDir.absolutePath, "docs/hugo"))
        outputs.dir(Paths.get(project.buildDir.absolutePath, "docs/hugoPublic"))
        outputs.cacheIf { true }
        doFirst {
            if (publicPath.toFile().exists()) {
                Files.walk(publicPath)
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }
        }
        doLast {
            Files.setLastModifiedTime(publicPath, FileTime.fromMillis(System.currentTimeMillis()))
        }
        dependsOn("prepareHugo")
        description = "Builds the html documentation"
        group = "docs"
        workingDir("build/docs/hugo")
        commandLine("hugo", "-d", "../hugoPublic")
    }
}
