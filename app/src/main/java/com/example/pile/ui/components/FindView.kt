package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.isDailyNode

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun FindView(
    nodes: List<OrgNode>,
    openNode: (OrgNode) -> Unit,
    createAndOpenNode: (nodeTitle: String, nodeType: OrgNodeType, refLink: String?, tags: List<String>?) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            if (text == "") {
                RandomNodeList(nodes, openNode)
                RecentNodeList(nodes, openNode)
            } else {
                NodeList(
                    nodes = nodes
                        .filter { text.lowercase() in it.title.lowercase() }
                        .sortedBy { text.length / it.title.length }
                        .take(5),
                    heading = null,
                    onClick = { openNode(it) }
                )
                if (text != "") {
                    CreateNodeButton(text) { nodeTitle, nodeType -> createAndOpenNode(nodeTitle, nodeType, null, null) }
                }
            }
        }
        FindField(text = text, onTextEntry = { text = it }, label = "Find or Create", placeholder = "Node Name")
    }
}

@Composable
fun RandomNodeList(nodes: List<OrgNode>, onClick: (OrgNode) -> Unit) {
    val randomNodes = remember {
        nodes
            .shuffled()
            .filter { !isDailyNode(it) }
            .take(3)
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        border = BorderStroke(0.dp, Color.Transparent),
        shape = RoundedCornerShape(10.dp)
    ) {
        NodeList(randomNodes,"Random", onClick)
    }
}

@Composable
fun RecentNodeList(nodes: List<OrgNode>, onClick: (OrgNode) -> Unit) {
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
                .filter { !isDailyNode(it) }
                .take(3),
            "Recent",
            onClick
        )
    }
}