package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Square
import compose.icons.fontawesomeicons.solid.CheckSquare
import compose.icons.fontawesomeicons.solid.Circle
import compose.icons.fontawesomeicons.solid.MinusSquare
import compose.icons.fontawesomeicons.solid.Square
import xyz.lepisma.orgmode.OrgChunk
import xyz.lepisma.orgmode.OrgList
import xyz.lepisma.orgmode.OrgListCheckState

@Composable
fun CheckBoxIcon(checkboxState: OrgListCheckState?) {
    val icon = when (checkboxState) {
        null -> FontAwesomeIcons.Solid.Square
        OrgListCheckState.CHECKED -> FontAwesomeIcons.Solid.CheckSquare
        OrgListCheckState.PARTIAL -> FontAwesomeIcons.Solid.MinusSquare
        OrgListCheckState.UNCHECKED -> FontAwesomeIcons.Regular.Square
    }
    Icon(
        imageVector = icon,
        contentDescription = "Checked icon",
        modifier = Modifier
            .padding(end = 5.dp)
            .size(17.dp),
        tint = if (checkboxState == null) {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.primary
        },
    )
}

@Composable
fun OrgListItemView(item: OrgList.OrgListItem) {
    val style = if (item.checkbox == OrgListCheckState.CHECKED) {
        MaterialTheme.typography.bodyLarge.copy(
            textDecoration = TextDecoration.LineThrough,
            color = MaterialTheme.colorScheme.outline
        )
    } else {
        MaterialTheme.typography.bodyLarge
    }

    Column {
        for (chunk in item.content) {
            if (chunk is OrgChunk.OrgParagraph) {
                OrgParagraphView(chunk, style = style)
            } else {
                OrgChunkView(chunk)
            }
        }
    }
}

@Composable
fun OrgListView(orglist: OrgList.OrgUnorderedList, modifier: Modifier = Modifier) {
    // If any item has checkbox, we don't show bullets (for unordered list only)
    val hasCheckbox = orglist.items.count { it is OrgList.OrgListItem && it.checkbox != null } > 0

    Column(
        modifier = modifier
    ) {
        for (item in orglist.items) {
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp)
            ) {
                if (item is OrgList.OrgListItem) {
                    if (!hasCheckbox) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Circle,
                            contentDescription = "List start icon",
                            modifier = Modifier
                                .padding(end = 7.dp, top = 5.dp)
                                .size(8.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant,
                        )
                    } else {
                        CheckBoxIcon(item.checkbox)
                    }

                    OrgListItemView(item)
                }
            }
        }
    }
}

@Composable
fun OrgListView(orglist: OrgList.OrgOrderedList, modifier: Modifier = Modifier) {
    val hasCheckbox = orglist.items.count { it is OrgList.OrgListItem && it.checkbox != null } > 0

    Column(
        modifier = modifier
    ) {
        for ((idx, item) in orglist.items.withIndex()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp),
            ) {
                if (item is OrgList.OrgListItem) {
                    Text(
                        text = "${idx + 1}.".padStart(2),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 5.dp)
                    )

                    if (hasCheckbox) {
                        CheckBoxIcon(item.checkbox)
                    }

                    OrgListItemView(item)
                }
            }
        }
    }
}