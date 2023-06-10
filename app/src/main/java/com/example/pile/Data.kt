package com.example.pile

import java.time.LocalDateTime
import io.github.serpro69.kfaker.Faker

data class OrgNode(
    val title: String,
    val datetime: LocalDateTime,
    val path: String,
    val id: String,
    val ref: String? = null
)

fun mockOrgNode(): OrgNode {
    val faker = Faker()
    return OrgNode(faker.name.nameWithMiddle(), LocalDateTime.now(), "/path/to/file", faker.random.nextUUID())
}

fun readFilesFromRepository(): List<OrgNode> {
    return (1..10).map({ it -> mockOrgNode() })
}