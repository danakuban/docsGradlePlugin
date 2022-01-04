package io.github.danakuban.docsGradlePlugin.exampleProject.system1.jobs

import io.github.danakuban.docsgradleplugin.processor.JobDocumentation
import io.github.danakuban.docsgradleplugin.processor.SuppressJobDocumentation
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

abstract class Abstract {

    @PostConstruct
    @SuppressJobDocumentation
    fun initialize() {
        // do something
    }
}

@Component
@Suppress("Unused")
@JobDocumentation(name = "NotifyOnAppStartJob", description = "Notifies when app is ready")
internal class NotificationService: Abstract()
