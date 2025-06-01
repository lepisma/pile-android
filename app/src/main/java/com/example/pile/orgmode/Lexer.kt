package com.example.pile.orgmode

import java.time.LocalDate
import java.time.LocalTime

sealed class Token {
    abstract val text: String
    abstract val range: Pair<Int, Int>

    // All the usual whitespaces
    data class LineBreak(
        override val text: String,
        override val range: Pair<Int, Int>
    ) : Token()

    data class Space(
        override val text: String,
        override val range: Pair<Int, Int>
    ) : Token()

    data class Tab(
        override val text: String,
        override val range: Pair<Int, Int>
    ) : Token()

    data class EOF(
        override val text: String,
        override val range: Pair<Int, Int>,
    ) : Token()

    // Drawers like the ones for Properties above the title or after headings
    data class DrawerStart(
        override val text: String,
        override val range: Pair<Int, Int>,
        val type: DrawerType
    ) : Token()
    enum class DrawerType { PROPERTIES }

    data class DrawerPropertyKey(
        override val text: String,
        override val range: Pair<Int, Int>,
        val key: String
    ) : Token()

    data class DrawerPropertyValue(
        override val text: String,
        override val range: Pair<Int, Int>,
        val value: String
    ) : Token()

    data class DrawerEnd(
        override val text: String,  // :END:
        override val range: Pair<Int, Int>
    ) : Token()

    // File level configuration lines. Few of these can come up at non-file level too
    data class FileKeyword(
        override val text: String,  // #+<KEYWORD>:
        override val range: Pair<Int, Int>,
        val type: FileKeywordType
    ) : Token()
    enum class FileKeywordType {
        TITLE,
        AUTHOR,
        EMAIL,
        DATE,
        SUBTITLE,
        DESCRIPTION,
        KEYWORDS,
        CATEGORY,
        FILETAGS,
        EXPORT_SELECT_TAGS,
        EXPORT_FILE_NAME,
        EXPORT_EXCLUDE_TAGS,
        TAGS,
        LANGUAGE,
        STARTUP,
        TODO,
        TYP_TODO,
        SEQ_TODO,
        PROPERTY,
        PILE,
        TOC,
        OPTIONS,
        LATEX_HEADER,
        HTML_HEAD,
        PRIORITIES,
        CITE_EXPORT
    }

    // Blocks
    data class BlockStart(
        override val text: String,  // #+BEGIN_<stuff>
        override val range: Pair<Int, Int>,
        val type: BlockType
    ) : Token()
    enum class BlockType {
        COMMENT,
        EXAMPLE,
        SRC,
        QUOTE,
        CENTER,
        HTML,
        VERSE,
        NONAME,   // Things like clock table are like this where BEGIN ends with :

        // These are custom
        PAGE_INTRO,
        EDIT,
        ASIDE,
        VIDEO
    }

    data class BlockEnd(
        override val text: String,
        override val range: Pair<Int, Int>,
        val type: BlockType
    ) : Token()

    data class CodeBlockResultStart(
        override val text: String,  // RESULTS:
        override val range: Pair<Int, Int>,
        val type: BlockType
    ) : Token()

    // This is used in output of the above
    data class FixedWidthLineStart(
        override val text: String,  // :
        override val range: Pair<Int, Int>,
    ) : Token()

    data class Keyword(
        override val text: String,
        override val range: Pair<Int, Int>,
        val type: KeywordType
    ) : Token()
    enum class KeywordType {
        ATTR_HTML,
        NAME,
        HTML,
        CAPTION,
        BIBLIOGRAPHY,
        TBLFM,
        CONSTANTS
    }

    data class HeadingStars(
        override val text: String,
        override val range: Pair<Int, Int>,
        val level: Int
    ) : Token()

    data class UnorderedListMarker(
        override val text: String,
        override val range: Pair<Int, Int>,
        val style: UnorderedListMarkerStyle,
        val nIndent: Int
    ) : Token()
    // We don't support * for plain list markers. See https://orgmode.org/manual/Plain-Lists.html#FOOT9 for the why
    enum class UnorderedListMarkerStyle { PLUS, DASH }

    data class OrderedListMarker(
        override val text: String,
        override val range: Pair<Int, Int>,
        val style: OrderedListMarkerStyle,
        val nIndent: Int
    ) : Token()
    enum class OrderedListMarkerStyle {
        PERIOD,      // 1.
        PARENTHESIS  // 1)
    }

    data class CheckBox(
        override val text: String,
        override val range: Pair<Int, Int>,
        val state: CheckBoxState
    ) : Token()
    enum class CheckBoxState {
        CHECKED,   // [X]
        UNCHECKED, // [ ]
        PARTIAL    // [-]
    }

    data class DescriptionListSep(
        override val text: String,  // ::
        override val range: Pair<Int, Int>
    ) : Token()

    // Main body text
    data class EmphasisDelimiter(
        override val text: String, // *, /, _, +, =, ~
        override val range: Pair<Int, Int>,
        val type: EmphasisType
    ) : Token()
    enum class EmphasisType { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, VERBATIM, CODE }

    data class InlineQuoteStart(
        override val text: String,  // @@<type>:
        override val range: Pair<Int, Int>,
        val type: InlineQuoteType
    ) : Token()
    enum class InlineQuoteType { HTML, LATEX }

    data class InlineQuoteEnd(
        override val text: String,  // @@
        override val range: Pair<Int, Int>
    ) : Token()

    // LaTeX stuff
    data class InlineMathStart(
        override val text: String,
        override val range: Pair<Int, Int>,
        val style: InlineMathStyle
    ) : Token()
    enum class InlineMathStyle {
        DOLLAR, // $ ... $
        AMS     // \( ... \)
    }

    data class InlineMathEnd(
        override val text: String,
        override val range: Pair<Int, Int>,
        val style: InlineMathStyle
    ) : Token()

    data class DisplayedMathStart(
        override val text: String,
        override val range: Pair<Int, Int>,
        val style: DisplayedMathStyle
    ) : Token()
    enum class DisplayedMathStyle {
        DOLLAR,  // $$
        AMS      // \[ ... \]
    }

    data class DisplayedMathEnd(
        override val text: String,
        override val range: Pair<Int, Int>,
        val style: DisplayedMathStyle
    ) : Token()

    data class MathBlockStart(
        override val text: String,  // \begin{<name>}
        override val range: Pair<Int, Int>,
        val name: String
    ) : Token()

    data class MathBlockEnd(
        override val text: String,  // \end{<name>}
        override val range: Pair<Int, Int>,
        val name: String
    ) : Token()

    data class TagString(
        override val text: String,
        override val range: Pair<Int, Int>,
        val tags: List<String>
    ) : Token()

    data class TODOKeyword(
        override val text: String,
        override val range: Pair<Int, Int>,
        val name: String,
        val state: TODOState
    ) : Token()
    enum class TODOState { TODO, DONE }

    data class PriorityKeyword(
        override val text: String, // [#A]
        override val range: Pair<Int, Int>,
        val value: String
    ) : Token()

    data class FootnoteStart(
        override val text: String, // [fn:
        override val range: Pair<Int, Int>,
        val ref: String,
    ) : Token()

    data class Colon(
        override val text: String,  // :
        override val range: Pair<Int, Int>,
    ) : Token()

    data class SemiColon(
        override val text: String,  // ;
        override val range: Pair<Int, Int>
    ) : Token()

    data class QuotationMark(
        override val text: String,  // "
        override val range: Pair<Int, Int>
    ) : Token()

    data class FootnoteEnd(
        override val text: String,  // ]
        override val range: Pair<Int, Int>,
    ) : Token()

    data class CitationStart(
        override val text: String,  // [cite
        override val range: Pair<Int, Int>
    ) : Token()

    data class CitationEnd(
        override val text: String,  // ]
        override val range: Pair<Int, Int>
    ) : Token()

    data class CitationKey(
        override val text: String,  // @<key>
        override val range: Pair<Int, Int>,
        val key: String
    ) : Token()

    // Planning and datetime
    data class Scheduled(
        override val text: String,  // SCHEDULED:
        override val range: Pair<Int, Int>
    ) : Token()

    data class Deadline(
        override val text: String,  // DEADLINE:
        override val range: Pair<Int, Int>
    ) : Token()

    data class Closed(
        override val text: String,  // CLOSED:
        override val range: Pair<Int, Int>
    ) : Token()

    // This handles full parsing of datetime
    data class DatetimeStamp(
        override val text: String,
        override val range: Pair<Int, Int>,
        val date: LocalDate,
        val showWeekDay: Boolean,
        val time: Pair<LocalTime, LocalTime?>?,
        val isActive: Boolean,
        val repeater: String?
    ) : Token()

    data class DatetimeDateRangeSep(
        override val text: String,  // --
        override val range: Pair<Int, Int>
    ) : Token()

    data class TableColumnSep(
        override val text: String,  // |
        override val range: Pair<Int, Int>
    ) : Token()

    data class TableRowSep(
        override val text: String,  // -
        override val range: Pair<Int, Int>
    ) : Token()

    data class TableIntersection(
        override val text: String,  // +
        override val range: Pair<Int, Int>
    ) : Token()

    // This also handles header
    data class TableCell(
        override val text: String,
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkStart(
        override val text: String, // [[
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkEnd(
        override val text: String, // ]]
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkTitleSep(
        override val text: String, // ][
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkUrl(
        override val text: String,
        override val range: Pair<Int, Int>,
    ) : Token()

    data class HorizontalRule(
        override val text: String, // -----
        override val range: Pair<Int, Int>
    ) : Token()

    data class CommentStart(
        override val text: String, // #
        override val range: Pair<Int, Int>
    ) : Token()

    // Unparseable text
    data class Error(
        override val text: String,
        override val range: Pair<Int, Int>,
        val message: String
    ) : Token()
}