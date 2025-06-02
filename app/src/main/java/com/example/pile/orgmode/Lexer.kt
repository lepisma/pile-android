package com.example.pile.orgmode

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.max

// TODO: Should probably use IntRange for range
sealed class Token {
    abstract val text: String
    abstract val range: Pair<Int, Int>

    // Start of file
    data class SOF(
        override val text: String = "",
        override val range: Pair<Int, Int> = Pair(0, 0),
    ) : Token()

    // All the usual whitespaces
    data class LineBreak(
        override val text: String = "\n",
        override val range: Pair<Int, Int>
    ) : Token()

    data class Space(
        override val text: String = " ",
        override val range: Pair<Int, Int>
    ) : Token()

    data class Tab(
        override val text: String = "\t",
        override val range: Pair<Int, Int>
    ) : Token()

    data class EOF(
        override val text: String = "",
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
        override val text: String = ":END:",
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
        LATEX,

        // These are custom
        PAGE_INTRO,
        EDITS,
        ASIDE,
        VIDEO
    }

    data class BlockEnd(
        override val text: String, // #+END_<stuff>
        override val range: Pair<Int, Int>,
        val type: BlockType
    ) : Token()

    // Things like clock table are like this where BEGIN ends with :
    data class GenericBlockStart(
        override val text: String = "#+BEGIN:",
        override val range: Pair<Int, Int>,
    ) : Token()

    data class GenericBlockEnd(
        override val text: String = "#+END:",
        override val range: Pair<Int, Int>
    ) : Token()

    // This is used in output of the above
    data class FixedWidthLineStart(
        override val text: String = ":",
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
        CONSTANTS,
        RESULTS
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
        override val text: String = "::",
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
        override val text: String = "@@",
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
        override val text: String = "[fn:",
        override val range: Pair<Int, Int>,
        val ref: String,
    ) : Token()

    data class Colon(
        override val text: String = ":",
        override val range: Pair<Int, Int>,
    ) : Token()

    data class SemiColon(
        override val text: String = ";",
        override val range: Pair<Int, Int>
    ) : Token()

    data class QuotationMark(
        override val text: String = "\"",
        override val range: Pair<Int, Int>
    ) : Token()

    data class FootnoteEnd(
        override val text: String = "]",
        override val range: Pair<Int, Int>,
    ) : Token()

    data class CitationStart(
        override val text: String = "[cite",
        override val range: Pair<Int, Int>
    ) : Token()

    data class CitationEnd(
        override val text: String = "]",
        override val range: Pair<Int, Int>
    ) : Token()

    data class CitationKey(
        override val text: String,  // @<key>
        override val range: Pair<Int, Int>,
        val key: String
    ) : Token()

    // Planning and datetime
    data class Scheduled(
        override val text: String = "SCHEDULED:",
        override val range: Pair<Int, Int>
    ) : Token()

    data class Deadline(
        override val text: String = "DEADLINE:",
        override val range: Pair<Int, Int>
    ) : Token()

    data class Closed(
        override val text: String = "CLOSED:",
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
        override val text: String = "--",
        override val range: Pair<Int, Int>
    ) : Token()

    data class TableColumnSep(
        override val text: String = "|",
        override val range: Pair<Int, Int>
    ) : Token()

    data class TableRowSep(
        override val text: String = "-",
        override val range: Pair<Int, Int>
    ) : Token()

    data class TableIntersection(
        override val text: String = "+",
        override val range: Pair<Int, Int>
    ) : Token()

    // This also handles header
    data class TableCell(
        override val text: String,
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkStart(
        override val text: String = "[[",
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkEnd(
        override val text: String = "]]",
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkTitleSep(
        override val text: String = "][",
        override val range: Pair<Int, Int>
    ) : Token()

    data class LinkUrl(
        override val text: String,
        override val range: Pair<Int, Int>,
    ) : Token()

    data class HorizontalRule(
        override val text: String = "-----",
        override val range: Pair<Int, Int>
    ) : Token()

    data class CommentStart(
        override val text: String = "#",
        override val range: Pair<Int, Int>
    ) : Token()

    data class Text(
        override val text: String,
        override val range: Pair<Int, Int>,
    ) : Token()

    // Unparseable text
    data class Error(
        override val text: String,
        override val range: Pair<Int, Int>,
        val message: String
    ) : Token()
}

/**
 * Convert a list of tokens to String, primarily for debugging.
 */
fun inverseLex(tokens: List<Token>): String {
    val strings = mutableListOf<String>()

    for (token in tokens) {
        if (token is Token.SOF || token is Token.EOF) {
            continue
        }

        strings.add(token.text)
    }

    return strings.joinToString("")
}

fun compareStrings(a: String, b: String): String {
    val lines = mutableListOf<String>()

    if (a == b) {
        return "Both strings are same"
    }

    if (a.length != b.length) {
        lines.add("Length difference: ${a.length} vs ${b.length}")
    }

    var i = 0
    while (i < max(a.length, b.length)) {
        val ca: Char?
        val cb: Char?
        if (i < a.length) {
            ca = a[i]
        } else {
            ca = null
        }

        if (i < b.length) {
            cb = b[i]
        } else {
            cb = null
        }

        if (ca != cb) {
            val contextA = a.substring(i - 10, i + 10)
            val contextB = b.substring(i - 10, i + 10)
            lines.add("\"$contextA\" vs \"$contextB\"")
            break
        }
        i++
    }

    return lines.joinToString("\n")
}

/**
 * Org mode lexer using simple FSM and tokens from above
 */
class OrgLexer(private val input: String) {
    private var currentPos = 0
    private var scannedPos = 0

    // For debugging and Error tokens
    private var currentLine = 0
    private var nConsecutiveErrors = 0

    private var TODOTodoWords = listOf("TODO")
    private var TODODoneWords = listOf("DONE")
    private var priorityIndicators = listOf("A", "B", "C")

    // State management variables
    private var reachedEOF = false
    private var listNesting = mutableListOf<Token>()
    private var blockNesting = mutableListOf<Token>()
    private var inPropDrawer = false
    private var inPreface = true
    private var inParagraph = false
    private var inHeadline = false

    // Accumulator
    private val tokens: MutableList<Token> = mutableListOf(Token.SOF())

    /**
     * Look ahead till we find the regex match.
     */
    private fun lookaheadTill(regex: Regex): MatchResult? {
        return regex.find(input, currentPos)
    }

    /**
     * Look ahead for the pattern at the current point
     */
    private fun lookahead(regex: Regex): MatchResult? {
        return regex.matchAt(input, currentPos)
    }

    private fun consumeSpace() {
        scannedPos = currentPos + 1
        tokens.add(Token.Space(range = Pair(currentPos, scannedPos)))
        currentPos = scannedPos
    }

    private fun consumeTab() {
        scannedPos = currentPos + 1
        tokens.add(Token.Tab(range = Pair(currentPos, scannedPos)))
        currentPos = scannedPos
    }

    /**
     * Consume contiguous sequence of spaces and tabs
     */
    private fun consumeSpacesAndTabs() {
        var char = input[currentPos]

        while (true) {
            when (char) {
                ' ' -> consumeSpace()
                '\t' -> consumeTab()
                else -> break
            }
            char = input[currentPos]
        }
    }

    private fun consumePropertyValue() {
        consumeSpacesAndTabs()
        val match = lookaheadTill(Regex("(\n|\$)"))

        if (match == null) {
            consumeError("Property value", skip = 1)
            return
        }

        scannedPos = match.range.first
        val text = input.substring(currentPos, scannedPos)
        tokens.add(Token.DrawerPropertyValue(
            text = text,
            range = Pair(currentPos, scannedPos),
            value = text.trim()
        ))
        currentLine = scannedPos
    }

    private fun consumeNCharsAsText(n: Int) {
        scannedPos = currentPos + n
        val text = input.substring(currentPos, scannedPos)
        tokens.add(Token.Text(text, range = Pair(currentPos, scannedPos)))
        currentPos = scannedPos
    }

    /**
     * Consume characters using skip count and return an Error token with details about current
     * location and the object being parsed.
     */
    private fun consumeError(parsedObject: String, skip: Int = 0) {
        scannedPos = currentPos + skip
        val text = input.substring(currentPos, scannedPos)

        if (tokens.last() !is Token.Error) {
            nConsecutiveErrors = 0
        }

        tokens.add(Token.Error(
            text = text,
            range = Pair(currentPos, scannedPos),
            message = "Error in parsing ${parsedObject} in line number ${currentLine} at (overall) position ${currentPos}"
        ))

        nConsecutiveErrors++
        currentLine = scannedPos
    }

    private fun consumeCommentLine() {
        scannedPos = currentPos + 1
        tokens.add(Token.CommentStart(range = Pair(currentPos, scannedPos)))
        currentPos = scannedPos
        consumeSpacesAndTabs()

        // We take the rest of the line as single text token
        val match = lookaheadTill(Regex("(\n|\$)"))

        if (match == null) {
            consumeError("Comment line", skip = 1)
            return
        }

        scannedPos = match.range.first
        tokens.add(Token.Text(
            text = input.substring(currentPos, scannedPos),
            range = Pair(currentPos, scannedPos)
        ))
        currentLine = scannedPos
    }

    fun tokenize(): List<Token> {

        while (!reachedEOF) {
            val char = input[currentPos]
            val lastToken = tokens[tokens.lastIndex]
            val atLineStart = (lastToken is Token.LineBreak) || (lastToken is Token.SOF)

            // Consecutive error could be because of skips in malformed org mode text
            if (nConsecutiveErrors > 5) {
                return tokens
            }

            when (char) {
                ' ' -> {
                    if (inPropDrawer) {
                        consumePropertyValue()
                    } else {
                        consumeSpace()
                        consumeSpacesAndTabs()
                    }
                }

                '\n' -> {
                    scannedPos = currentPos + 1
                    tokens.add(Token.LineBreak(range = Pair(currentPos, scannedPos)))
                    currentLine++
                    if (atLineStart) {
                        // Double break is a change of paragraph. Also works when we are at SOF
                        inParagraph = false
                    }
                }

                '\t' -> {
                    if (inPropDrawer) {
                        consumePropertyValue()
                    } else {
                        consumeTab()
                        consumeSpacesAndTabs()
                    }
                }

                '*' -> {
                    if (atLineStart) {
                        // This could be heading (we don't support * list) or general text
                        if (!inParagraph) {
                            val match = lookahead(Regex("\\*+"))
                            if (match == null) {
                                consumeError("Heading Star", skip = 1)
                            } else {
                                val matchText = match.value
                                scannedPos = currentPos + matchText.length

                                tokens.add(
                                    Token.HeadingStars(
                                        matchText,
                                        range = Pair(currentPos, scannedPos),
                                        level = matchText.length
                                    )
                                )
                            }

                            inHeadline = true
                            // Once we hit a headline, we stop being in preface forever
                            inPreface = false
                        } else {
                            scannedPos = currentPos + 1
                            tokens.add(
                                Token.EmphasisDelimiter(
                                    text = "*",
                                    range = Pair(currentPos, scannedPos),
                                    type = Token.EmphasisType.BOLD
                                )
                            )
                        }
                    } else {
                        // Just read it as an emphasis character
                        scannedPos = currentPos + 1
                        tokens.add(
                            Token.EmphasisDelimiter(
                                text = "*",
                                range = Pair(currentPos, scannedPos),
                                type = Token.EmphasisType.BOLD
                            )
                        )
                    }
                }

                ':' -> {
                    if (atLineStart) {
                        // This could be drawer stuff or fixed width line, or plain colon
                        var match = lookahead(Regex(":PROPERTIES:", RegexOption.IGNORE_CASE))
                        if (match != null) {
                            scannedPos = currentPos + match.value.length
                            tokens.add(
                                Token.DrawerStart(
                                    text = match.value,
                                    range = Pair(currentPos, scannedPos),
                                    type = Token.DrawerType.PROPERTIES
                                )
                            )
                            inPropDrawer = true
                        } else {
                            match = lookahead(Regex(":END:", RegexOption.IGNORE_CASE))
                            if (match != null) {
                                scannedPos = currentPos + match.value.length
                                tokens.add(
                                    Token.DrawerEnd(
                                        range = Pair(currentPos, scannedPos)
                                    )
                                )
                                inPropDrawer = false
                            } else {
                                if (inPropDrawer) {
                                    match = lookahead(
                                        Regex(":[a-zA-Z0-9_]+:", RegexOption.IGNORE_CASE)
                                    )
                                    if (match == null) {
                                        consumeError("Property Key", skip = 1)
                                    } else {
                                        scannedPos = currentPos + match.value.length
                                        tokens.add(
                                            Token.DrawerPropertyKey(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                key = match.value.substring(
                                                    1, match.value.length - 1
                                                )
                                            )
                                        )
                                    }
                                } else {
                                    // TODO: Handle FixedWidthLineStart here, or do that in the parser I guess
                                    scannedPos = currentPos + 1
                                    tokens.add(Token.Colon(
                                        range = Pair(currentPos, scannedPos)
                                    ))
                                }
                            }
                        }
                    } else {
                        // This could be description list sep or plain colon, or tags string
                        if (inHeadline) {
                            val match = lookahead(Regex(":([a-zA-Z]+:)+"))
                            if (match != null) {
                                val tags = match.value.split(":")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                scannedPos = currentPos + match.value.length
                                tokens.add(Token.TagString(
                                    text = match.value,
                                    range = Pair(currentPos, scannedPos),
                                    tags = tags
                                ))
                                // Tags are the last thing in a heading line
                                inHeadline = false
                            } else {
                                scannedPos = currentPos + 1
                                tokens.add(Token.Colon(range = Pair(currentPos, scannedPos)))
                            }
                        } else {
                            if (!listNesting.isEmpty() && listNesting.last() is Token.UnorderedListMarker) {
                                // We could've hit a description list
                                if (lastToken is Token.Space && lookahead(Regex(":: ")) != null
                                ) {
                                    scannedPos = currentPos + 2
                                    tokens.add(Token.DescriptionListSep(
                                        range = Pair(currentPos, scannedPos)
                                    ))
                                } else {
                                    scannedPos = currentPos + 1
                                    tokens.add(Token.Colon(range = Pair(currentPos, scannedPos)))
                                }
                            } else {
                                scannedPos = currentPos + 1
                                tokens.add(Token.Colon(range = Pair(currentPos, scannedPos)))
                            }
                        }
                    }
                }
                '#' -> {
                    if (atLineStart) {
                        // This could be comment or keyword, block marker, or just plain text
                        if (lookahead(Regex("# ")) != null) {
                            consumeCommentLine()
                        } else {
                            // Keyword or general block
                            var match = lookahead(Regex("#\\+([a-zA-Z_]+):"))
                            if (match != null) {
                                when (match.groupValues[1].uppercase()) {
                                    "TITLE" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.TITLE
                                        ))
                                    }
                                    "AUTHOR" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.AUTHOR
                                        ))
                                    }
                                    "EMAIL" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.EMAIL
                                        ))
                                    }
                                    "DATE" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.DATE
                                        ))
                                    }
                                    "SUBTITLE" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.SUBTITLE
                                        ))
                                    }
                                    "DESCRIPTION" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.DESCRIPTION
                                        ))
                                    }
                                    "KEYWORDS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.KEYWORDS
                                        ))
                                    }
                                    "CATEGORY" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.CATEGORY
                                        ))
                                    }
                                    "FILETAGS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.FILETAGS
                                        ))
                                    }
                                    "EXPORT_SELECT_TAGS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.EXPORT_SELECT_TAGS
                                        ))
                                    }
                                    "EXPORT_FILE_NAME" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.EXPORT_FILE_NAME
                                        ))
                                    }
                                    "EXPORT_EXCLUDE_TAGS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.EXPORT_EXCLUDE_TAGS
                                        ))
                                    }
                                    "TAGS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.TAGS
                                        ))
                                    }
                                    "LANGUAGE" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.LANGUAGE
                                        ))
                                    }
                                    "STARTUP" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.STARTUP
                                        ))
                                    }
                                    "TODO" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.TODO
                                        ))
                                    }
                                    "TYP_TODO" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.TYP_TODO
                                        ))
                                    }
                                    "SEQ_TODO" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.SEQ_TODO
                                        ))
                                    }
                                    "PROPERTY" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.PROPERTY
                                        ))
                                    }
                                    "PILE" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.PILE
                                        ))
                                    }
                                    "TOC" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.TOC
                                        ))
                                    }
                                    "OPTIONS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.OPTIONS
                                        ))
                                    }
                                    "LATEX_HEADER" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.LATEX_HEADER
                                        ))
                                    }
                                    "HTML_HEAD" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.HTML_HEAD
                                        ))
                                    }
                                    "PRIORITIES" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.PRIORITIES
                                        ))
                                    }
                                    "CITE_EXPORT" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.FileKeyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.FileKeywordType.CITE_EXPORT
                                        ))
                                    }
                                    // Other keywords
                                    "ATTR_HTML" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.ATTR_HTML
                                        ))
                                    }
                                    "NAME" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.NAME
                                        ))
                                    }
                                    "HTML" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.HTML
                                        ))
                                    }
                                    "CAPTION" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.CAPTION
                                        ))
                                    }
                                    "BIBLIOGRAPHY" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.BIBLIOGRAPHY
                                        ))
                                    }
                                    "TBLFM" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.TBLFM
                                        ))
                                    }
                                    "CONSTANTS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.CONSTANTS
                                        ))
                                    }
                                    "RESULTS" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.Keyword(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos),
                                            type = Token.KeywordType.RESULTS
                                        ))
                                    }
                                    // Generic block stuff
                                    "BEGIN" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.GenericBlockStart(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos)
                                        ))
                                    }
                                    "END" -> {
                                        scannedPos = match.range.last + 1
                                        tokens.add(Token.GenericBlockEnd(
                                            text = match.value,
                                            range = Pair(currentPos, scannedPos)
                                        ))
                                    }
                                    else -> {
                                        consumeError("Keyword", skip = match.value.length)
                                    }
                                }
                            } else {
                                // Blocks
                                match = lookahead(Regex("#\\+BEGIN_([a-zA-Z_\\-]+)", RegexOption.IGNORE_CASE))
                                if (match != null) {
                                    when (match.groupValues[1].uppercase()) {
                                        "COMMENT" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.COMMENT
                                            ))
                                        }
                                        "EXAMPLE" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.EXAMPLE
                                            ))
                                        }
                                        "SRC" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.SRC
                                            ))
                                        }
                                        "QUOTE" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.QUOTE
                                            ))
                                        }
                                        "CENTER" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.CENTER
                                            ))
                                        }
                                        "HTML" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.HTML
                                            ))
                                        }
                                        "VERSE" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.VERSE
                                            ))
                                        }
                                        "LATEX" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.LATEX
                                            ))
                                        }

                                        // Custom blocks
                                        "PAGE-INTRO" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.PAGE_INTRO
                                            ))
                                        }
                                        "EDITS" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.EDITS
                                            ))
                                        }
                                        "ASIDE" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.ASIDE
                                            ))
                                        }
                                        "VIDEO" -> {
                                            scannedPos = match.range.last + 1
                                            tokens.add(Token.BlockStart(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                type = Token.BlockType.VIDEO
                                            ))
                                        }
                                        else -> {
                                            consumeError("Block Start", skip = match.value.length)
                                        }
                                    }
                                } else {
                                    match = lookahead(Regex("#\\+END_([a-zA-Z_\\-]+)", RegexOption.IGNORE_CASE))
                                    if (match != null) {
                                        when (match.groupValues[1].uppercase()) {
                                            "COMMENT" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.COMMENT
                                                    )
                                                )
                                            }

                                            "EXAMPLE" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.EXAMPLE
                                                    )
                                                )
                                            }

                                            "SRC" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.SRC
                                                    )
                                                )
                                            }

                                            "QUOTE" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.QUOTE
                                                    )
                                                )
                                            }

                                            "CENTER" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.CENTER
                                                    )
                                                )
                                            }

                                            "HTML" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.HTML
                                                    )
                                                )
                                            }

                                            "VERSE" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.VERSE
                                                    )
                                                )
                                            }

                                            "LATEX" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.LATEX
                                                    )
                                                )
                                            }

                                            // Custom blocks
                                            "PAGE-INTRO" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.PAGE_INTRO
                                                    )
                                                )
                                            }

                                            "EDITS" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.EDITS
                                                    )
                                                )
                                            }

                                            "ASIDE" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.ASIDE
                                                    )
                                                )
                                            }

                                            "VIDEO" -> {
                                                scannedPos = match.range.last + 1
                                                tokens.add(
                                                    Token.BlockEnd(
                                                        text = match.value,
                                                        range = Pair(currentPos, scannedPos),
                                                        type = Token.BlockType.VIDEO
                                                    )
                                                )
                                            }
                                            else -> {
                                                consumeError("Block End", skip = match.value.length)
                                            }
                                        }
                                    } else {
                                        scannedPos = currentPos + 1
                                        tokens.add(Token.Text(
                                            text = "#",
                                            range = Pair(currentPos, scannedPos)
                                        ))
                                    }
                                }
                            }
                        }
                    } else {
                        // This is plain text
                        // We don't support #tag
                        scannedPos = currentPos + 1
                        tokens.add(Token.Text(
                            text = "#",
                            range = Pair(currentPos, scannedPos)
                        ))
                    }
                }
                ';' -> {
                    scannedPos = currentPos + 1
                    tokens.add(Token.SemiColon(
                        range = Pair(currentPos, scannedPos)
                    ))
                }
                '"' -> {
                    scannedPos = currentPos + 1
                    tokens.add(Token.QuotationMark(
                        range = Pair(currentPos, scannedPos)
                    ))
                }
                'S' -> {
                    if (atLineStart) {
                        val match = lookahead(Regex("SCHEDULED:"))
                        if (match != null) {
                            scannedPos = currentPos + match.value.length
                            tokens.add(
                                Token.Scheduled(
                                    range = Pair(currentPos, scannedPos)
                                )
                            )
                        } else {
                            consumeNCharsAsText(1)
                        }
                    } else {
                        consumeNCharsAsText(1)
                    }
                }
                'D' -> {
                    if (atLineStart) {
                        val match = lookahead(Regex("DEADLINE:"))
                        if (match != null) {
                            scannedPos = currentPos + match.value.length
                            tokens.add(
                                Token.Deadline(
                                    range = Pair(currentPos, scannedPos)
                                )
                            )
                        } else {
                            consumeNCharsAsText(1)
                        }
                    } else {
                        consumeNCharsAsText(1)
                    }
                }
                'C' -> {
                    if (atLineStart) {
                        val match = lookahead(Regex("CLOSED:"))
                        if (match != null) {
                            scannedPos = currentPos + match.value.length
                            tokens.add(
                                Token.Closed(
                                    range = Pair(currentPos, scannedPos)
                                )
                            )
                        } else {
                            consumeNCharsAsText(1)
                        }
                    } else {
                        consumeNCharsAsText(1)
                    }
                }
                else -> {
                    val match = lookaheadTill(Regex("\\s"))
                    if (match == null) {
                        consumeError("Text", skip = 1)
                    } else {
                        scannedPos = match.range.first
                        tokens.add(
                            Token.Text(
                                input.substring(currentPos, scannedPos),
                                range = Pair(currentPos, scannedPos)
                            )
                        )
                    }
                }
            }
            currentPos = scannedPos

            if (currentPos > input.length - 1) {
                reachedEOF = true
            }
        }
        tokens.add(Token.EOF(range = Pair(currentPos, currentPos)))
        return tokens.toList()
    }
}