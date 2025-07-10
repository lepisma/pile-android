package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExternalLinkAlt
import xyz.lepisma.orgmode.OrgInlineElem
import xyz.lepisma.orgmode.OrgLine
import xyz.lepisma.orgmode.OrgPreamble

private fun orgLineToString(line: OrgLine): String {
    return line.items
        .filter { it is OrgInlineElem.Text }
        .joinToString("") { (it as OrgInlineElem.Text).text }
}

@Composable
fun OrgPreambleView(preamble: OrgPreamble, modifier: Modifier = Modifier) {
    var rawTitle = orgLineToString(preamble.title)

    Column (
        modifier = modifier
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = rawTitle,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displayMedium
        )

        if (preamble.properties?.map?.containsKey("ROAM_REFS") == true) {
            val localUriHandler = LocalUriHandler.current
            val uri = orgLineToString(preamble.properties!!.map["ROAM_REFS"]!!)
            val style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = TextDecoration.Underline
            )

            val iconSize = with(LocalDensity.current) {
                style.lineHeight.toDp()
            }

            Row(
                modifier = modifier
                    .padding(top = 10.dp)
                    .clickable(onClick = { localUriHandler.openUri(uri) }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ExternalLinkAlt,
                    contentDescription = "Open Reference Link",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(end = 5.dp)
                )
                Text(
                    text = uri,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = style,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}