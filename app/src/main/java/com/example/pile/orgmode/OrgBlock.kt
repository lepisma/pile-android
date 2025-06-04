package com.example.pile.orgmode

sealed class OrgBlock: OrgElem {
    data class OrgCommentBlock(
        val text: String,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgExampleBlock(
        val text: String,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgSourceBlock(
        val language: String,
        val switches: List<String>,
        val headerArgs: List<String>,
        val body: String,
        val name: String?,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgQuoteBlock(
        val body: List<OrgChunk>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgCenterBlock(
        val body: List<OrgChunk>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgHTMLBlock(
        val body: String,
        val name: String?,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgVerseBlock(
        val body: String,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgLaTeXBlock(
        val body: String,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgPageIntroBlock(
        val body: List<OrgChunk>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgEditsBlock(
        val body: List<OrgChunk>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgAsideBlock(
        val body: List<OrgChunk>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgVideoBlock(
        val body: List<OrgChunk>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem
}