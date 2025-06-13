package com.example.pile.orgmode

/**
 * A section is an org chunk with heading attached to it
 */
data class OrgSection(
    val heading: OrgHeading,
    val body: List<OrgChunk>,
    override var tokens: List<Token>
) : OrgChunk(), OrgElem

data class OrgHeading(
    val title: OrgLine,
    val level: OrgHeadingLevel,
    val tags: OrgTags? = null,
    val todoState: OrgTODOState? = null,
    val priority: OrgPriority? = null,
    val planningInfo: OrgPlanningInfo? = null,
    val properties: OrgProperties? = null,
    override var tokens: List<Token>
) : OrgElem

data class OrgHeadingLevel(
    val level: Int,
    override var tokens: List<Token>
) : OrgElem

data class OrgPlanningInfo(
    val scheduled: OrgInlineElem.DTStamp?,
    val deadline: OrgInlineElem.DTStamp?,
    val closed: OrgInlineElem.DTStamp?,
    override var tokens: List<Token>
) : OrgElem

data class OrgPriority(
    val priority: Int,
    val text: String,
    override var tokens: List<Token>
) : OrgElem

data class OrgTODOState(
    val text: String,
    val isDone: Boolean,
    override var tokens: List<Token>
) : OrgElem