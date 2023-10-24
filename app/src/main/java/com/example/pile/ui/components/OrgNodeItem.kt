package com.example.pile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pile.OrgNode

/* View for one node */
@Composable
fun OrgNodeItem(node: OrgNode, onClick: (OrgNode) -> Unit) {
    Column(modifier = Modifier
        .padding(vertical = 5.dp)
        .fillMaxWidth()
        .clickable { onClick(node) }
    ) {
        Text(node.title)
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}