package com.example.pile.data

import androidx.documentfile.provider.DocumentFile
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Data class representing each node in the zettelkasten. As of now this maps to file based nodes
 * and not Org header based ones.
 *
 * @property pinned Pile-Android specific flag to tell if this node is pinned. This can be moved to
 *                  files later.
 * @property tags   Org mode file tags.
 */
@Entity(tableName = "nodes")
data class OrgNode(
    @PrimaryKey
    val id: String,
    val title: String,
    val datetime: LocalDateTime,
    val fileString: String,
    val file: DocumentFile? = null,
    val pinned: Boolean = false,
    val tags: List<String> = listOf(),
    val lastModified: Long = 0
)

enum class OrgNodeType {
    CONCEPT, LITERATURE, DAILY
}

/*
 Tell whether this is a daily note node only based on the file title. This would improve and become
 more robust later.
 */
fun isDailyNode(node: OrgNode): Boolean = node.title.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
fun isLiteratureNodePath(path: String): Boolean {
    val pattern = Regex("%2Fliterature%2F\\d{14}-")
    return pattern.containsMatchIn(path)
}

fun isLiteratureNode(node: OrgNode): Boolean {
    if (node.file?.parentFile?.name == "literature") {
        return true
    }

    return isLiteratureNodePath(node.fileString)
}

// Tell if the node is a literature node which is not sorted (using Raindrop's terminology). These
// are links that have not been read or skimmed.
fun isUnsortedNode(node: OrgNode): Boolean {
    return node.tags.contains("unsorted")
}