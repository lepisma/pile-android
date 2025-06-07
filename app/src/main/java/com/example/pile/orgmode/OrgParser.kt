package com.example.pile.orgmode

/**
 * Output from a parser
 */
sealed class ParsingResult<out T> {
    data class Success<T>(
        val output: T,
        val nextPos: Int
    ) : ParsingResult<T>()

    data class Failure<T>(
        val error: OrgParsingError
    ) : ParsingResult<T>()
}

fun <T> parsingError(message: String, tokens: List<Token> = emptyList()): ParsingResult.Failure<T> {
    return ParsingResult.Failure<T>(
        error = OrgParsingError(message, tokens)
    )
}

/**
 * Type of a parsing function which takes the list of all tokens and the current working position.
 */
fun interface Parser<out T> {
    fun invoke(tokens: List<Token>, pos: Int): ParsingResult<T>
}

// Parser Combinators Start

fun <T, R> Parser<T>.map(transform: (T) -> R) : Parser<R> {
    return Parser { tokens, pos ->
        val result = this@map.invoke(tokens, pos)
        result.map(transform)
    }
}

fun <T, R> ParsingResult<T>.map(transform: (T) -> R): ParsingResult<R> {
    return when (this) {
        is ParsingResult.Success -> ParsingResult.Success(transform(this.output), this.nextPos)
        is ParsingResult.Failure -> ParsingResult.Failure(this.error)
    }
}

/**
 * Sequence given parsers and execute them one by one. If any parse fails, stop immediately and
 * return the failed result.
 */
fun seq(vararg parsers: Parser<OrgElem>): Parser<OrgElem> {
    return Parser<OrgElem> { tokens, pos ->
        var currentPos = pos
        val results = mutableListOf<ParsingResult.Success<OrgElem>>()

        for (parser in parsers) {
            val result = parser.invoke(tokens, currentPos)
            if (result is ParsingResult.Success) {
                results.add(result)
                currentPos = result.nextPos
            } else {
                return@Parser result
            }
        }

        ParsingResult.Success(
            output = OrgElemList(
                results.map { it.output },
                tokens = results.flatMap { it.output.tokens }
            ),
            nextPos = currentPos
        )
    }
}

/**
 * Run the same parser many times and return results till a failure.
 */
fun zeroOrMore(parser: Parser<OrgElem>): Parser<OrgElemList> {
    return Parser<OrgElemList> { tokens, pos ->
        var currentPos = pos
        val results = mutableListOf<ParsingResult.Success<OrgElem>>()

        while (true) {
            val result = parser.invoke(tokens, currentPos)
            if (result is ParsingResult.Success) {
                results.add(result)
                currentPos = result.nextPos
            } else {
                break
            }
        }

        ParsingResult.Success(
            output = OrgElemList(
                results.map { it.output },
                tokens = results.flatMap { it.output.tokens }
            ),
            nextPos = currentPos
        )
    }
}

/**
 * Run the same parser many times and return results till a failure. This ensures that at least one
 * parse happens.
 */
fun oneOrMore(parser: Parser<OrgElem>): Parser<OrgElemList> = seq(
    parser,
    zeroOrMore(parser)
).map { output ->
    OrgElemList(
        items = listOf((output as OrgElemList).items[0]) + (output.items[1] as OrgElemList).items,
        tokens = output.tokens
    )
}

/**
 * Convert a failed result to success with OrgNothing
 */
fun maybe(parser: Parser<OrgElem>): Parser<OrgElem> {
    return Parser<OrgElem> { tokens, pos ->
        val result = parser.invoke(tokens, pos)

        result as? ParsingResult.Success
            ?: ParsingResult.Success(
                output = OrgNothing(),
                nextPos = pos
            )
    }
}

/**
 * Match one of the parsers and return the result. Order matters.
 */
fun oneOf(vararg parsers: Parser<OrgElem>): Parser<OrgElem> {
    return Parser<OrgElem> { tokens, pos ->
        for (parser in parsers) {
            val result = parser.invoke(tokens, pos)
            if (result is ParsingResult.Success) {
                return@Parser result
            }
        }

        parsingError("Unable to find any match in oneOf", tokens = listOf(tokens[pos]))
    }
}

/**
 * Match a single token at current position
 */
fun matchToken(matchFn: (Token) -> Boolean): Parser<OrgToken> {
    return Parser<OrgToken> { tokens, pos ->
        val tok = tokens[pos]

        if (matchFn(tok)) {
            ParsingResult.Success(
                output = OrgToken(tokens = listOf(tok)),
                nextPos = pos + 1
            )
        } else {
            parsingError("Unable to match token: ${tok}", tokens = listOf(tokens[pos]))
        }
    }
}

// Combinators end

val parseProperties: Parser<OrgProperties> = seq(
    ::matchToken { it is Token.DrawerStart },
    ::matchToken { it is Token.LineBreak },
    oneOrMore(
        seq(
            ::matchToken { it is Token.DrawerPropertyKey },
            zeroOrMore(
                ::matchToken { it is Token.Space }
            ),
            ::matchToken { it is Token.DrawerPropertyValue },
            ::matchToken { it is Token.LineBreak }
        )
    ),
    ::matchToken { it is Token.DrawerEnd }
).map { output ->
    val propLines = (output as OrgElemList).items[2] as OrgElemList
    var map = mutableMapOf<String, OrgLine>()

    for (propLine in propLines.items) {
        val k = (propLine as OrgElemList).items[0] as OrgToken
        val v = propLine.items[2] as OrgToken

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
        tokens = output.tokens
    )
}

val parseOrgLine: Parser<OrgLine> = Parser { tokens, pos ->
    val lineTokens = tokens.drop(pos).takeWhile { it !is Token.LineBreak && it !is Token.EOF }

    if (lineTokens.isEmpty()) {
        return@Parser parsingError("Unable to find valid OrgLine")
    }

    ParsingResult.Success(
        // TODO: Do this handling more inline elements
        output = OrgLine(
            items = listOf(
                OrgInlineElem.Text(
                    lineTokens.joinToString("") { it.text },
                    tokens = lineTokens
                )),
            tokens = lineTokens
        ),
        nextPos = pos + lineTokens.count()
    )
}

val parseFileKeyword: Parser<OrgElemList> = seq(
    ::matchToken { it is Token.FileKeyword },
    oneOrMore(::matchToken { it is Token.Space }),
    parseOrgLine
).map { output ->
    OrgElemList(
        items = listOf(
            (output as OrgElemList).items[0],
            output.items[2]
        ),
        tokens = output.tokens
    )
}

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
    oneOrMore(::matchToken { it is Token.LineBreak }),
    oneOrMore(seq(parseFileKeyword, ::matchToken { it is Token.LineBreak })),
    zeroOrMore(::matchToken { it is Token.LineBreak })
).map { output ->
    // We need to interpret all the file keywords
    var title: OrgLine? = null
    var tags: OrgTags? = null

    for (keywordLine in ((output as OrgElemList).items[2] as OrgElemList).items) {
        val keywordPair = (keywordLine as OrgElemList).items[0] as OrgElemList
        val token = (keywordPair.items[0] as OrgToken).tokens[0] as Token.FileKeyword
        val valueLine = keywordPair.items[1] as OrgLine

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
        tokens = output.tokens,
        properties = if (output.items[0] is OrgNothing) null else { output.items[0] as OrgProperties }
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
    ::matchToken { it is Token.Space },
    // maybe(::parseTODOState),
    // maybe(::parsePriority),
    parseHeadingTitle,
    oneOrMore(::matchToken { it is Token.LineBreak }),
    // maybe(::parseHeadingTags),
    // ::parsePlanningInfo,
    // maybe(::parseProperties)
).map { output ->
    OrgHeading(
        level = (output as OrgElemList).items[0] as OrgHeadingLevel,
        title = output.items[2] as OrgLine,
        tokens = output.tokens
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
    ::matchToken { it is Token.Space },
    parseOrgLine
    // Need to handle end
).map { output ->
    val marker = when(
        ((((output as OrgElemList)
            .items[0] as OrgToken)
            .tokens[0]) as Token.UnorderedListMarker)
            .style
    ) {
        Token.UnorderedListMarkerStyle.DASH -> OrgUnorderedListMarker.DASH
        Token.UnorderedListMarkerStyle.PLUS -> OrgUnorderedListMarker.PLUS
    }
    val line = output.items[2] as OrgLine

    OrgList.OrgUnorderedList(
        marker = marker,
        checkbox = null,
        items = listOf(
            OrgList.OrgListItem(
                content = listOf(OrgChunk.OrgParagraph(items = line.items, tokens = line.tokens)),
                tokens = line.tokens
            )
        ),
        tokens = output.tokens
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

val parseChunk: Parser<OrgElem> = seq(
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
        // ::parsePageIntroBlock,
        // ::parseEditsBlock,
        // ::parseAsideBlock,
        // ::parseVideoBlock,
        parseUnorderedList,
        // ::parseOrderedList,
        parseParagraph
    ),
    zeroOrMore(::matchToken { it is Token.LineBreak })
).map { output ->
    (output as OrgElemList).items[0]
}

val parsePreface: Parser<OrgPreface> = zeroOrMore(parseChunk).map { output ->
    OrgPreface(
        body = (output as OrgElemList).items as List<OrgChunk>,
        tokens = output.tokens
    )
}

val parseSection: Parser<OrgSection> = seq(
    parseHeading,
    zeroOrMore(parseChunk)
).map { output ->
    OrgSection(
        heading = (output as OrgElemList).items[0] as OrgHeading,
        body = (output.items[1] as OrgElemList).items as List<OrgChunk>,
        tokens = output.tokens
    )
}

val parseDocument: Parser<OrgDocument> = seq(
    ::matchToken { it is Token.SOF },
    parsePreamble,
    parsePreface,
    zeroOrMore(parseSection),
    ::matchToken { it is Token.EOF }
).map { output ->
    OrgDocument(
        preamble = (output as OrgElemList).items[1] as OrgPreamble,
        preface = output.items[2] as OrgPreface,
        content = (output.items[3] as OrgElemList).items as List<OrgSection>,
        tokens = output.tokens
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