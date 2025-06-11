package com.example.pile.orgmode

/**
 * Org list which could have nested lists inside or list content
 */
sealed class OrgList {
    data class OrgUnorderedList(
        val markerStyle: OrgUnorderedListMarker,
        val items: List<OrgList>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgOrderedList(
        val markerStyle: OrgOrderedListMarker,
        val items: List<OrgList>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgListItem(
        val content: List<OrgChunk>,
        val checkbox: OrgListCheckState?,
        override val tokens: List<Token>
    ) : OrgList(), OrgElem

    data class OrgDescriptionListItem(
        val term: String,
        val description: OrgChunk.OrgParagraph,
        override val tokens: List<Token>
    ) : OrgList(), OrgElem
}

enum class OrgUnorderedListMarker {
    PLUS,
    DASH
}

enum class OrgOrderedListMarker {
    PERIOD,
    PARENTHESIS
}

enum class OrgListCheckState {
    CHECKED, UNCHECKED, PARTIAL
}