package com.example.pile.orgmode

import java.time.LocalDate
import java.time.LocalTime

/**
 * A piece of text in org mode with consistent styling and interpretation
 */
sealed class OrgInlineElem {
    data class Text(
        val text: String,
    ) : OrgInlineElem()

    data class DTStamp(
        val date: LocalDate,
        val showWeekDay: Boolean,
        val time: Pair<LocalTime, LocalTime?>?,
        val isActive: Boolean,
        val repeater: String?
    ) : OrgInlineElem()

    data class DTRange(
        val start: DTStamp,
        val end: DTStamp
    ) : OrgInlineElem()

    data class Link(
        val type: String?,
        val target: String,
        val title: OrgLine?
    ) : OrgInlineElem()

    data class Citation(
        val citeString: String
    ) : OrgInlineElem()

    data class Footnote(
        val key: String?,
        val text: OrgLine
    ) : OrgInlineElem()

    data class InlineMath(
        val style: InlineMathStyle,
        val text: String,
    ) : OrgInlineElem()
    enum class InlineMathStyle { AMS, DOLLAR }

    data class InlineQuote(
        val type: InlineQuoteType,
        val text: String,
    ) : OrgInlineElem()
    enum class InlineQuoteType { HTML, LATEX }

    data class Bold(
        val content: List<OrgInlineElem>
    ) : OrgInlineElem()

    data class Italic(
        val content: List<OrgInlineElem>
    ) : OrgInlineElem()

    data class Underline(
        val content: List<OrgInlineElem>
    ) : OrgInlineElem()

    data class StrikeThrough(
        val content: List<OrgInlineElem>
    ) : OrgInlineElem()

    data class Verbatim(
        val content: List<OrgInlineElem>
    ) : OrgInlineElem()

    data class Code(
        val content: List<OrgInlineElem>
    ) : OrgInlineElem()
}