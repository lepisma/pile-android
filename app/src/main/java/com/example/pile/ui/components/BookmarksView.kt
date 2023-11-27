package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode

@Composable
fun BookmarksView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            NodeList(
                nodes = nodes
                    .filter { it.bookmarked }
                    .sortedBy { it.title },
                heading = null,
                onClick = openNode
            )
        }
    }
}