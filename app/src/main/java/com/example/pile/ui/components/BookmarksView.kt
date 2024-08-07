package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.isLiteratureNode
import com.example.pile.isUnsortedNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit) {
    var text by remember { mutableStateOf("") }

    val bookmarkNodes = nodes.filter { isLiteratureNode(it) }

    // Filter criteria. We will add categories (via fixed tags) and tags later.
    var onlyUnsorted by remember { mutableStateOf(true) }

    val filteredNodes = bookmarkNodes
        .shuffled()
        .filter { text.lowercase() in it.title.lowercase() }
        .filter {
            if (onlyUnsorted) {
                isUnsortedNode(it)
            } else {
                true
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        if (nodes.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxSize(),
                    border = BorderStroke(0.dp, Color.Transparent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    NodeList(
                        filteredNodes,
                        "${filteredNodes.count()} matches",
                        openNode,
                        expandedView = true
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = onlyUnsorted, onCheckedChange = { onlyUnsorted = it })
                Text("Only unsorted", modifier = Modifier.padding(start = 10.dp))
            }

            FindField(text = text, onTextEntry = { text = it }, label = "Find", placeholder = "Node Name")
        }
    }
}