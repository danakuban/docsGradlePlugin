package io.github.danakuban.docsgradleplugin.render

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class DocsEndpointsRenderer(private val project: Project) {

    private val logger = LoggerFactory.getLogger(DocsEndpointsRenderer::class.java)

    fun writeEndpoints(): String {
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

    private fun toBackendOrFrontend(host: String) = when (HttpStatus.SC_OK) {
        tryUrl(host) -> "https://${host}/swagger-ui.html"
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

    // we need to remove https from the beginning for target blank to work
    private fun getLinkOpeningInNewTab(link: String): String =
        "<a href=\"$link\" target=\"_blank\">${link.replace("https://", "")}</a>"
}
