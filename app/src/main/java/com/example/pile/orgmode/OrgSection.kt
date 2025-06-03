package com.example.pile.orgmode

/**
 * A section is an org chunk with heading attached to it
 */
data class OrgSection(
    val heading: OrgHeading,
    val body: List<OrgChunk>,
    val range: Pair<Int, Int>
) : OrgChunk()

data class OrgHeading(
    val title: String,
    val level: Int,
    val tags: List<String>,
    val todoState: OrgTODOState?,
    val priority: OrgPriority?,
    val scheduled: OrgInlineElem.DTStamp?,
    val deadline: OrgInlineElem.DTStamp?,
    val closed: OrgInlineElem.DTStamp?,
    val properties: OrgProperties,
    val range: Pair<Int, Int>
)

data class OrgPriority(
    val p: Int,
    val text: String,
    val range: Pair<Int, Int>
)

data class OrgTODOState(
    val text: String,
    val isDone: Boolean,
    val range: Pair<Int, Int>
)