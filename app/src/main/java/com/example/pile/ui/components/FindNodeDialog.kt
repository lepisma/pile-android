package com.example.pile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.isDailyNode

/**
 * Dialog that allows finding nodes and creating new ones. For me main view, you should be relying
 * on FindView. This is a minimal version to be embedded in, say, editing environment.
 *
 * @param nodes List of all nodes.
 * @param onClick Function that runs when you click on any shown node.
 * @param onDismiss Function that runs on dismissal.
 * @param onCreateClick Function that runs after you click on create button. It takes name of the
 *                      new node as an argument.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindNodeDialog(
    nodes: List<OrgNode>,
    onClick: (OrgNode) -> Unit,
    onDismiss: () -> Unit,
    onCreateClick: (nodeTitle: String, nodeType: OrgNodeType) -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Card (modifier = Modifier.padding(bottom = 60.dp)) {
                var text by remember { mutableStateOf("") }

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    if (text == "") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            LazyColumn {
                                items(
                                    nodes
                                        .sortedByDescending { it.datetime }
                                        .filter { !isDailyNode(it) }
                                        .take(5)
                                ) { node ->
                                    OrgNodeItem(node, expandedView = false) { onClick(node) }
                                }
                            }
                        }
                    } else {
                        NodeList(
                            nodes = nodes
                                .filter { text.lowercase() in it.title.lowercase() }
                                .sortedBy { text.length / it.title.length }
                                .take(5),
                            heading = null,
                            onClick = { /* TODO onClick(it) */ }
                        )
                        CreateNodeButton(text) { title, nodeType -> onCreateClick(title, nodeType) }
                    }
                    FindField(
                        text = text,
                        onTextEntry = { text = it },
                        label = "Insert",
                        placeholder = "Node Name"
                    )
                }
            }
        }
    }
}