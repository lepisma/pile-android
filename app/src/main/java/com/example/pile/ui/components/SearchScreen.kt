package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.pile.OrgNode
import com.example.pile.ui.theme.PileTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(nodeList: List<OrgNode>, isLoading: Boolean, openNode: (String) -> Unit) {
    PileTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    SearchView(nodeList, openNode)
                }
            }
        }
    }
}

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun SearchView(nodes: List<OrgNode>, openNode: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column {
        if (nodes.isNotEmpty()) {
            if (text == "") {
                RandomNodeList(nodes, openNode)
                RecentNodeList(nodes, openNode)
            } else {
                SearchNodeList(nodes, text, openNode)
            }
        }
        SearchCreateField(text = text, onTextEntry = { text = it })
    }
}

@Composable
fun RandomNodeList(nodes: List<OrgNode>, openNode: (String) -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
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
                items(nodes.shuffled().take(5)) { node ->
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
                items(nodes.sortedByDescending { it.datetime }.take(5)) { node ->
                    OrgNodeItem(node) { openNode(node.id) }
                }
            }
        }
    }
}

/* Clickable list of nodes that open edit/read view */
@Composable
fun SearchNodeList(nodes: List<OrgNode>, searchString: String, openNode: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(nodes.filter {
            searchString.lowercase() in it.title.lowercase()
        }.sortedBy {
            searchString.length / it.title.length
        }.take(10)) { node ->
            OrgNodeItem(node) { openNode(node.id) }
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
        .padding(vertical = 5.dp)
        .fillMaxWidth()
        .clickable { onClick(node) }
    ) {
        Text(node.title)
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}