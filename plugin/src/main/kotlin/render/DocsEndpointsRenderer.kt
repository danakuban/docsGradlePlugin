package io.github.danakuban.docsgradleplugin.render

import io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress
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
        return (findHosts(client, serviceName)).map {
            toBackendOrFrontend(it)
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

    private fun findHosts(client: DefaultKubernetesClient,
        serviceName: String) =
        listIngress(client, serviceName).flatMap {
            it.spec.rules.map { it.host }
        } + listBetaIngress(client, serviceName).flatMap {
            it.spec.rules.map { it.host }
        }

    private fun listBetaIngress(client: DefaultKubernetesClient,
        serviceName: String): List<Ingress> = try {
            client.inAnyNamespace().network().apiVersion
        client.inAnyNamespace().network().v1beta1().ingresses()
            .withLabels(mapOf("app.kubernetes.io/managed-by" to "Helm", "app.kubernetes.io/instance" to serviceName))
            .list().items
    } catch (e: Throwable) {
        logger.error("Could not fetch ingresses", e)
        emptyList()
    }

    private fun listIngress(client: DefaultKubernetesClient,
        serviceName: String): List<io.fabric8.kubernetes.api.model.networking.v1.Ingress> = try {
        client.inAnyNamespace().network().v1().ingresses()
            .withLabels(mapOf("app.kubernetes.io/managed-by" to "Helm", "app.kubernetes.io/instance" to serviceName))
            .list().items
    } catch (e: Throwable) {
        logger.error("Could not fetch ingresses", e)
        emptyList()
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
