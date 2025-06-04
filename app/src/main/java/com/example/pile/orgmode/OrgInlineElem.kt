package com.example.pile.orgmode

import java.time.LocalDate
import java.time.LocalTime

/**
 * A piece of text in org mode with consistent styling and interpretation
 */
sealed class OrgInlineElem {
    data class Text(
        val text: String,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class DTStamp(
        val date: LocalDate,
        val showWeekDay: Boolean,
        val time: Pair<LocalTime, LocalTime?>?,
        val isActive: Boolean,
        val repeater: String?,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class DTRange(
        val start: DTStamp,
        val end: DTStamp,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Link(
        val type: String?,
        val target: String,
        val title: OrgLine?,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Citation(
        val citeString: String,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Footnote(
        val key: String?,
        val text: OrgLine,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class InlineMath(
        val style: InlineMathStyle,
        val text: String,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem
    enum class InlineMathStyle { AMS, DOLLAR }

    data class InlineQuote(
        val type: InlineQuoteType,
        val text: String,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem
    enum class InlineQuoteType { HTML, LATEX }

    data class Bold(
        val content: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Italic(
        val content: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Underline(
        val content: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class StrikeThrough(
        val content: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Verbatim(
        val content: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Code(
        val content: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgInlineElem(), OrgElem
}