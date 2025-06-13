package com.example.pile.orgmode

import android.util.Log

val parseProperties: Parser<OrgProperties> = seq(
    ::matchToken { it is Token.DrawerStart },
    matchLineBreak,
    oneOrMore(
        seq(
            ::matchToken { it is Token.DrawerPropertyKey },
            zeroOrMore(matchSpace),
            ::matchToken { it is Token.DrawerPropertyValue },
            matchLineBreak
        )
    ),
    ::matchToken { it is Token.DrawerEnd }
).map { (ds, lb, propLines, de) ->
    var map = mutableMapOf<String, OrgLine>()

    for ((k, _, v, _) in propLines) {
        val keyString = (k.tokens.first() as Token.DrawerPropertyKey).key
        val valueString = (v.tokens.first() as Token.DrawerPropertyValue).value

        map[keyString] = OrgLine(
            items = listOf(
                OrgInlineElem.Text(
                    valueString,
                    tokens = v.tokens
                )
            ),
            tokens = v.tokens
        )
    }

    OrgProperties(
        map = map,
        tokens = collectTokens(Tuple4(ds, lb, propLines, de))
    )
}

val parseOrgLine = collectUntil { it is Token.LineBreak || it is Token.EOF }
    .map { tokens ->
        OrgLine(
            items = listOf(
                OrgInlineElem.Text(
                    tokens.joinToString("") { it.text },
                    tokens = tokens
                )),
            tokens = tokens
        )
    }

val parseLine: Parser<String> = collectUntil { it is Token.LineBreak || it is Token.EOF }
    .map { tokens -> tokens.joinToString("") { it.text } }

val parseFileKeyword: Parser<Pair<OrgToken, OrgLine>> = seq(
    ::matchToken { it is Token.FileKeyword },
    matchSpaces,
    parseOrgLine
).map { (k, _, line) -> Pair(k, line) }

fun orgLineToTags(line: OrgLine): OrgTags {
    val rawText = line.items
        .filter { it is OrgInlineElem.Text }
        .joinToString("") { (it as OrgInlineElem.Text).text }

    return OrgTags(
        tags = rawText.split(",").map { it.trim() }.filter { it.isNotBlank() },
        tokens = line.tokens
    )
}

/**
 * Preamble is everything before the start of actual content.
 *
 * We don't allow empty lines here
 */
val parsePreamble: Parser<OrgPreamble> = seq(
    maybe(parseProperties),
    oneOrMore(matchLineBreak),
    oneOrMore(seq(parseFileKeyword, matchLineBreak)),
    zeroOrMore(matchLineBreak)
).map { (props, lbs, keywordLines, lbsEnd) ->
    // We need to interpret all the file keywords
    var title: OrgLine? = null
    var tags: OrgTags? = null

    for ((kwMatch, _) in keywordLines) {
        val token = kwMatch.first.tokens[0] as Token.FileKeyword
        val valueLine = kwMatch.second

        when (token.type) {
            Token.FileKeywordType.TITLE -> title = valueLine
            Token.FileKeywordType.TAGS -> tags = orgLineToTags(valueLine)
            else -> { }
        }
    }

    if (title == null) {
        println("Unable to parse Title")
    }

    OrgPreamble(
        title = title ?: OrgLine(emptyList(), tokens = emptyList()),
        tags = tags,
        tokens = collectTokens(Tuple4(props, lbs, keywordLines, lbsEnd)),
        properties = props
    )
}

val parseLevel: Parser<OrgHeadingLevel> = matchToken {
    it is Token.HeadingStars
}.map { output ->
    OrgHeadingLevel(
        level = (output.tokens.first() as Token.HeadingStars).level,
        tokens = output.tokens
    )
}

// TODO: Fix this to handle tags
val parseHeadingTitle: Parser<OrgLine> = parseOrgLine

val parseHeading: Parser<OrgHeading> = seq(
    parseLevel,
    matchSpace,
    // maybe(::parseTODOState),
    // maybe(::parsePriority),
    parseHeadingTitle,
    oneOrMore(matchLineBreak),
    // maybe(::parseHeadingTags),
    // ::parsePlanningInfo,
    // maybe(::parseProperties)
).map { (level, sp, title, lbs) ->
    OrgHeading(
        level = level,
        title = title,
        tokens = collectTokens(Tuple4(level, sp, title, lbs))
    )
}

val parseHorizontalRule: Parser<OrgChunk.OrgHorizontalLine> = matchToken {
    it is Token.HorizontalRule
}.map { output ->
    OrgChunk.OrgHorizontalLine(tokens = output.tokens)
}

// ul(i=0) = 1+
//             matchindent(i=0), marker, space, (checkbox, space)?,
//                                                oneof
//                                                   1. paragraph(i=<+1>) ... other chunks, <...>
//                                                   2. ul(i=<+1>)
//                                                   3. ol(i=<+1>)

fun unorderedList(indentLevel: Int = 0): Parser<OrgList.OrgUnorderedList> {
    return oneOrMore(
        seq(
            // Unordered lists have a fixed indent mapping of x2
            ::matchToken { it is Token.UnorderedListMarker && it.nIndent == indentLevel * 2 },
            matchSpace,
            maybe(seq(matchToken { it is Token.CheckBox }, matchSpace)),
            // Maybe take out list content as a separate parser. This will also help me in ordered
            // list parsing.
            oneOrMore(
                seq(
                    oneOf(
                        lazy { unorderedList(indentLevel + 1) },
                        parseOrderedList,
                        // I need paragraph to also be parsed in an indented manner
                        parseParagraph
                    ),
                    zeroOrMore(matchLineBreak)
                )
            )
        )
    ).map { listItems ->
        val markerTok = listItems.first().first
        val markerStyle = when (((markerTok.tokens[0]) as Token.UnorderedListMarker).style) {
            Token.UnorderedListMarkerStyle.DASH -> OrgUnorderedListMarker.DASH
            Token.UnorderedListMarkerStyle.PLUS -> OrgUnorderedListMarker.PLUS
        }

        var items: MutableList<OrgList.OrgListItem> = mutableListOf()

        for ((marker, _, cb, chunks) in listItems) {
            Log.d("LIST PARSING", (marker.tokens[0] as Token.UnorderedListMarker).toString())

            val checkbox = if (cb == null) {
                null
            } else {
                when ((cb.first.tokens[0] as Token.CheckBox).state) {
                    Token.CheckBoxState.UNCHECKED -> OrgListCheckState.UNCHECKED
                    Token.CheckBoxState.CHECKED -> OrgListCheckState.CHECKED
                    Token.CheckBoxState.PARTIAL -> OrgListCheckState.PARTIAL
                }
            }
            items.add(
                OrgList.OrgListItem(
                    content = chunks.map { it.first as OrgChunk },
                    checkbox = checkbox,
                    tokens = collectTokens(chunks)
                )
            )
        }

        val list = OrgList.OrgUnorderedList(
            markerStyle = markerStyle,
            items = items,
            tokens = collectTokens(listItems)
        )

        Log.d("LIST PARSING", "Final list is : ${list.items}")
        list
    }
}

fun parseUnorderedList(tokens: List<Token>, pos: Int): ParsingResult<OrgList.OrgUnorderedList> {
    return unorderedList(0).invoke(tokens, pos)
}

val parseOrderedList : Parser<OrgList.OrgOrderedList> = oneOrMore(seq(
    // No indent matching, assuming things to be indented at 0
    ::matchToken { it is Token.OrderedListMarker },
    matchSpace,
    maybe(seq(matchToken { it is Token.CheckBox }, matchSpace)),
    parseOrgLine,
    oneOf(matchLineBreak, matchEOF)
)).map { listItems ->
    val markerTok = listItems.first().first
    val markerStyle = when(((markerTok.tokens[0]) as Token.OrderedListMarker).style) {
        Token.OrderedListMarkerStyle.PARENTHESIS -> OrgOrderedListMarker.PARENTHESIS
        Token.OrderedListMarkerStyle.PERIOD -> OrgOrderedListMarker.PERIOD
    }

    var items: MutableList<OrgList.OrgListItem> = mutableListOf()

    for ((_, _, cb, line, _) in listItems) {
        val checkbox = if (cb == null) {
            null
        } else {
            when ((cb.first.tokens[0] as Token.CheckBox).state) {
                Token.CheckBoxState.UNCHECKED -> OrgListCheckState.UNCHECKED
                Token.CheckBoxState.CHECKED -> OrgListCheckState.CHECKED
                Token.CheckBoxState.PARTIAL -> OrgListCheckState.PARTIAL
            }
        }
        items.add(OrgList.OrgListItem(
            content = listOf(OrgChunk.OrgParagraph(items = line.items, tokens = line.tokens)),
            checkbox = checkbox,
            tokens = line.tokens
        ))
    }

    OrgList.OrgOrderedList(
        markerStyle = markerStyle,
        items = items,
        tokens = collectTokens(listItems)
    )
}

val parseParagraph: Parser<OrgChunk.OrgParagraph> = Parser { tokens, pos ->
    if (pos >= tokens.size) {
        return@Parser parsingError("Exhausted tokens while parsing paragraph")
    }

    // Stopping condition for paragraph parsing
    fun shouldStop(position: Int): Boolean {
        val token = tokens[position]

        return when (token) {
            is Token.EOF,
            is Token.UnorderedListMarker,
            is Token.OrderedListMarker,
            is Token.HeadingStars  -> true
            is Token.BlockEnd -> when (token.type) {
                Token.BlockType.ASIDE,
                Token.BlockType.SRC,
                Token.BlockType.PAGE_INTRO,
                Token.BlockType.EDITS,
                Token.BlockType.QUOTE -> true
                else -> false
            }
            is Token.BlockStart -> when (token.type) {
                Token.BlockType.ASIDE,
                Token.BlockType.SRC,
                Token.BlockType.PAGE_INTRO,
                Token.BlockType.EDITS,
                Token.BlockType.QUOTE -> true
                else -> false
            }
            else -> false
        }
    }

    var accumulator = mutableListOf<Token>()
    var currentPos = pos
    var lbCount = 0
    var tok: Token

    while (currentPos < tokens.size) {
        tok = tokens[currentPos]

        if (shouldStop(currentPos)) {
            break
        }

        lbCount = if (tok is Token.LineBreak) { lbCount + 1 } else { 0 }
        if (lbCount == 2) {
            accumulator.dropLast(1)
            break
        }

        accumulator.add(tokens[currentPos])
        currentPos++
    }

    if (accumulator.isNotEmpty()) {
        // Currently taking all raw texts from tokens and throwing them as a single text
        ParsingResult.Success(
            output = OrgChunk.OrgParagraph(
                items = accumulator.map { tok -> OrgInlineElem.Text(tok.text, tokens = listOf(tok)) },
                tokens = accumulator
            ),
            nextPos = currentPos
        )
    } else {
        parsingError("Unable to parse paragraph because of lack of tokens", tokens = listOf(tokens[pos]))
    }
}

val parseSourceBlock: Parser<OrgBlock.OrgSourceBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.SRC },
    matchSpace,
    parseLine,
    matchLineBreak,
    collectUntil { it is Token.BlockEnd && it.type == Token.BlockType.SRC },
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.SRC }
).map { (start, sp, configLine, lb, tokens, end) ->
    // TODO: Fix token collection for plain line
    val allTokens = collectTokens(Tuple5(start, sp, lb, tokens, end))

    OrgBlock.OrgSourceBlock(
        language = configLine,
        switches = emptyList(),
        headerArgs = emptyList(),
        name = null,
        body = tokens.dropLast(1).joinToString("") { tok -> tok.text },
        tokens = allTokens
    )
}

// TODO: Parse more chunks
val parseQuoteBlock: Parser<OrgBlock.OrgQuoteBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.QUOTE },
    matchLineBreak,
    oneOrMore(seq(
        oneOf(
            // ::parseCommentLine,
            parseHorizontalRule,
            parseSourceBlock,
            ::parseUnorderedList,
            parseOrderedList,
            parseParagraph
        ),
        zeroOrMore(matchLineBreak)
    )),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.QUOTE }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(Tuple4(start, lb, chunks, end))

    OrgBlock.OrgQuoteBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parsePageIntroBlock: Parser<OrgBlock.OrgPageIntroBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.PAGE_INTRO },
    matchLineBreak,
    oneOrMore(seq(
        oneOf(
            // ::parseCommentLine,
            parseHorizontalRule,
            parseSourceBlock,
            parseQuoteBlock,
            ::parseUnorderedList,
            parseOrderedList,
            parseParagraph
        ),
        zeroOrMore(matchLineBreak)
    )),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.PAGE_INTRO }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(Tuple4(start, lb, chunks, end))

    OrgBlock.OrgPageIntroBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parseAsideBlock: Parser<OrgBlock.OrgAsideBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.ASIDE },
    matchLineBreak,
    oneOrMore(seq(
        oneOf(
            // ::parseCommentLine,
            parseHorizontalRule,
            // ::parseTable,
            // ::parseCommentBlock,
            // ::parseExampleBlock,
            parseSourceBlock,
            parseQuoteBlock,
            // ::parseCenterBlock,
            // ::parseHTMLBlock,
            // ::parseVerseBlock,
            // ::parseLaTeXBlock
            // ::parseVideoBlock,
            ::parseUnorderedList,
            parseOrderedList,
            parseParagraph
        ),
        zeroOrMore(matchLineBreak)
    )),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.ASIDE }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(Tuple4(start, lb, chunks, end))

    OrgBlock.OrgAsideBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parseEditsBlock: Parser<OrgBlock.OrgEditsBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.EDITS },
    matchLineBreak,
    oneOrMore(seq(
        oneOf(
            // ::parseCommentLine,
            parseHorizontalRule,
            parseSourceBlock,
            parseQuoteBlock,
            ::parseUnorderedList,
            parseOrderedList,
            parseParagraph
        ),
        zeroOrMore(matchLineBreak)
    )),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.EDITS }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(Tuple4(start, lb, chunks, end))

    OrgBlock.OrgEditsBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parseChunk: Parser<OrgChunk> = seq(
    oneOf(
        // ::parseCommentLine,
        parseHorizontalRule,
        // ::parseTable,
        // ::parseCommentBlock,
        // ::parseExampleBlock,
        parseSourceBlock,
        parseQuoteBlock,
        // ::parseCenterBlock,
        // ::parseHTMLBlock,
        // ::parseVerseBlock,
        // ::parseLaTeXBlock,
        parsePageIntroBlock,
        parseEditsBlock,
        parseAsideBlock,
        // ::parseVideoBlock,
        ::parseUnorderedList,
        parseOrderedList,
        parseParagraph
    ),
    zeroOrMore(matchLineBreak)
).map { (chunk, lbs) ->
    chunk as OrgChunk
    // We are missing the tokens from lbs here
}

val parsePreface: Parser<OrgPreface> = zeroOrMore(parseChunk).map { output ->
    OrgPreface(
        body = output,
        tokens = collectTokens(output)
    )
}

val parseSection: Parser<OrgSection> = seq(
    parseHeading,
    zeroOrMore(parseChunk)
).map { (heading, chunks) ->
    OrgSection(
        heading = heading,
        body = chunks,
        tokens = collectTokens(Pair(heading, chunks))
    )
}

val parseDocument: Parser<OrgDocument> = seq(
    matchSOF,
    parsePreamble,
    parsePreface,
    zeroOrMore(parseSection),
    matchEOF
).map { (sof, preamble, preface, sections, eof) ->
    OrgDocument(
        preamble = preamble,
        preface = preface,
        content = sections,
        tokens = collectTokens(Tuple5(sof, preamble, preface, sections, eof))
    )
}

/**
 * The main exposed function for parsing an org mode document
 */
fun parse(tokens: List<Token>): OrgDocument? {
    val result = parseDocument.invoke(tokens, pos = 0)
    return if (result is ParsingResult.Success) {
        result.output
    } else {
        null
    }
}