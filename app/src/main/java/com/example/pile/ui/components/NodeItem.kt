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
import com.example.pile.data.OrgNode
import com.example.pile.data.isDailyNode
import com.example.pile.data.isLiteratureNode
import com.example.pile.data.isUnsortedNode
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.regular.Calendar
import compose.icons.fontawesomeicons.solid.Bookmark
import java.time.format.DateTimeFormatter

/* View for one node */
@Composable
fun NodeItem(node: OrgNode, onClick: () -> Unit) {
    Column(modifier = Modifier
        .padding(vertical = 5.dp)
        .fillMaxWidth()
        .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLiteratureNode(node)) {
                Icon(
                    imageVector = if (isUnsortedNode(node)) FontAwesomeIcons.Regular.Bookmark else FontAwesomeIcons.Solid.Bookmark,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 5.dp),
                    contentDescription = "Literature Node",
                    tint = Color.Gray
                )
            } else if (isDailyNode(node)) {
                Icon(
                    imageVector = FontAwesomeIcons.Regular.Calendar,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 5.dp),
                    contentDescription = "Daily Node",
                    tint = Color.Gray
                )
            }
            Text(
                text = node.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val dtFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
        Text(node.datetime.format(dtFormatter), fontSize = 10.sp, color = Color.Gray)
    }
}