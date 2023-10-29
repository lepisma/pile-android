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
import com.example.pile.isDailyNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertLinkDialog(
    nodes: List<OrgNode>,
    onClick: (OrgNode) -> Unit,
    onDismiss: () -> Unit,
    createAndOpenNode: (String) -> Unit
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
                                    OrgNodeItem(node) { onClick(node) }
                                }
                            }
                        }
                    } else {
                        FindNodeList(nodes, text) { onClick(it) }
                        CreateButton(text, createAndOpenNode)
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