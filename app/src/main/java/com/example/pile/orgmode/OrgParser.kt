package com.example.pile.orgmode

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

// Without checkbox, and no nesting, and no multiline chunks
// oneOrMore
// parse indent (default 0), parse marker, parse space, parse line, parse eof or lb
val parseUnorderedList : Parser<OrgList.OrgUnorderedList> = seq(
    // No indent matching, assuming things to be indented at 0
    ::matchToken { it is Token.UnorderedListMarker },
    matchSpace,
    parseOrgLine
    // Need to handle end
).map { (markerTok, sp, line) ->
    val marker = when(((markerTok.tokens[0]) as Token.UnorderedListMarker).style) {
        Token.UnorderedListMarkerStyle.DASH -> OrgUnorderedListMarker.DASH
        Token.UnorderedListMarkerStyle.PLUS -> OrgUnorderedListMarker.PLUS
    }

    OrgList.OrgUnorderedList(
        marker = marker,
        checkbox = null,
        items = listOf(
            OrgList.OrgListItem(
                content = listOf(OrgChunk.OrgParagraph(items = line.items, tokens = line.tokens)),
                tokens = line.tokens
            )
        ),
        tokens = collectTokens(Triple(markerTok, sp, line))
    )
}

val parseParagraph: Parser<OrgChunk.OrgParagraph> = Parser { tokens, pos ->
    if (pos >= tokens.size) {
        return@Parser parsingError("Exhausted tokens while parsing paragraph")
    }

    // Stopping condition for paragraph parsing
    fun shouldStop(position: Int): Boolean {
        val token = tokens[position]

        return token is Token.EOF
                || token is Token.HeadingStars
                || token is Token.UnorderedListMarker
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

val parsePageIntroBlock: Parser<OrgBlock.OrgPageIntroBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.PAGE_INTRO },
    collectUntil { it is Token.BlockEnd && it.type == Token.BlockType.PAGE_INTRO },
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.PAGE_INTRO }
).map { (start, tokens, end) ->
    val allTokens = collectTokens(Triple(start, tokens, end))

    OrgBlock.OrgPageIntroBlock(
        body = listOf(OrgChunk.OrgParagraph(
            items = tokens.map { tok -> OrgInlineElem.Text(tok.text, tokens = listOf(tok)) },
            tokens = allTokens
        )),
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
        // ::parseSourceBlock,
        // ::parseQuoteBlock,
        // ::parseCenterBlock,
        // ::parseHTMLBlock,
        // ::parseVerseBlock,
        // ::parseLaTeXBlock,
        parsePageIntroBlock,
        // ::parseEditsBlock,
        // ::parseAsideBlock,
        // ::parseVideoBlock,
        parseUnorderedList,
        // ::parseOrderedList,
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