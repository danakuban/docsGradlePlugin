package io.github.danakuban.docsgradleplugin.processor

import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.persistence.Entity
import javax.tools.Diagnostic

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EntityDocumentation(val name: String, val description: String)

data class EntityDocumentationEntry(val className: String, val name: String, val description: String)

/**
* Scans all classes for the @Entity annotation and checks if it's documented and extracts this documentation to
* the directory "entityannotationprocessor.outputdir"
*/
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes
@SupportedOptions(EntityAnnotationProcessor.ENTITY_OUTPUT_DIR)
class EntityAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val ENTITY_OUTPUT_DIR = "entityannotationprocessor.outputdir"
    }

    private var outputDir: File? = null
    private val entityDocumentations = mutableListOf<EntityDocumentationEntry>()

    override fun getSupportedOptions() = setOf(ENTITY_OUTPUT_DIR)

    private fun outputDir(): String {
        processingEnv.options.get(ENTITY_OUTPUT_DIR)?.let {
            return it
        }
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Entity output directory: $ENTITY_OUTPUT_DIR not set")
        error("Job output directory: $ENTITY_OUTPUT_DIR not set")
    }

    private val supportedTypes = setOf(Entity::class.java.canonicalName, EntityDocumentation::class.java.canonicalName)


    override fun getSupportedAnnotationTypes(): Set<String> = supportedTypes

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val entities = roundEnv.getElementsAnnotatedWithAny(
            setOf(Entity::class.java))
        val documented = roundEnv.getElementsAnnotatedWith(EntityDocumentation::class.java)

        checkForUndocumented(entities, documented)

        val newJobDocumentations = documented.map {
            EntityDocumentationEntry(fullClassName(it), it.getAnnotation(EntityDocumentation::class.java).name,
                it.getAnnotation(EntityDocumentation::class.java).description)
        }
        checkForBlankDocumentations(newJobDocumentations)

        entityDocumentations.addAll(newJobDocumentations)

        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }

        if (roundEnv.processingOver()) {
            entityDocumentations.joinToString("\n") { "- ${it.name}: ${it.description}" }.let {
                File(outputDir!!.absolutePath + "/entities.md").writeText(it)
            }
        }

        return false
    }

    private fun checkForBlankDocumentations(jobDocumentations: List<EntityDocumentationEntry>) {
        jobDocumentations.filter {
            it.description.isBlank() ||
                it.name.isBlank()
        }.forEach {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "${it.className} has a blank EntityDocumentation annotation")
            error("undocumented entities detected")
        }
    }

    private fun checkForUndocumented(jobs: Set<Element>, documented: Set<Element>) {
        val documentedMethods = documented.map { fullClassName(it) }
        val undocumented = jobs.filter { fullClassName(it) !in documentedMethods }
        if (undocumented.isNotEmpty()) {
            undocumented.forEach {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "${fullClassName(it)} has @Entity" +
                        " annotated but doesn't have a @EntityDocumentation please fix")
                error("undocumented jobs detected")
            }
        }
    }

    private fun fullClassName(it: Element) = "${it.enclosingElement.simpleName}.${it.simpleName}"
}
