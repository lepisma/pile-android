package com.example.pile.ui.components.nodescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pile.data.OrgNode
import com.example.pile.data.OrgNodeType
import com.example.pile.ui.components.FindField
import com.example.pile.ui.components.NewNodeButton
import com.example.pile.ui.components.NodeList
import com.example.pile.viewmodel.SharedViewModel

/**
 * Dialog that allows finding nodes and creating new ones. For me main view, you should be relying
 * on FindView. This is a minimal version to be embedded in, say, editing environment.
 *
 * @param onClick Function that runs when you click on any shown node.
 * @param onDismiss Function that runs on dismissal.
 * @param onCreateClick Function that runs after you click on create button. It takes name of the
 *                      new node as an argument.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindNodeDialog(
    viewModel: SharedViewModel,
    onClick: (OrgNode) -> Unit,
    onDismiss: () -> Unit,
    onCreateClick: (nodeTitle: String, nodeType: OrgNodeType) -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val recentNodes by viewModel.recentNodes.collectAsState()

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Card(
                modifier = Modifier
                    .padding(bottom = 60.dp)
            ) {
                var text by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    if (text == "") {
                        Text(
                            text = "Recent Nodes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                        NodeList(
                            nodes = recentNodes.sortedByDescending { it.datetime },
                            heading = null,
                            onClick = onClick,
                            modifier = Modifier
                                .weight(1f, fill = false)
                        )
                    } else {
                        if (searchResults.isEmpty()) {
                            Text(
                                text = "No matches found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 20.dp)
                            )
                        } else {
                            NodeList(
                                nodes = searchResults,
                                heading = null,
                                onClick = onClick,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                            )
                        }
                        NewNodeButton(text) { title, nodeType ->
                            onCreateClick(title, nodeType)
                        }
                    }
                    FindField(
                        text = text,
                        onTextEntry = {
                            text = it
                            viewModel.setSearchQuery(it)
                        },
                        label = "Insert",
                        placeholder = "Node Name"
                    )
                }
            }
        }
    }
}