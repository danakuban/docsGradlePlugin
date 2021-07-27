package io.github.danakuban.docsGradlePlugin.exampleProject.system1.entities

import io.github.danakuban.docsgradleplugin.processor.EntityDocumentation
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
@EntityDocumentation(name = "User", description = "Describes some user")
internal class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String = "",
    @OneToOne
    val address: Address? = null
)
