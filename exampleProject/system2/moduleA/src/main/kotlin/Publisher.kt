package io.github.danakuban.docsGradlePlugin.exampleProject.system2.moduleA

import io.github.danakuban.docsgradleplugin.processor.EntityDocumentation
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@EntityDocumentation(name = "Author", description = "Describes an author")
internal class Author(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String = "",
)
