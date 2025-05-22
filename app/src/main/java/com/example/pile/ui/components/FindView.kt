package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Thumbtack

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun FindView(
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    createAndOpenNode: (nodeTitle: String, nodeType: OrgNodeType, refLink: String?, tags: List<String>?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showPinnedDialog by remember { mutableStateOf(false) }

    val recentNodes by viewModel.recentNodes.collectAsState()
    val randomNodes by viewModel.randomNodes.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    viewModel.generateNewRandomNodes()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        if (recentNodes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (text == "") {
                    Text(
                        text = "Pile",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = "Welcome to your org-roam pile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    RandomNodeList(randomNodes, openNodeById)
                    RecentNodeList(recentNodes, openNodeById)
                } else {
                    NodeList(
                        nodes = searchResults,
                        heading = null,
                        onClick = openNodeById
                    )
                }
            }

            if (showPinnedDialog) {
                PinnedDialog(
                    emptyList(),
                    { showPinnedDialog = false },
                    openNodeById
                )
            }

            if (text != "") {
                CreateNodeButton(text) { nodeTitle, nodeType ->
                    createAndOpenNode(
                        nodeTitle,
                        nodeType,
                        null,
                        null
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showPinnedDialog = true },
                    modifier = Modifier
                        .padding(end = 10.dp, top = 20.dp),
                ) {
                    Icon(
                        FontAwesomeIcons.Solid.Thumbtack,
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Pinned Nodes"
                    )
                }
                FindField(
                    text = text,
                    onTextEntry = {
                        text = it
                        viewModel.setSearchQuery(it)
                    },
                    label = "Find or Create",
                    placeholder = "Node Name"
                )
            }
        }
    }
}

@Composable
fun RandomNodeList(nodes: List<OrgNode>, onClick: (String) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        border = BorderStroke(0.dp, Color.Transparent),
        shape = RoundedCornerShape(10.dp)
    ) {
        NodeList(nodes,"Random", onClick)
    }
}

@Composable
fun RecentNodeList(nodes: List<OrgNode>, onClick: (String) -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        NodeList(
            nodes,
            "Recently Modified",
            onClick
        )
    }
}