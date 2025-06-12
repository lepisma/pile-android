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
import com.example.pile.orgmode.OrgChunk
import com.example.pile.orgmode.OrgList
import com.example.pile.orgmode.OrgListCheckState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Circle
import compose.icons.fontawesomeicons.regular.DotCircle
import compose.icons.fontawesomeicons.solid.CheckCircle
import compose.icons.fontawesomeicons.solid.Circle

@Composable
fun OrgListView(orglist: OrgList.OrgUnorderedList, modifier: Modifier = Modifier) {
    // If any item has checkbox, we don't show bullets (for unordered list only)
    val hasCheckbox = orglist.items.count { it is OrgList.OrgListItem && it.checkbox != null } > 0

    Column(
        modifier = modifier
            .padding(vertical = 10.dp)
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
                        val icon = when (item.checkbox) {
                            null -> FontAwesomeIcons.Solid.Circle
                            OrgListCheckState.CHECKED -> FontAwesomeIcons.Solid.CheckCircle
                            OrgListCheckState.PARTIAL -> FontAwesomeIcons.Regular.DotCircle
                            OrgListCheckState.UNCHECKED -> FontAwesomeIcons.Regular.Circle
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Checked icon",
                            modifier = Modifier
                                .padding(start = 5.dp, end = 5.dp)
                                .size(20.dp),
                            tint = if (item.checkbox == null) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }

                    val style = if (item.checkbox == OrgListCheckState.CHECKED) {
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }

                    for (paragraph in item.content.filter { it is OrgChunk.OrgParagraph }) {
                        OrgParagraphView(
                            paragraph as OrgChunk.OrgParagraph,
                            style = style
                        )
                    }

                    // TODO: Display nested lists also
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
            .padding(vertical = 10.dp)
    ) {
        for ((idx, item) in orglist.items.withIndex()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp)
            ) {
                if (item is OrgList.OrgListItem) {
                    Text(
                        text = "${idx + 1}. ".padStart(4),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (hasCheckbox) {
                        val icon = when (item.checkbox) {
                            null -> FontAwesomeIcons.Solid.Circle
                            OrgListCheckState.CHECKED -> FontAwesomeIcons.Solid.CheckCircle
                            OrgListCheckState.PARTIAL -> FontAwesomeIcons.Regular.DotCircle
                            OrgListCheckState.UNCHECKED -> FontAwesomeIcons.Regular.Circle
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Checked icon",
                            modifier = Modifier
                                .padding(start = 5.dp, end = 5.dp)
                                .size(20.dp),
                            tint = if (item.checkbox == null) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }

                    val style = if (item.checkbox == OrgListCheckState.CHECKED) {
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }

                    for (paragraph in item.content.filter { it is OrgChunk.OrgParagraph }) {
                        OrgParagraphView(
                            paragraph as OrgChunk.OrgParagraph,
                            style = style
                        )
                    }

                    // TODO: Display nested lists also
                }
            }
        }
    }
}