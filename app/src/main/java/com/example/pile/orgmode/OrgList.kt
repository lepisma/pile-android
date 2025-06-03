package com.example.pile.orgmode

/**
 * Org list which could have nested lists inside or list content
 */
sealed class OrgList {
    data class OrgUnorderedList(
        val marker: OrgUnorderedListMarker,
        val checkbox: OrgListCheckState?,
        val items: List<OrgList>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgOrderedList(
        val marker: OrgOrderedListMarker,
        val checkbox: OrgListCheckState?,
        val items: List<OrgList>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgListItem(
        val content: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgDescriptionListItem(
        val term: String,
        val description: OrgChunk.OrgParagraph,
        val range: Pair<Int, Int>
    ) : OrgChunk()
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