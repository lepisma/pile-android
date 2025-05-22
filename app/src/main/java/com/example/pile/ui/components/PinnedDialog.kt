package com.example.pile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pile.OrgNode


@Composable
fun PinnedDialog(nodes: List<OrgNode>, onDismiss: () -> Unit, openNodeById: (String) -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box {
            Card {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    val pinnedNodes = nodes
                        .filter { it.pinned }
                        .sortedBy { it.title }

                    if (pinnedNodes.isNotEmpty()) {
                        NodeList(
                            nodes = pinnedNodes,
                            heading = "Pinned Nodes",
                            onClick = openNodeById
                        )
                    } else {
                        Text(text = "No pinned nodes")
                    }
                }
            }
        }
    }
}