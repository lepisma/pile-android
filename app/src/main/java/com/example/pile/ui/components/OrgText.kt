package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.pile.unfillText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun strikeThrough(original: AnnotatedString): AnnotatedString {
    return buildAnnotatedString {
        append(original.text)

        original.spanStyles.forEach { range ->
            addStyle(range.item, range.start, range.end)
        }

        addStyle(
            SpanStyle(textDecoration = TextDecoration.LineThrough),
            start = 0,
            end = original.length
        )
    }
}

/*
 Split a checklist item (marked as complete) string in two pieces for separate styling
 */
fun splitCheckList(original: AnnotatedString): Pair<AnnotatedString, AnnotatedString> {
    val splitPoint = 3
    return Pair(original.subSequence(0, splitPoint), original.subSequence(splitPoint, original.length))
}

@Composable
fun OrgText(text: String, openNodeById: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var formattedString by remember { mutableStateOf<AnnotatedString?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(text) {
        coroutineScope.launch(Dispatchers.Default) {
            val output = formatString(text, primaryColor)
            withContext(Dispatchers.Main) { formattedString = output }
        }
    }

    if (formattedString != null) {
        ClickableText(
            text = formattedString!!,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            onClick = { offset ->
                formattedString!!.getStringAnnotations("NodeID", offset, offset).firstOrNull()?.let {
                    openNodeById(it.item)
                }
            },
            modifier = Modifier.padding(bottom = 10.dp)
        )
    } else {
        Text(text)
    }
}

/**
 * Parse org paragraph text and return annotated string object.
 */
private fun formatString(text: String, primaryColor: Color): AnnotatedString {
    var unfilledText = unfillText(text)
    val shouldCross = unfilledText.matches(Regex("(?s)^\\[X\\].*"))

    unfilledText = unfilledText
        .replace(Regex("^\\[ \\]"), "⬜")
        .replace(Regex("^\\[-\\]"), "\uD83D\uDD33")
        .replace(Regex("^\\[X\\]"), "\uD83D\uDFE9")

    val nodeLinkPattern = Regex("""\[\[id:([a-fA-F0-9\-]+)\]\[([^\]]+)\]\]""")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        nodeLinkPattern.findAll(unfilledText).forEach { match ->
            val nodeId = match.groups[1]?.value ?: ""
            val label = match.groups[2]?.value ?: ""

            append(unfilledText.substring(lastIndex, match.range.first))

            withStyle(
                SpanStyle(
                    color = primaryColor,
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
    return if (shouldCross) {
        val (checkmark, content) = splitCheckList(annotatedString)
        checkmark + strikeThrough(content)
    } else {
        annotatedString
    }
}