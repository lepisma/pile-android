package com.example.pile.orgmode

/**
 * Output from a parser
 */
data class ParsingResult(
    val succeeded: Boolean,
    val output: List<OrgElem>,
    val nextPos: Int
)

/**
 * Type of a parsing function which takes the list of all tokens and the current working position.
 */
typealias ParsingFn = (tokens: List<Token>, pos: Int) -> ParsingResult

// Parser Combinators

/**
 * Sequence given parsers and execute them one by one. If any parse fails, stop immediately and
 * return the failed result.
 */
fun seq(vararg parsers: ParsingFn): ParsingFn {
    return fun(tokens: List<Token>, pos: Int): ParsingResult {
        var currentPos = pos
        val results = mutableListOf<ParsingResult>()

        for (parser in parsers) {
            val result = parser(tokens, currentPos)
            if (result.succeeded) {
                results.add(result)
                currentPos = result.nextPos
            } else {
                return result
            }
        }

        return ParsingResult(
            succeeded = true,
            output = results.map { it.output }.flatten(),
            nextPos = results.last().nextPos
        )
    }
}

/**
 * Run the same parser many times and return results till a failure.
 */
fun zeroOrMore(parser: ParsingFn): ParsingFn {
    return fun(tokens: List<Token>, pos: Int): ParsingResult {
        var currentPos = pos
        val results = mutableListOf<ParsingResult>()

        while (true) {
            val result = parser(tokens, currentPos)
            if (result.succeeded) {
                results.add(result)
                currentPos = result.nextPos
            } else {
                break
            }
        }

        return ParsingResult(
            succeeded = true,
            output = results.map { it.output }.flatten(),
            nextPos = results.last().nextPos
        )
    }
}

/**
 * Run the same parser many times and return results till a failure. This ensures that at least one
 * parse happens.
 */
fun oneOrMore(parser: ParsingFn): ParsingFn {
    return fun(tokens: List<Token>, pos: Int): ParsingResult {
        val result = zeroOrMore(parser)(tokens, pos)

        if (result.output.isEmpty()) {
            // This is a failure since we need at least one parse here
            return ParsingResult(
                succeeded = false,
                output = listOf(OrgParsingError(
                    message = "Unable to run oneOrMore for ${parser}",
                    tokens = tokens.subList(pos, tokens.lastIndex)
                )),
                nextPos = pos
            )
        } else {
            return result
        }
    }
}

fun maybe(parser: ParsingFn): ParsingFn {
    return fun(tokens: List<Token>, pos: Int): ParsingResult {
        val result = parser(tokens, pos)
        if (result.succeeded) {
            return result
        } else {
            return ParsingResult(
                succeeded = true,
                output = emptyList(),
                nextPos = pos
            )
        }
    }
}

/**
 * Match one of the parsers and return the result. Order matters.
 */
fun oneOf(vararg parsers: ParsingFn): ParsingFn {
    return fun(tokens: List<Token>, pos: Int): ParsingResult {
        for (parser in parsers) {
            val result = parser(tokens, pos)
            if (result.succeeded) {
                return result
            }
        }

        return ParsingResult(
            succeeded = false,
            output = emptyList(),
            nextPos = pos
        )
    }
}

fun parseDocument(input: List<Token>) {
    // seq
    //   preamble
    //   preface
    //   >=0 section
}

fun parsePreamble() {
    // seq
    //   maybe properties
    //   permut
    //     title
    //     maybe author
    //     maybe email
    //     maybe date
    //     maybe category
    //     filetags
    //     tags
    //     options
    //     pileOptions
}

fun parsePreface() {
    // >=1 chunk
}

fun parseSection() {
    // seq
    //   heading
    //   >=1 chunk
}

fun parseHeading() {
    // seq
    //   level
    //   maybe TODOstate
    //   maybe priority
    //   title
    //   tags
    //   planningInfo
    //   maybe properties
}

fun parseChunk() {
    // oneOf (order matters)
    //   commentLine
    //   HR
    //   table
    //   commentBlock
    //   exampleBlock
    //   sourceBlock
    //   quoteBlock
    //   centerBlock
    //   htmlBlock
    //   verseBlock
    //   latexBlock
    //   pageIntroBlock
    //   editsBlock
    //   asideBlock
    //   videoBlock
    //   unorderedList
    //   orderedList
    //   paragraph
}

class OrgParser {
    fun parse(tokens: List<Token>): OrgDocument? {

        for (token in tokens) {
            if (token is Token.FileKeyword) {
                if (token.type == Token.FileKeywordType.TITLE) {
                    //
                }
            }
        }
        return null
    }
}