package com.example.pile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pile.OrgNode
import com.example.pile.isLiteratureNode
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bookmark

/* View for one node */
@Composable
fun OrgNodeItem(node: OrgNode, onClick: (OrgNode) -> Unit) {
    Column(modifier = Modifier
        .padding(vertical = 5.dp)
        .fillMaxWidth()
        .clickable { onClick(node) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLiteratureNode(node)) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Bookmark,
                    modifier = Modifier.size(14.dp).padding(end = 5.dp),
                    contentDescription = "Literature Node",
                    tint = Color.Gray
                )
            }
            Text(
                text = node.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}