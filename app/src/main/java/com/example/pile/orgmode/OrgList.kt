package com.example.pile.orgmode

/**
 * Org list which could have nested lists inside or list content
 */
sealed class OrgList {
    data class OrgUnorderedList(
        val marker: OrgUnorderedListMarker,
        val checkbox: OrgListCheckState?,
        val items: List<OrgList>,
        override val range: Pair<Int, Int>
    ) : OrgChunk(), OrgElem

    data class OrgOrderedList(
        val marker: OrgOrderedListMarker,
        val checkbox: OrgListCheckState?,
        val items: List<OrgList>,
        override val range: Pair<Int, Int>
    ) : OrgChunk(), OrgElem

    data class OrgListItem(
        val content: List<OrgChunk>,
        override val range: Pair<Int, Int>
    ) : OrgChunk(), OrgElem

    data class OrgDescriptionListItem(
        val term: String,
        val description: OrgChunk.OrgParagraph,
        override val range: Pair<Int, Int>
    ) : OrgChunk(), OrgElem
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