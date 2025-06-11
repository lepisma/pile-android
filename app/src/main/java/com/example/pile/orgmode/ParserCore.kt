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

fun <T, R> Parser<T>.map(transform: (T) -> R) : Parser<R> {
    return Parser { tokens, pos ->
        val result = this@map.invoke(tokens, pos)
        result.map(transform)
    }
}

fun <T> Parser<T>.debug(name: String? = null) : Parser<T> {
    return Parser { tokens, pos ->
        val nTokens = 5
        println("Parser $name inputs: ${tokens.subList(pos, pos + nTokens)}")
        val result = this@debug.invoke(tokens, pos)
        println("Parser $name output: $result")
        result
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
fun <T1, T2> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>
): Parser<Pair<T1, T2>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Pair<T1, T2>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Pair<T1, T2>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        ParsingResult.Success(
            output = Pair(
                success1.output,
                success2.output
            ),
            nextPos = result2.nextPos
        )
    }
}

fun <T1, T2, T3> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>
): Parser<Triple<T1, T2, T3>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Triple<T1, T2, T3>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Triple<T1, T2, T3>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Triple<T1, T2, T3>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        ParsingResult.Success(
            output = Triple(
                success1.output,
                success2.output,
                success3.output
            ),
            nextPos = success3.nextPos
        )
    }
}

data class Tuple4<T1, T2, T3, T4>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4
)

fun <T1, T2, T3, T4> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>
): Parser<Tuple4<T1, T2, T3, T4>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple4<T1, T2, T3, T4>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple4<T1, T2, T3, T4>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple4<T1, T2, T3, T4>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple4<T1, T2, T3, T4>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        ParsingResult.Success(
            output = Tuple4(
                success1.output,
                success2.output,
                success3.output,
                success4.output
            ),
            nextPos = success4.nextPos
        )
    }
}


data class Tuple5<T1, T2, T3, T4, T5>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5
)

fun <T1, T2, T3, T4, T5> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>,
    parser5: Parser<T5>
): Parser<Tuple5<T1, T2, T3, T4, T5>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple5<T1, T2, T3, T4, T5>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple5<T1, T2, T3, T4, T5>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple5<T1, T2, T3, T4, T5>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple5<T1, T2, T3, T4, T5>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        val result5 = parser5.invoke(tokens, success4.nextPos)
        if (result5 is ParsingResult.Failure) {
            return@Parser result5 as ParsingResult.Failure<Tuple5<T1, T2, T3, T4, T5>>
        }
        val success5 = result5 as ParsingResult.Success<T5>

        ParsingResult.Success(
            output = Tuple5(
                success1.output,
                success2.output,
                success3.output,
                success4.output,
                success5.output
            ),
            nextPos = success5.nextPos
        )
    }
}

data class Tuple6<T1, T2, T3, T4, T5, T6>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6
)

fun <T1, T2, T3, T4, T5, T6> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>,
    parser5: Parser<T5>,
    parser6: Parser<T6>
): Parser<Tuple6<T1, T2, T3, T4, T5, T6>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple6<T1, T2, T3, T4, T5, T6>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple6<T1, T2, T3, T4, T5, T6>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple6<T1, T2, T3, T4, T5, T6>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple6<T1, T2, T3, T4, T5, T6>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        val result5 = parser5.invoke(tokens, success4.nextPos)
        if (result5 is ParsingResult.Failure) {
            return@Parser result5 as ParsingResult.Failure<Tuple6<T1, T2, T3, T4, T5, T6>>
        }
        val success5 = result5 as ParsingResult.Success<T5>

        val result6 = parser6.invoke(tokens, success5.nextPos)
        if (result6 is ParsingResult.Failure) {
            return@Parser result6 as ParsingResult.Failure<Tuple6<T1, T2, T3, T4, T5, T6>>
        }
        val success6 = result6 as ParsingResult.Success<T6>

        ParsingResult.Success(
            output = Tuple6(
                success1.output,
                success2.output,
                success3.output,
                success4.output,
                success5.output,
                success6.output
            ),
            nextPos = success6.nextPos
        )
    }
}

data class Tuple7<T1, T2, T3, T4, T5, T6, T7>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6,
    val seventh: T7
)

fun <T1, T2, T3, T4, T5, T6, T7> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>,
    parser5: Parser<T5>,
    parser6: Parser<T6>,
    parser7: Parser<T7>
): Parser<Tuple7<T1, T2, T3, T4, T5, T6, T7>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        val result5 = parser5.invoke(tokens, success4.nextPos)
        if (result5 is ParsingResult.Failure) {
            return@Parser result5 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success5 = result5 as ParsingResult.Success<T5>

        val result6 = parser6.invoke(tokens, success5.nextPos)
        if (result6 is ParsingResult.Failure) {
            return@Parser result6 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success6 = result6 as ParsingResult.Success<T6>

        val result7 = parser7.invoke(tokens, success6.nextPos)
        if (result7 is ParsingResult.Failure) {
            return@Parser result7 as ParsingResult.Failure<Tuple7<T1, T2, T3, T4, T5, T6, T7>>
        }
        val success7 = result7 as ParsingResult.Success<T7>

        ParsingResult.Success(
            output = Tuple7(
                success1.output,
                success2.output,
                success3.output,
                success4.output,
                success5.output,
                success6.output,
                success7.output
            ),
            nextPos = success7.nextPos
        )
    }
}

data class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6,
    val seventh: T7,
    val eighth: T8
)

fun <T1, T2, T3, T4, T5, T6, T7, T8> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>,
    parser5: Parser<T5>,
    parser6: Parser<T6>,
    parser7: Parser<T7>,
    parser8: Parser<T8>
): Parser<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        val result5 = parser5.invoke(tokens, success4.nextPos)
        if (result5 is ParsingResult.Failure) {
            return@Parser result5 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success5 = result5 as ParsingResult.Success<T5>

        val result6 = parser6.invoke(tokens, success5.nextPos)
        if (result6 is ParsingResult.Failure) {
            return@Parser result6 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success6 = result6 as ParsingResult.Success<T6>

        val result7 = parser7.invoke(tokens, success6.nextPos)
        if (result7 is ParsingResult.Failure) {
            return@Parser result7 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success7 = result7 as ParsingResult.Success<T7>

        val result8 = parser8.invoke(tokens, success7.nextPos)
        if (result8 is ParsingResult.Failure) {
            return@Parser result8 as ParsingResult.Failure<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>
        }
        val success8 = result8 as ParsingResult.Success<T8>

        ParsingResult.Success(
            output = Tuple8(
                success1.output,
                success2.output,
                success3.output,
                success4.output,
                success5.output,
                success6.output,
                success7.output,
                success8.output
            ),
            nextPos = success8.nextPos
        )
    }
}

data class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6,
    val seventh: T7,
    val eighth: T8,
    val ninth: T9
)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>,
    parser5: Parser<T5>,
    parser6: Parser<T6>,
    parser7: Parser<T7>,
    parser8: Parser<T8>,
    parser9: Parser<T9>
): Parser<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        val result5 = parser5.invoke(tokens, success4.nextPos)
        if (result5 is ParsingResult.Failure) {
            return@Parser result5 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success5 = result5 as ParsingResult.Success<T5>

        val result6 = parser6.invoke(tokens, success5.nextPos)
        if (result6 is ParsingResult.Failure) {
            return@Parser result6 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success6 = result6 as ParsingResult.Success<T6>

        val result7 = parser7.invoke(tokens, success6.nextPos)
        if (result7 is ParsingResult.Failure) {
            return@Parser result7 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success7 = result7 as ParsingResult.Success<T7>

        val result8 = parser8.invoke(tokens, success7.nextPos)
        if (result8 is ParsingResult.Failure) {
            return@Parser result8 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success8 = result8 as ParsingResult.Success<T8>

        val result9 = parser9.invoke(tokens, success8.nextPos)
        if (result9 is ParsingResult.Failure) {
            return@Parser result9 as ParsingResult.Failure<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
        }
        val success9 = result9 as ParsingResult.Success<T9>

        ParsingResult.Success(
            output = Tuple9(
                success1.output,
                success2.output,
                success3.output,
                success4.output,
                success5.output,
                success6.output,
                success7.output,
                success8.output,
                success9.output
            ),
            nextPos = success9.nextPos
        )
    }
}

data class Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6,
    val seventh: T7,
    val eighth: T8,
    val ninth: T9,
    val tenth: T10
)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> seq(
    parser1: Parser<T1>,
    parser2: Parser<T2>,
    parser3: Parser<T3>,
    parser4: Parser<T4>,
    parser5: Parser<T5>,
    parser6: Parser<T6>,
    parser7: Parser<T7>,
    parser8: Parser<T8>,
    parser9: Parser<T9>,
    parser10: Parser<T10>
): Parser<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> {
    return Parser { tokens, pos ->
        val result1 = parser1.invoke(tokens, pos)
        if (result1 is ParsingResult.Failure) {
            return@Parser result1 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success1 = result1 as ParsingResult.Success<T1>

        val result2 = parser2.invoke(tokens, success1.nextPos)
        if (result2 is ParsingResult.Failure) {
            return@Parser result2 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success2 = result2 as ParsingResult.Success<T2>

        val result3 = parser3.invoke(tokens, success2.nextPos)
        if (result3 is ParsingResult.Failure) {
            return@Parser result3 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success3 = result3 as ParsingResult.Success<T3>

        val result4 = parser4.invoke(tokens, success3.nextPos)
        if (result4 is ParsingResult.Failure) {
            return@Parser result4 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success4 = result4 as ParsingResult.Success<T4>

        val result5 = parser5.invoke(tokens, success4.nextPos)
        if (result5 is ParsingResult.Failure) {
            return@Parser result5 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success5 = result5 as ParsingResult.Success<T5>

        val result6 = parser6.invoke(tokens, success5.nextPos)
        if (result6 is ParsingResult.Failure) {
            return@Parser result6 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success6 = result6 as ParsingResult.Success<T6>

        val result7 = parser7.invoke(tokens, success6.nextPos)
        if (result7 is ParsingResult.Failure) {
            return@Parser result7 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success7 = result7 as ParsingResult.Success<T7>

        val result8 = parser8.invoke(tokens, success7.nextPos)
        if (result8 is ParsingResult.Failure) {
            return@Parser result8 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success8 = result8 as ParsingResult.Success<T8>

        val result9 = parser9.invoke(tokens, success8.nextPos)
        if (result9 is ParsingResult.Failure) {
            return@Parser result9 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success9 = result9 as ParsingResult.Success<T9>

        val result10 = parser10.invoke(tokens, success9.nextPos)
        if (result10 is ParsingResult.Failure) {
            return@Parser result10 as ParsingResult.Failure<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>>
        }
        val success10 = result10 as ParsingResult.Success<T10>

        ParsingResult.Success(
            output = Tuple10(
                success1.output,
                success2.output,
                success3.output,
                success4.output,
                success5.output,
                success6.output,
                success7.output,
                success8.output,
                success9.output,
                success10.output
            ),
            nextPos = success10.nextPos
        )
    }
}

/**
 * Run the same parser many times and return results till a failure.
 */
fun <T> zeroOrMore(parser: Parser<T>): Parser<List<T>> {
    return Parser<List<T>> { tokens, pos ->
        var currentPos = pos
        val results = mutableListOf<T>()

        while (true) {
            val result = parser.invoke(tokens, currentPos)
            if (result is ParsingResult.Success) {
                results.add(result.output)
                currentPos = result.nextPos
            } else {
                break
            }
        }

        ParsingResult.Success<List<T>>(
            output = results.toList(),
            nextPos = currentPos
        )
    }
}

/**
 * Run the same parser many times and return results till a failure. This ensures that at least one
 * parse happens.
 */
fun <T> oneOrMore(parser: Parser<T>): Parser<List<T>> = seq(
    parser,
    zeroOrMore(parser)
).map { (first, rest) -> listOf(first) + rest }

/**
 * Convert a failed result to success with OrgNothing
 */
fun <T> maybe(parser: Parser<T>): Parser<T?> {
    return Parser<T?> { tokens, pos ->
        val result = parser.invoke(tokens, pos)

        result as? ParsingResult.Success<T?>
            ?: ParsingResult.Success<T?>(
                output = null,
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

/**
 * Collect all tokens from current till any one of them matches the given function.
 */
fun collectUntill(matchFn: (Token) -> Boolean): Parser<List<Token>> {
    return Parser<List<Token>> { tokens, pos ->
        val collectedTokens = tokens.drop(pos).takeWhile { !matchFn(it) }

        if (collectedTokens.isEmpty()) {
            parsingError("Unable to collect any token")
        } else {
            ParsingResult.Success(
                output = collectedTokens,
                nextPos = pos + collectedTokens.count()
            )
        }
    }
}

// A few helpful parsers
val matchSOF: Parser<OrgToken> = matchToken { it is Token.SOF }
val matchLineBreak: Parser<OrgToken> = matchToken { it is Token.LineBreak }
val matchSpace: Parser<OrgToken> = matchToken { it is Token.Space }
val matchSpaces: Parser<List<OrgToken>> = oneOrMore(matchSpace)
val matchEOF: Parser<OrgToken> = matchToken { it is Token.EOF }

fun <T> collectTokens(result: T): List<Token> {
    return when (result) {
        is List<*> -> result.map { collectTokens(it) }.flatten()
        is OrgElem -> result.tokens
        else -> {
            println("Unable to collect tokens from $result")
            emptyList()
        }
    }
}

fun <T1, T2> collectTokens(
    result: Pair<T1, T2>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second)
}

fun <T1, T2, T3> collectTokens(
    result: Triple<T1, T2, T3>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third)
}

fun <T1, T2, T3, T4> collectTokens(
    result: Tuple4<T1, T2, T3, T4>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth)
}

fun <T1, T2, T3, T4, T5> collectTokens(
    result: Tuple5<T1, T2, T3, T4, T5>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth) +
            collectTokens(result.fifth)
}

fun <T1, T2, T3, T4, T5, T6> collectTokens(
    result: Tuple6<T1, T2, T3, T4, T5, T6>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth) +
            collectTokens(result.fifth) +
            collectTokens(result.sixth)
}

fun <T1, T2, T3, T4, T5, T6, T7> collectTokens(
    result: Tuple7<T1, T2, T3, T4, T5, T6, T7>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth) +
            collectTokens(result.fifth) +
            collectTokens(result.sixth) +
            collectTokens(result.seventh)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8> collectTokens(
    result: Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth) +
            collectTokens(result.fifth) +
            collectTokens(result.sixth) +
            collectTokens(result.seventh) +
            collectTokens(result.eighth)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> collectTokens(
    result: Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth) +
            collectTokens(result.fifth) +
            collectTokens(result.sixth) +
            collectTokens(result.seventh) +
            collectTokens(result.eighth) +
            collectTokens(result.ninth)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> collectTokens(
    result: Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
): List<Token> {
    return collectTokens(result.first) +
            collectTokens(result.second) +
            collectTokens(result.third) +
            collectTokens(result.fourth) +
            collectTokens(result.fifth) +
            collectTokens(result.sixth) +
            collectTokens(result.seventh) +
            collectTokens(result.eighth) +
            collectTokens(result.ninth) +
            collectTokens(result.tenth)
}