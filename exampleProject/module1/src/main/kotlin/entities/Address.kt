package io.github.danakuban.docsGradlePlugin.exampleProject.module1.entities

import io.github.danakuban.docsgradleplugin.processor.EntityDocumentation
import java.util.UUID
import java.util.UUID.randomUUID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@EntityDocumentation(name = "Address", description = "Describes the address of a user")
internal class Address(
    @Id
    val id: UUID = randomUUID(),

    val street: String = "",
    val zip: String = "",
    val city: String = ""
)
