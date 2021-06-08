package io.github.danakuban.docsgradleplugin.processor

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JobDocumentation(val name: String, val description: String)

data class JobDocumentationEntry(val methodName: String, val name: String, val description: String)

/**
* Scans all classes for the @EventListener,@Scheduled and @PostConstruct annotation and checks if it's documented with @JobDocumentation.
* Than it extracts this documentation to the directory "jobannotationprocessor.outputdir"
*/
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes
@SupportedOptions(JobAnnotationProcessor.JOBS_OUTPUT_DIR)
class JobAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val JOBS_OUTPUT_DIR = "jobannotationprocessor.outputdir"
    }

    private var outputDir: File? = null
    private val jobDocumentations = mutableListOf<JobDocumentationEntry>()

    override fun getSupportedOptions() = setOf(JOBS_OUTPUT_DIR)

    private fun outputDir(): String {
        processingEnv.options.get(JOBS_OUTPUT_DIR)?.let {
            return it
        }
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Job output directory: $JOBS_OUTPUT_DIR not set")
        error("Job output directory: $JOBS_OUTPUT_DIR not set")
    }

    private val supportedTypes = setOf(PostConstruct::class.java.canonicalName,
        EventListener::class.java.canonicalName, JobDocumentation::class.java.canonicalName,
        Scheduled::class.java.canonicalName)


    override fun getSupportedAnnotationTypes(): Set<String> = supportedTypes

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val jobs = roundEnv.getElementsAnnotatedWithAny(
            setOf(EventListener::class.java, PostConstruct::class.java, Scheduled::class.java))
        val documented = roundEnv.getElementsAnnotatedWith(JobDocumentation::class.java)

        checkForUndocumented(jobs, documented)

        val newJobDocumentations = documented.map {
            JobDocumentationEntry(fullMethodName(it), it.getAnnotation(JobDocumentation::class.java).name,
                it.getAnnotation(JobDocumentation::class.java).description)
        }
        checkForBlankDocumentations(newJobDocumentations)

        jobDocumentations.addAll(newJobDocumentations)

        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }

        if (roundEnv.processingOver()) {
            jobDocumentations.joinToString("\n") { "- ${it.name}: ${it.description}" }.let {
                File(outputDir!!.absolutePath + "/jobs.md").writeText(it)
            }
        }

        return false
    }

    private fun checkForBlankDocumentations(
        jobDocumentations: List<JobDocumentationEntry>) {
        jobDocumentations.filter {
            it.description.isBlank() ||
                it.name.isBlank()
        }.forEach {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "${it.methodName} has a blank JobDocumentation annotation")
            error("undocumented jobs detected")
        }
    }

    private fun checkForUndocumented(jobs: Set<Element>,
                                     documented: Set<Element>) {
        val documentedMethods = documented.map { fullMethodName(it) }
        val undocumented = jobs.filter { fullMethodName(it) !in documentedMethods }
        if (undocumented.isNotEmpty()) {
            undocumented.forEach {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "${it.enclosingElement.simpleName}.${it.simpleName} has @PostConstruct, @Scheduled" +
                        " or @EventListener annotated but doesn't have a @JobDocumentation please fix")
                error("undocumented jobs detected")
            }
        }
    }

    private fun fullMethodName(it: Element) = "${it.enclosingElement.simpleName}.${it.simpleName}"
}
