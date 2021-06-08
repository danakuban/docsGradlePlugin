package io.github.danakuban.docsGradlePlugin.exampleProject.module2.entities

import io.github.danakuban.docsgradleplugin.processor.EntityDocumentation
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
@EntityDocumentation(name = "Author", description = "Describes an author")
internal class Author(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String = "",
)
