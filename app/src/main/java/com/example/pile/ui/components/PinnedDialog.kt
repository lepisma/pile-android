package com.example.pile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pile.OrgNode


@Composable
fun PinnedDialog(nodes: List<OrgNode>, onDismiss: () -> Unit, openNode: (OrgNode) -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box {
            Card {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    if (nodes.isNotEmpty()) {
                        NodeList(
                            nodes = nodes
                                .filter { it.pinned }
                                .sortedBy { it.title },
                            heading = "Pinned Nodes",
                            onClick = openNode
                        )
                    }
                }
            }
        }
    }
}