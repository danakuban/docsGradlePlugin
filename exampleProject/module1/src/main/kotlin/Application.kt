package io.github.danakuban.docsGradlePlugin.exampleProject.module1

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication

@EntityScan
@SpringBootApplication
internal class Application

fun main(args: Array<String>) {
    @Suppress("SpreadOperator") // no performance issue here
    (runApplication<Application>(*args))
}
