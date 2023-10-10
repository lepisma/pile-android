package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.pile.unfillText

@Composable
fun OrgText(text: String, openNode: (String) -> Unit) {
    val unfilledText = unfillText(text)
    val nodeLinkPattern = Regex("""\[\[id:([a-fA-F0-9\-]+)\]\[([^\]]+)\]\]""")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        nodeLinkPattern.findAll(unfilledText).forEach { match ->
            val nodeId = match.groups[1]?.value ?: ""
            val label = match.groups[2]?.value ?: ""

            append(unfilledText.substring(lastIndex, match.range.first))

            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(label)
            }

            addStringAnnotation(
                tag = "NodeID",
                annotation = nodeId,
                start = length - label.length,
                end = length
            )
            lastIndex = match.range.last + 1
        }
        append(unfilledText.substring(lastIndex))
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedString.getStringAnnotations("NodeID", offset, offset).firstOrNull()?.let {
                openNode(it.item)
            }
        },
        modifier = Modifier.padding(bottom = 10.dp)
    )
}