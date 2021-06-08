package io.github.danakuban.docsGradlePlugin.exampleProject.module1.jobs

import io.github.danakuban.docsgradleplugin.processor.JobDocumentation
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Suppress("Unused")
internal class NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    @EventListener(value = [ApplicationStartedEvent::class])
    @JobDocumentation(name = "NotifyOnAppStartJob", description = "Notifies when app is ready")
    fun notifyOnAppStart() {
        logger.debug("App is ready.")
    }

    @Scheduled(cron = "0 0 * * * *")
    @JobDocumentation(name = "NotifyOnMidnightJob", description = "Notifies on midnight")
    fun notifyOnMidnight() {
        logger.debug("It's midnight!")
    }

    @PostConstruct
    @JobDocumentation(name = "NotifyOnPostConstructJob", description = "Notifies on service construction")
    fun notifyOnPostConstruct() {
        logger.debug("NotificationService is ready.")
    }
}
