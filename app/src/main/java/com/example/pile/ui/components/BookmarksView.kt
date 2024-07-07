package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.isLiteratureNode
import com.example.pile.isUnsortedNode

@Composable
fun BookmarksView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            val randomNodes = remember {
                nodes
                    .shuffled()
                    .filter { isLiteratureNode(it) }
                    .filter { isUnsortedNode(it) }
                    .take(5)
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                border = BorderStroke(0.dp, Color.Transparent),
                shape = RoundedCornerShape(10.dp)
            ) {
                NodeList(randomNodes,"Random unsorted bookmarks", openNode, expandedView = true)
            }
        }
    }
}