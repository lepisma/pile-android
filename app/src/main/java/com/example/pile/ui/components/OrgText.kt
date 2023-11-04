package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.pile.unfillText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun addSpanStyle(original: AnnotatedString, spanStyle: SpanStyle): AnnotatedString {
    return buildAnnotatedString {
        append(original.text)
        original.spanStyles.forEach { range -> addStyle(range.item, range.start, range.end) }
        original.paragraphStyles.forEach { range -> addStyle(range.item, range.start, range.end) }

        addStyle(spanStyle, start = 0, end = original.length)
    }
}

/*
 Split a checklist item (marked as complete) string in two pieces for separate styling
 */
fun splitCheckList(original: AnnotatedString): Pair<AnnotatedString, AnnotatedString> {
    val splitPoint = 3
    return Pair(original.subSequence(0, splitPoint), original.subSequence(splitPoint, original.length))
}

/**
 * Replace portions from annotated string and style them.
 *
 * @param input Input annotated string
 * @param pattern Regex pattern to find matches for, this should work well with the replacementFn
 * @param replacementFn Function that runs on the match and returns replacement string
 * @param spanStyle Span style to apply on the replacement text (in addition to the older style)
 */
fun formatPattern(
    input: AnnotatedString,
    pattern: Regex,
    replacementFn: (MatchResult) -> String,
    spanStyle: SpanStyle,
    annotationFn: ((MatchResult) -> Pair<String, String>)? = null
): AnnotatedString {
    val pieces = mutableListOf<AnnotatedString>()

    var lastIndex = 0
    pattern.findAll(input.text).forEach { matchResult ->
        pieces.add(input.subSequence(lastIndex, matchResult.range.first))

        val matchedSubstring = input.subSequence(matchResult.range.first, matchResult.range.last + 1)
        val replacement = replacementFn(matchResult)

        pieces.add(buildAnnotatedString {
            append(replacement)

            matchedSubstring.spanStyles.forEach { range ->
                addStyle(range.item, length - replacement.length, length)
            }
            matchedSubstring.paragraphStyles.forEach { range ->
                addStyle(range.item, length - replacement.length, length)
            }

            addStyle(spanStyle, length - replacement.length, length)

            if (annotationFn != null) {
                val (tag, annotation) = annotationFn(matchResult)
                addStringAnnotation(tag, annotation, start = length - replacement.length, end = length)
            }
        })

        lastIndex = matchResult.range.last + 1
    }
    if (lastIndex <= input.text.length) {
        pieces.add(input.subSequence(lastIndex, input.text.length))
    }

    return pieces.reduce { acc, annotatedString -> acc + annotatedString }
}

/**
 * Parse org paragraph text and return annotated string object.
 */
private fun formatString(text: String, colorScheme: ColorScheme, typography: Typography): AnnotatedString {
    var unfilledText = unfillText(text)
    val shouldCross = unfilledText.matches(Regex("(?s)^\\[X\\].*"))

    unfilledText = unfilledText
        .replace(Regex("^\\[ \\]"), "⬜")
        .replace(Regex("^\\[-\\]"), "\uD83D\uDD33")
        .replace(Regex("^\\[X\\]"), "\uD83D\uDFE9")

    var annotatedString = buildAnnotatedString { append(unfilledText) }

    // Link formatting
    annotatedString = formatPattern(
        annotatedString,
        Regex("""\[\[(?<orglink>.+?)\](?:\[(?<label>[^\]]*?)\])?\]|(?<rawlink>https?:\/\/\S+)"""),
        { matchResult ->
            val url = matchResult.groups["orglink"]?.value ?: matchResult.groups["rawlink"]?.value ?: ""
            val label = matchResult.groups["label"]?.value ?: url

            if (url.startsWith("id:")) {
                "‹ $label ›"
            } else {
                label
            }
        },
        SpanStyle(color = colorScheme.primary, textDecoration = TextDecoration.Underline),
        { matchResult ->
            val url = matchResult.groups["orglink"]?.value ?: matchResult.groups["rawlink"]?.value ?: ""

            if (url.startsWith("http")) {
                Pair("EXTERNAL", url)
            } else if (url.startsWith("id:")) {
                Pair("NODE", url.substring(3))
            } else {
                Pair("UNK", url)
            }
        }
    )

    // Italic formatting
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)\/(?<text>\S(.*?)\S)\/"""),
        { matchResult -> matchResult.groups["text"]?.value ?: "" },
        SpanStyle(fontStyle = FontStyle.Italic)
    )

    // Bold formatting
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)\*(?<text>\S(.*?)\S)\*"""),
        { matchResult -> matchResult.groups["text"]?.value ?: "" },
        SpanStyle(fontWeight = FontWeight.Bold)
    )

    // Inline code formatting
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)[~=](?<text>\S(.*?)\S)[~=]"""),
        { matchResult -> matchResult.groups["text"]?.value ?: "" },
        SpanStyle(fontFamily = FontFamily.Monospace, fontSize = typography.bodyMedium.fontSize)
    )

    // Date tag
    annotatedString = formatPattern(
        annotatedString,
        Regex("""[<\[](?<stamp>\d{4}-\d{2}-\d{2}( [a-zA-Z]+)?(\s+\d{1,2}:\d{2})?)[>\]]"""),
        { matchResult ->
            val stamp = matchResult.groups["stamp"]?.value
            " $stamp "
        },
        SpanStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = typography.bodyMedium.fontSize,
            background = colorScheme.secondaryContainer,
            color = colorScheme.onSecondaryContainer
        )
    )

    return if (shouldCross) {
        val (checkmark, content) = splitCheckList(annotatedString)
        checkmark + addSpanStyle(content, SpanStyle(textDecoration = TextDecoration.LineThrough))
    } else {
        annotatedString
    }
}

@Composable
fun OrgText(text: String, openNodeById: (String) -> Unit, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var formattedString by remember { mutableStateOf<AnnotatedString?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val localUriHandler = LocalUriHandler.current

    LaunchedEffect(text) {
        coroutineScope.launch(Dispatchers.Default) {
            val output = formatString(text, colorScheme, typography)
            withContext(Dispatchers.Main) { formattedString = output }
        }
    }

    if (formattedString != null) {
        ClickableText(
            text = formattedString!!,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            onClick = { offset ->
                formattedString!!.getStringAnnotations("NODE", offset, offset).firstOrNull()?.let {
                    openNodeById(it.item)
                }
                formattedString!!.getStringAnnotations("EXTERNAL", offset, offset).firstOrNull()?.let {
                    localUriHandler.openUri(it.item)
                }
            },
            modifier = modifier.padding(bottom = 10.dp)
        )
    } else {
        Text(text)
    }
}