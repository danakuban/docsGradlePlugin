package io.github.danakuban.docsGradlePlugin.exampleProject.system2.moduleA

import io.github.danakuban.docsgradleplugin.processor.JobDocumentation
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
internal class PublisherSupervisor {

    @PostConstruct
    @JobDocumentation("PublisherSupervisor", "Some supervisor job for publishers.")
    fun checkPublisher() {
        // do something
    }
}
