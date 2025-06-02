package com.example.pile.orgmode

import java.time.LocalDate
import java.time.LocalTime

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
        override val text: String = "RESULTS:",
        override val range: Pair<Int, Int>,
        val type: BlockType
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
 * Org mode lexer using simple FSM and tokens from above
 */
class OrgLexer(private val input: String) {
    private var currentPos = 0
    private var scannedPos = 0
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

    fun tokenize(): List<Token> {
        val tokens: MutableList<Token> = mutableListOf(Token.SOF())

        while (!reachedEOF) {
            val char = input[currentPos]
            val lastToken = tokens[tokens.lastIndex]
            val atLineStart = (lastToken is Token.LineBreak) || (lastToken is Token.SOF)

            when (char) {
                ' ' -> {
                    scannedPos = currentPos + 1
                    tokens.add(Token.Space(range = Pair(currentPos, scannedPos)))
                }

                '\n' -> {
                    scannedPos = currentPos + 1
                    tokens.add(Token.LineBreak(range = Pair(currentPos, scannedPos)))
                    if (atLineStart) {
                        // Double break is a change of paragraph. Also works when we are at SOF
                        inParagraph = false
                    }
                }

                '\t' -> {
                    scannedPos = currentPos + 1
                    tokens.add(Token.Tab(range = Pair(currentPos, scannedPos)))
                }

                '*' -> {
                    if (atLineStart) {
                        // This could be heading (we don't support * list) or general text
                        if (!inParagraph) {
                            val match = lookahead(Regex("\\*+"))
                            val matchText = match!!.value
                            scannedPos = currentPos + matchText.length

                            tokens.add(
                                Token.HeadingStars(
                                    matchText,
                                    range = Pair(currentPos, scannedPos),
                                    level = matchText.length
                                )
                            )
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
                        // Just read it as emphasis thing
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
                        // This could be drawer stuff or fixedwidth line, or plain colon
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
                                    if (match != null) {
                                        scannedPos = currentPos + match.value.length
                                        tokens.add(
                                            Token.DrawerPropertyKey(
                                                text = match.value,
                                                range = Pair(currentPos, scannedPos),
                                                key = match.value.substring(
                                                    1, match.value.length - 2
                                                )
                                            )
                                        )
                                    } else {
                                        scannedPos = currentPos + 1
                                        if (lookahead(Regex(" ")) != null) {
                                            tokens.add(Token.FixedWidthLineStart(
                                                range = Pair(currentPos, scannedPos)
                                            ))
                                        } else {
                                            tokens.add(Token.Colon(
                                                range = Pair(currentPos, scannedPos)
                                            ))
                                        }
                                    }
                                } else {
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
                                // We could hit a description list
                                if (lastToken is Token.Space && lookahead(
                                        Regex(":: ")
                                    ) != null
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

                else -> {
                    val match = lookaheadTill(Regex("\\s"))
                    scannedPos = match!!.range.first

                    tokens.add(
                        Token.Text(
                            input.substring(currentPos, scannedPos),
                            range = Pair(currentPos, scannedPos)
                        )
                    )
                }
            }
            currentPos = scannedPos

            if (currentPos >= input.length - 1) {
                reachedEOF = true
            }
        }
        tokens.add(Token.EOF(range = Pair(currentPos, currentPos)))
        return tokens.toList()
    }
}