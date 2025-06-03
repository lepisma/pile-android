package com.example.pile.orgmode

sealed class OrgBlock {
    data class OrgCommentBlock(
        val text: String
    ) : OrgChunk()

    data class OrgExampleBlock(
        val text: String
    ) : OrgChunk()

    data class OrgSourceBlock(
        val language: String,
        val switches: List<String>,
        val headerArgs: List<String>,
        val body: String,
        val name: String?
    ) : OrgChunk()

    data class OrgQuoteBlock(
        val body: List<OrgChunk>
    ) : OrgChunk()

    data class OrgCenterBlock(
        val body: List<OrgChunk>
    ) : OrgChunk()

    data class OrgHTMLBlock(
        val body: String,
        val name: String?
    ) : OrgChunk()

    data class OrgVerseBlock(
        val body: String
    ) : OrgChunk()

    data class OrgLaTeXBlock(
        val body: String
    ) : OrgChunk()

    data class OrgPageIntroBlock(
        val body: List<OrgChunk>
    ) : OrgChunk()

    data class OrgEditsBlock(
        val body: List<OrgChunk>
    ) : OrgChunk()

    data class OrgAsideBlock(
        val body: List<OrgChunk>
    ) : OrgChunk()

    data class OrgVideoBlock(
        val body: List<OrgChunk>
    ) : OrgChunk()
}