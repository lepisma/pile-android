package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pile.OrgNode
import com.example.pile.ui.theme.PileTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    nodeList: List<OrgNode>,
    isLoading: Boolean,
    openNode: (String) -> Unit,
    createAndOpenNode: (String) -> Unit,
    refreshDatabase: () -> Unit
) {
    // This will move out
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    PileTheme {
        Scaffold (
            topBar = {
                Box {
                    TopAppBar(
                        title = { Text("") },
                        actions = {
                            IconButton(onClick = { refreshDatabase() }, enabled = !isLoading) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh Database"
                                )
                            }
                        }
                    )
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchView(nodeList, openNode, createAndOpenNode)
                    NavigationBar {
                        NavigationBarItem(
                            selected = true,
                            onClick = { /*TODO*/ },
                            icon = {
                                Icon(Icons.Filled.Search, contentDescription = "Search View")
                            },
                            label = { Text("Search") }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { /*TODO*/ },
                            icon = {
                                Icon(Icons.Filled.DateRange, contentDescription = "Journal")
                            },
                            label = { Text("Journal") }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { /*TODO*/ },
                            icon = {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        }
    }
}

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun SearchView(nodes: List<OrgNode>, openNode: (String) -> Unit, createAndOpenNode: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            if (text == "") {
                RandomNodeList(nodes, openNode)
                RecentNodeList(nodes, openNode)
            } else {
                SearchNodeList(nodes, text, openNode)
                if (text != "") {
                    CreateButton(text, createAndOpenNode)
                }
            }
        }
        SearchCreateField(text = text, onTextEntry = { text = it })
    }
}

@Composable
fun RandomNodeList(nodes: List<OrgNode>, openNode: (String) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        border = BorderStroke(0.dp, Color.Transparent),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                "Random",
                color = Color.Gray,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            LazyColumn {
                items(nodes.shuffled().take(3)) { node ->
                    OrgNodeItem(node) { openNode(node.id) }
                }
            }
        }
    }
}

@Composable
fun RecentNodeList(nodes: List<OrgNode>, openNode: (String) -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                "Recent",
                color = Color.Gray,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            LazyColumn {
                items(nodes.sortedByDescending { it.datetime }.take(3)) { node ->
                    OrgNodeItem(node) { openNode(node.id) }
                }
            }
        }
    }
}

/* Clickable list of nodes that open edit/read view */
@Composable
fun SearchNodeList(nodes: List<OrgNode>, searchString: String, openNode: (String) -> Unit) {
    if (searchString.trim() != "") {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(nodes.filter {
                searchString.lowercase() in it.title.lowercase()
            }.sortedBy {
                searchString.length / it.title.length
            }.take(5)) { node ->
                OrgNodeItem(node) { openNode(node.id) }
            }
        }
    }
}

@Composable
fun CreateButton(nodeName: String, createAndOpenNode: (String) -> Unit) {
    FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = {
        createAndOpenNode(nodeName)
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Create ", color = Color.Gray)
            Text(nodeName)
        }
    }
}

/* Input field that searches or creates a new node */
@ExperimentalMaterial3Api
@Composable
fun SearchCreateField(text: String, onTextEntry: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = onTextEntry,
        label = { Text(text = "Search or Create") },
        placeholder = { Text(text = "Node name") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(60.dp),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

/* View for one node */
@Composable
fun OrgNodeItem(node: OrgNode, onClick: (OrgNode) -> Unit) {
    Column(modifier = Modifier
        .padding(vertical = 5.dp)
        .fillMaxWidth()
        .clickable { onClick(node) }
    ) {
        Text(node.title)
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}