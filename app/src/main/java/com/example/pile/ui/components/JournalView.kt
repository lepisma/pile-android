package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.isDailyNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            if (text == "") {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    NodeList(
                        nodes
                            .sortedByDescending { it.datetime }
                            .filter { isDailyNode(it) }
                            .take(5),
                        heading = "Recent",
                        onClick = openNode
                    )
                }
            } else {
                NodeList(
                    nodes = nodes
                        .filter { isDailyNode(it) }
                        .filter { text.lowercase() in it.title.lowercase() }
                        .take(5),
                    heading = null,
                    onClick = openNode
                )
            }
        }
        FindField(text = text, onTextEntry = { text = it }, label = "Find", placeholder = "Node Name")
    }
}