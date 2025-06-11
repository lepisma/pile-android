package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Row(
        modifier = modifier
            .padding(vertical = 5.dp)
    ) {
        for (item in orglist.items) {
            if (item is OrgList.OrgListItem) {
                if (!hasCheckbox) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Circle,
                        contentDescription = "List start icon",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 5.dp)
                            .size(8.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant,
                    )
                } else {
                    val icon = when(item.checkbox) {
                        null -> FontAwesomeIcons.Regular.Circle
                        OrgListCheckState.CHECKED -> FontAwesomeIcons.Solid.CheckCircle
                        OrgListCheckState.PARTIAL -> FontAwesomeIcons.Regular.DotCircle
                        OrgListCheckState.UNCHECKED -> FontAwesomeIcons.Regular.Circle
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Checked icon",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 5.dp)
                            .size(25.dp),
                        tint = if (item.checkbox == null) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }

                for (paragraph in item.content.filter { it is OrgChunk.OrgParagraph }) {
                    OrgParagraphView(paragraph as OrgChunk.OrgParagraph)
                }

                // TODO: Display nested lists also
            }
        }
    }
}