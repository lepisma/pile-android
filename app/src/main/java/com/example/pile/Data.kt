package com.example.pile

import java.time.LocalDateTime

data class OrgNode(
    val title: String,
    val datetime: LocalDateTime,
    val path: String,
    val id: String,
    val ref: String? = null
)

fun mockOrgNode(): OrgNode {
    return OrgNode("test", LocalDateTime.now(), "", "")
}

fun readFilesFromRepository(): List<OrgNode> {
    return listOf()
}