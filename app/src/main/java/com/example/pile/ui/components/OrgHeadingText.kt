package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.pile.ui.formatBoldPattern
import com.example.pile.ui.formatDatePattern
import com.example.pile.ui.formatInlineCodePattern
import com.example.pile.ui.formatItalicPattern
import com.example.pile.ui.formatLinkPattern
import com.example.pile.ui.formatTagPattern
import com.example.pile.ui.formatTitlePattern

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

    annotatedString = formatTitlePattern(annotatedString, colorScheme)
    annotatedString = formatDatePattern(annotatedString, colorScheme, style.fontSize)
    annotatedString = formatLinkPattern(annotatedString, colorScheme)
    annotatedString = formatTagPattern(annotatedString, colorScheme)
    annotatedString = formatItalicPattern(annotatedString)
    annotatedString = formatBoldPattern(annotatedString)
    annotatedString = formatInlineCodePattern(annotatedString, style.fontSize)

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