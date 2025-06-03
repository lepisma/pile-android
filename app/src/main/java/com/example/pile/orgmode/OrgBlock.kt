package com.example.pile.orgmode

sealed class OrgBlock {
    data class OrgCommentBlock(
        val text: String,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgExampleBlock(
        val text: String,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgSourceBlock(
        val language: String,
        val switches: List<String>,
        val headerArgs: List<String>,
        val body: String,
        val name: String?,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgQuoteBlock(
        val body: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgCenterBlock(
        val body: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgHTMLBlock(
        val body: String,
        val name: String?,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgVerseBlock(
        val body: String,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgLaTeXBlock(
        val body: String,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgPageIntroBlock(
        val body: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgEditsBlock(
        val body: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgAsideBlock(
        val body: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()

    data class OrgVideoBlock(
        val body: List<OrgChunk>,
        val range: Pair<Int, Int>
    ) : OrgChunk()
}