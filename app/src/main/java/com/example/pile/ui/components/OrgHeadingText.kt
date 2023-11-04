package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun OrgHeadingText(text: String, level: Int, openNodeById: (String) -> Unit) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineLarge
        2 -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineSmall
    }

    var annotatedString = buildAnnotatedString { append(text) }
    val localUriHandler = LocalUriHandler.current
    val colorScheme = MaterialTheme.colorScheme

    // Link
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

    // Italic
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)\/(?<text>\S(.*?)\S)\/"""),
        { matchResult -> matchResult.groups["text"]?.value ?: "" },
        SpanStyle(fontStyle = FontStyle.Italic)
    )

    // Bold
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)\*(?<text>\S(.*?)\S)\*"""),
        { matchResult -> matchResult.groups["text"]?.value ?: "" },
        SpanStyle(fontWeight = FontWeight.Bold)
    )

    // Inline code
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)[~=](?<text>\S(.*?)\S)[~=]"""),
        { matchResult -> matchResult.groups["text"]?.value ?: "" },
        SpanStyle(fontFamily = FontFamily.Monospace, fontSize = style.fontSize)
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
            background = colorScheme.secondaryContainer,
            color = colorScheme.onSecondaryContainer
        )
    )

    // Title state
    annotatedString = formatPattern(
        annotatedString,
        Regex("""^(?<state>TODO|NEXT|ACTIVE|DONE)"""),
        { matchResult ->
            " ${matchResult.groups["state"]?.value} "
        },
        SpanStyle(
            background = colorScheme.primaryContainer,
            color = colorScheme.onPrimaryContainer
        )
    )

    // Title completion
    annotatedString = formatPattern(
        annotatedString,
        Regex("""(?<completion>\[\d{1,5}\/\d{1,5}\])$"""),
        { matchResult ->
            "${matchResult.groups["completion"]?.value}"
        },
        SpanStyle(
            fontFamily = FontFamily.Monospace,
            color = colorScheme.primary
        )
    )

    ClickableText(
        text = annotatedString,
        style = style,
        onClick = { offset ->
            annotatedString.getStringAnnotations("NODE", offset, offset).firstOrNull()?.let {
                openNodeById(it.item)
            }
            annotatedString.getStringAnnotations("EXTERNAL", offset, offset).firstOrNull()?.let {
                localUriHandler.openUri(it.item)
            }
        },
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
    )
}