package com.example.pile.data

import androidx.documentfile.provider.DocumentFile
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Data class representing each node in the zettelkasten. As of now this maps to file based nodes
 * and not ones based on Org headers. This only contains the metadata and not the main content. For
 * that, checkout `OrgContent`.
 *
 * A few values are ignored for Room since they are recovered using certain DB joins while reading.
 *
 * @property id            Org-Roam id for this node
 * @property title         Title of the note
 * @property datetime      Created (local) datetime for the node
 * @property fileString    Local file URI for recovering the `file` object
 * @property file          DocumentFile object from the `fileString`
 * @property pinned        Tell if this node is pinned
 * @property tags          Tags for this node
 * @property lastModified  Milliseconds since epoch representing last modified time
 * @property nodeType      See OrgNodeType enum
 * @property ref           Reference url/id for literature nodes
 */
@Entity(tableName = "nodes")
data class OrgNode(
    @PrimaryKey
    val id: String,
    val title: String,
    val datetime: LocalDateTime,
    val fileString: String,
    @Ignore
    val file: DocumentFile? = null,
    val pinned: Boolean = false,
    @Ignore
    val tags: List<String> = listOf(),
    val lastModified: Long = 0,
    val nodeType: OrgNodeType,
    val ref: String? = null
)  {
    constructor(
        id: String,
        title: String,
        datetime: LocalDateTime,
        fileString: String,
        pinned: Boolean,
        lastModified: Long,
        nodeType: OrgNodeType,
        ref: String?
    ) : this(
        id = id,
        title = title,
        datetime = datetime,
        fileString = fileString,
        file = null,
        pinned = pinned,
        tags = listOf(),
        lastModified = lastModified,
        nodeType = nodeType,
        ref = ref
    )
}


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