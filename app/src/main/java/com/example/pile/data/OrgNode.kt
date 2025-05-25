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
 * @property datetime Created (local) datetime for the node
 * @property lastModified Milliseconds since epoch representing last modified time
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
    val lastModified: Long = 0,
    val nodeType: OrgNodeType,
    val ref: String? = null
)

enum class OrgNodeType {
    CONCEPT, LITERATURE, DAILY
}

fun isDailyNode(node: OrgNode): Boolean = node.nodeType == OrgNodeType.DAILY
fun isLiteratureNode(node: OrgNode): Boolean = node.nodeType == OrgNodeType.LITERATURE

// Tell if the node is a literature node which is not sorted (using Raindrop's terminology). These
// are links that have not been read or skimmed.
fun isUnsortedNode(node: OrgNode): Boolean {
    return node.tags.contains("unsorted")
}