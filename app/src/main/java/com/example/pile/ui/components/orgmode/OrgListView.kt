package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgChunk
import com.example.pile.orgmode.OrgList
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CheckCircle
import compose.icons.fontawesomeicons.solid.Circle

@Composable
fun OrgListView(orglist: OrgList.OrgUnorderedList, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(vertical = 5.dp)
    ) {
        Icon(
            imageVector = FontAwesomeIcons.Solid.Circle,
            contentDescription = "List start icon",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 5.dp)
                .size(8.dp),
            tint = MaterialTheme.colorScheme.outlineVariant,
        )

        Icon(
            imageVector = FontAwesomeIcons.Solid.CheckCircle,
            // imageVector = FontAwesomeIcons.Regular.DotCircle,
            // imageVector = FontAwesomeIcons.Regular.Circle,
            contentDescription = "Checked icon",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(horizontal = 5.dp)
                .size(25.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        if (orglist.checkbox != null) {
            Text(orglist.checkbox.toString())
        }

        val firstItem = orglist.items[0]

        when (firstItem) {
            is OrgList.OrgListItem -> {
                for (paragraph in firstItem.content.filter { it is OrgChunk.OrgParagraph }) {
                    OrgParagraphView(paragraph as OrgChunk.OrgParagraph)
                }
            }
            else -> {}
        }
    }
}