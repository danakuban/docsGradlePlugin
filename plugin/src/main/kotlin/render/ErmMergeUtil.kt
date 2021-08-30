package io.github.danakuban.docsgradleplugin.render

import net.sourceforge.plantuml.SourceStringReader
import java.io.File

fun renderErmsTogether(pumls: List<File>, outputFile: File) {
    val mergedErm = pumls.map { it.readText() }
        .joinToString("\n") { removeHeaderAndFooter(it) }
        .let { "@startuml\n$it\n@enduml" }
    SourceStringReader(mergedErm).outputImage(outputFile)
}

private fun removeHeaderAndFooter(content: String) = content
    .replace("@startuml", "")
    .replace("@enduml", "")
