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
typealias ParsingFn<T> = (tokens: List<Token>, pos: Int) -> ParsingResult<T>

// Parser Combinators Start

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
fun seq(vararg parsers: ParsingFn<OrgElem>): ParsingFn<OrgElem> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult<OrgElem> {
        var currentPos = pos
        val results = mutableListOf<ParsingResult.Success<OrgElem>>()

        for (parser in parsers) {
            val result = parser(tokens, currentPos)
            if (result is ParsingResult.Success) {
                results.add(result)
                currentPos = result.nextPos
            } else {
                return result
            }
        }

        return ParsingResult.Success(
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
fun zeroOrMore(parser: ParsingFn<OrgElem>): ParsingFn<OrgElem> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult.Success<OrgElem> {
        var currentPos = pos
        val results = mutableListOf<ParsingResult.Success<OrgElem>>()

        while (true) {
            val result = parser(tokens, currentPos)
            if (result is ParsingResult.Success) {
                results.add(result)
                currentPos = result.nextPos
            } else {
                break
            }
        }

        return ParsingResult.Success(
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
fun oneOrMore(parser: ParsingFn<OrgElem>): ParsingFn<OrgElem> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult<OrgElemList> {
        return seq(
            parser,
            zeroOrMore(parser)
        )(tokens, pos).map { output ->
            OrgElemList(
                items = listOf((output as OrgElemList).items[0]) + (output.items[1] as OrgElemList).items,
                tokens = emptyList()
            )
        }
    }
}

/**
 * Convert a failed result to success with OrgNothing
 */
fun maybe(parser: ParsingFn<OrgElem>): ParsingFn<OrgElem> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult.Success<OrgElem> {
        val result = parser(tokens, pos)

        return result as? ParsingResult.Success
            ?: ParsingResult.Success(
                output = OrgNothing(),
                nextPos = pos
            )
    }
}

/**
 * Match one of the parsers and return the result. Order matters.
 */
fun oneOf(vararg parsers: ParsingFn<OrgElem>): ParsingFn<OrgElem> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult<OrgElem> {
        for (parser in parsers) {
            val result = parser(tokens, pos)
            if (result is ParsingResult.Success) {
                return result
            }
        }

        return parsingError("Unable to find any match in oneOf", tokens = listOf(tokens[pos]))
    }
}

/**
 * Run the given parser and convert the successful result to Nothing
 */
fun skip(parser: ParsingFn<OrgElem>): ParsingFn<OrgElem> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult<OrgElem> {
        return parser(tokens, pos).map { output ->
            OrgNothing(tokens = output.tokens)
        }
    }
}

/**
 * Match a single token at current position
 */
fun matchToken(matchFn: (Token) -> Boolean): ParsingFn<OrgToken> {
    return fun(tokens: List<Token>, pos: Int): ParsingResult<OrgToken> {
        val tok = tokens[pos]

        return if (matchFn(tok)) {
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

fun parseDocument(tokens: List<Token>, pos: Int = 0): ParsingResult<OrgDocument> {
    return seq(
        ::matchToken { it is Token.SOF },
        ::parsePreamble,
        ::parsePreface,
        zeroOrMore(::parseSection),
        ::matchToken { it is Token.EOF }
    )(tokens, pos).map { output ->
        OrgDocument(
            preamble = (output as OrgElemList).items[1] as OrgPreamble,
            preface = output.items[2] as OrgPreface,
            content = (output.items[3] as OrgElemList).items as List<OrgSection>,
            tokens = output.tokens
        )
    }
}

fun parsePreamble(tokens: List<Token>, pos: Int): ParsingResult<OrgPreamble> {
    return seq(
        maybe(::parseProperties),
        oneOrMore(::matchToken { it is Token.LineBreak }),
        ::parseTitle,
        oneOrMore(::matchToken { it is Token.LineBreak }),
        // Need to handle permutations in these
        // maybe(::parseAuthor),
        // maybe(::parseEmail),
        // maybe(::parseDate),
        // maybe(::parseCategory),
        // ::parseFiletags,
        maybe(::parseTags),
        zeroOrMore(::matchToken { it is Token.LineBreak })
        // ::parseOptions,
        // ::parsePileOptions
    )(tokens, pos).map { output ->
        OrgPreamble(
            title = (output as OrgElemList).items[2] as OrgLine,
            tags = if (output.items[4] is OrgNothing) null else { output.items[4] as OrgTags},
            tokens = output.tokens,
            properties = if (output.items[0] is OrgNothing) null else { output.items[0] as OrgProperties }
        )
    }
}

fun parseProperties(tokens: List<Token>, pos: Int): ParsingResult<OrgProperties> {
    return seq(
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
    )(tokens, pos).map { output ->
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
}

fun parseTitle(tokens: List<Token>, pos: Int): ParsingResult<OrgLine> {
    return seq(
        ::matchToken { it is Token.FileKeyword && it.type == Token.FileKeywordType.TITLE },
        oneOrMore(::matchToken { it is Token.Space }),
        ::parseOrgLine
    )(tokens, pos).map { output ->
        (output as OrgElemList).items[2] as OrgLine
    }
}

fun parseOrgLine(tokens: List<Token>, pos: Int): ParsingResult<OrgLine> {
    val lineTokens = tokens.drop(pos).takeWhile { it !is Token.LineBreak && it !is Token.EOF }

    if (lineTokens.isEmpty()) {
        return parsingError("Unable to find valid OrgLine")
    }

    return ParsingResult.Success(
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

fun parseTags(tokens: List<Token>, pos: Int): ParsingResult<OrgTags> {
    return seq(
        ::matchToken { it is Token.FileKeyword && it.type == Token.FileKeywordType.TAGS },
        ::parseOrgLine
    )(tokens, pos).map { output ->
        val line = ((output as OrgElemList).items[1] as OrgLine).items.first() as OrgInlineElem.Text
        OrgTags(
            // FIXME: Assuming plain line
            tags = line.text.split(",").map { it.trim() },
            tokens = output.tokens
        )
    }
}

fun parsePreface(tokens: List<Token>, pos: Int): ParsingResult<OrgPreface> {
    return oneOrMore(::parseChunk)(tokens, pos).map { output ->
        OrgPreface(
            body = (output as OrgElemList).items as List<OrgChunk>,
            tokens = output.tokens
        )
    }
}

fun parseSection(tokens: List<Token>, pos: Int): ParsingResult<OrgSection> {
    return seq(
        ::parseHeading,
        oneOrMore(::parseChunk)
    )(tokens, pos).map { output ->
        OrgSection(
            heading = (output as OrgElemList).items[0] as OrgHeading,
            body = output.items.drop(1) as List<OrgChunk>,
            tokens = output.tokens
        )
    }
}

fun parseHeading(tokens: List<Token>, pos: Int): ParsingResult<OrgHeading> {
    return seq(
        ::parseLevel,
        ::matchToken { it is Token.Space },
        // maybe(::parseTODOState),
        // maybe(::parsePriority),
        ::parseHeadingTitle,
        oneOrMore(::matchToken { it is Token.LineBreak }),
        // maybe(::parseHeadingTags),
        // ::parsePlanningInfo,
        // maybe(::parseProperties)
    )(tokens, pos).map { output ->
        OrgHeading(
            level = (output as OrgElemList).items[0] as OrgHeadingLevel,
            title = output.items[1] as OrgLine,
            tokens = output.tokens
        )
    }
}

fun parseLevel(tokens: List<Token>, pos: Int): ParsingResult<OrgHeadingLevel> {
    return matchToken {
        it is Token.HeadingStars
    }(tokens, pos).map { output ->
        OrgHeadingLevel(
            level = (output.tokens.first() as Token.HeadingStars).level,
            tokens = output.tokens
        )
    }
}

fun parseHeadingTitle(tokens: List<Token>, pos: Int): ParsingResult<OrgLine> {
    // TODO: Fix this to handle tags
    return parseOrgLine(tokens, pos)
}

fun parseChunk(tokens: List<Token>, pos: Int): ParsingResult<OrgElem> {
    return seq(
        oneOf(
            // ::parseCommentLine,
            ::parseHorizontalRule,
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
            // ::parseUnorderedList,
            // ::parseOrderedList,
            ::parseParagraph
        ),
        zeroOrMore(::matchToken { it is Token.LineBreak })
    )(tokens, pos).map { output ->
        (output as OrgElemList).items[0]
    }
}

fun parseHorizontalRule(tokens: List<Token>, pos: Int): ParsingResult<OrgChunk.OrgHorizontalLine> {
    return matchToken {
        it is Token.HorizontalRule
    }(tokens, pos).map { output ->
        OrgChunk.OrgHorizontalLine(tokens = output.tokens)
    }
}

fun parseParagraph(tokens: List<Token>, pos: Int): ParsingResult<OrgChunk.OrgParagraph> {
    // TODO: Fix this since paragraph formatting will involve multi line elements
    return oneOrMore(::parseOrgLine)(tokens, pos).map { output ->
        OrgChunk.OrgParagraph(
            lines = listOf((output as OrgElemList).items[0] as OrgLine),
            tokens = output.tokens
        )
    }
}

/**
 * The main exposed function for parsing an org mode document
 */
fun parse(tokens: List<Token>): OrgDocument? {
    val result = parseDocument(tokens, pos = 0)
    return if (result is ParsingResult.Success) {
        result.output
    } else {
        null
    }
}