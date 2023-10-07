package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.pile.dropPreamble
import com.example.pile.parseOrg
import com.example.pile.parseRoamRef
import com.example.pile.parseTitle
import com.example.pile.unfillText
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bookmark

@Composable
fun OrgPreview(text: String, openNode: (String) -> Unit) {
    Column {
        OrgTitle(title = parseTitle(text))
        OrgRoamRef(text = text)
        OrgBody(text, openNode)
    }
}

@Composable
fun OrgTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(bottom = 20.dp)
    )
}

@Composable
fun OrgRoamRef(text: String) {
    parseRoamRef(text)?.let {
        OutlinedButton(
            onClick = { println(it) },
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Bookmark,
                    contentDescription = "Reference Icon",
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
                Text("Reference")
            }
        }
    }
}

@Composable
fun OrgBody(text: String, openNode: (String) -> Unit) {
    val parsed = parseOrg(dropPreamble(text))

    Column {
        ClickableBodyText(unfillText(parsed.file.preface), openNode)

        parsed.headsInList.forEach {
            val style = when (it.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.headlineSmall
            }

            Text(
                text = it.head.title,
                style = style,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )
            ClickableBodyText(unfillText(it.head.content), openNode)
        }
    }
}

@Composable
fun ClickableBodyText(text: String, openNode: (String) -> Unit) {
    val nodeLinkPattern = Regex("""\[\[id:([a-fA-F0-9\-]+)\]\[([^\]]+)\]\]""")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        nodeLinkPattern.findAll(text).forEach { match ->
            val nodeId = match.groups[1]?.value ?: ""
            val label = match.groups[2]?.value ?: ""

            append(text.substring(lastIndex, match.range.first))

            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                append(label)
            }

            addStringAnnotation(tag = "NodeID", annotation = nodeId, start = length - label.length, end = length)
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedString.getStringAnnotations("NodeID", offset, offset).firstOrNull()?.let {
                openNode(it.item)
            }
        }
    )
}