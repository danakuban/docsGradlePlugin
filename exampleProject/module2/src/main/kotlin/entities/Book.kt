package io.github.danakuban.docsGradlePlugin.exampleProject.module2.entities

import io.github.danakuban.docsgradleplugin.processor.EntityDocumentation
import java.util.UUID
import java.util.UUID.randomUUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
@EntityDocumentation(name = "Book", description = "Describes a book")
internal class Book(
    @Id
    val id: UUID = randomUUID(),

    val title: String = "",
    @ManyToOne
    val Author: Author? = null,
    val publisher: String = "",
    val year: Int = 0,
)
