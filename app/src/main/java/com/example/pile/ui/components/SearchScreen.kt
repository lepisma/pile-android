package com.example.pile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pile.OrgNode
import com.example.pile.ui.theme.PileTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(nodeList: List<OrgNode>, isLoading: Boolean, navController: NavController) {
    PileTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("pile-android")
                    },
                    actions = {
                        IconButton(onClick = { println("clicked") }, enabled = !isLoading) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Sync database"
                            )
                        }
                    },
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
                        rememberTopAppBarState()
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                if (isLoading) {
                    LinearProgressIndicator (
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        SearchView(nodeList, navController)
                    }
                }
            }
        }
    }
}

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun SearchView(nodes: List<OrgNode>, navController: NavController) {
    var text by remember { mutableStateOf("") }

    Column {
        OrgNodeList(nodes, text, navController)
        SearchCreateField(text = text, onTextEntry = { text = it })
    }
}

/* Clickable list of nodes that open edit/read view */
@Composable
fun OrgNodeList(nodes: List<OrgNode>, searchString: String, navController: NavController) {
    LazyColumn {
        items(nodes.filter {
            searchString.lowercase() in it.title.lowercase()
        }.sortedBy {
            searchString.length / it.title.length
        }.take(10)) { node ->
            OrgNodeItem(node) {
                navController.navigate("nodeScreen/${node.id}")
            }
        }
    }
}

/* Input field that searches or creates a new node */
@ExperimentalMaterial3Api
@Composable
fun SearchCreateField(text: String, onTextEntry: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextEntry,
        label = { Text(text = "Search or Create") },
        placeholder = { Text(text = "Node name") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(60.dp)
    )
}

/* View for one node */
@Composable
fun OrgNodeItem(node: OrgNode, onClick: (OrgNode) -> Unit) {
    Column(modifier = Modifier
        .padding(5.dp)
        .clickable { onClick(node) }
    ) {
        Text(node.title)
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}