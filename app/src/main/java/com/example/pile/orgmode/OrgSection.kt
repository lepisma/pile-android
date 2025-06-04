package com.example.pile.orgmode

/**
 * A section is an org chunk with heading attached to it
 */
data class OrgSection(
    val heading: OrgHeading,
    val body: List<OrgChunk>,
    override val tokens: List<Token>
) : OrgChunk(), OrgElem

data class OrgHeading(
    val title: String,
    val level: Int,
    val tags: List<String>,
    val todoState: OrgTODOState?,
    val priority: OrgPriority?,
    val planningInfo: OrgPlanningInfo,
    val properties: OrgProperties,
    override val tokens: List<Token>
) : OrgElem

data class OrgPlanningInfo(
    val scheduled: OrgInlineElem.DTStamp?,
    val deadline: OrgInlineElem.DTStamp?,
    val closed: OrgInlineElem.DTStamp?,
    override val tokens: List<Token>
) : OrgElem

data class OrgPriority(
    val priority: Int,
    val text: String,
    override val tokens: List<Token>
) : OrgElem

data class OrgTODOState(
    val text: String,
    val isDone: Boolean,
    override val tokens: List<Token>
) : OrgElem