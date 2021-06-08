package io.github.danakuban.docsGradlePlugin.exampleProject.module2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan
@EnableJpaRepositories
@SpringBootApplication
internal class Application

fun main(args: Array<String>) {
    @Suppress("SpreadOperator") // no performance issue here
    (runApplication<Application>(*args))
}
