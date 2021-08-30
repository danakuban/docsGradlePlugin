package io.github.danakuban.docsGradlePlugin.exampleProject.system2.moduleA

import io.github.danakuban.docsgradleplugin.processor.EntityDocumentation
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@EntityDocumentation(name = "Publisher", description = "Describes a publisher")
internal class Publisher(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String = ""
)
