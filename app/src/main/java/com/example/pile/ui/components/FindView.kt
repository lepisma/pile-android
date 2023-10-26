package com.example.pile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun FindView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit, createAndOpenNode: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            if (text == "") {
                RandomNodeList(nodes, openNode)
                RecentNodeList(nodes, openNode)
            } else {
                FindNodeList(nodes, text) { openNode(it) }
                if (text != "") {
                    CreateButton(text, createAndOpenNode)
                }
            }
        }
        FindCreateField(text = text, onTextEntry = { text = it })
    }
}

@Composable
fun RandomNodeList(nodes: List<OrgNode>, onClick: (OrgNode) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        border = BorderStroke(0.dp, Color.Transparent),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Random",
                color = Color.Gray,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            LazyColumn {
                items(nodes.shuffled().take(3)) { node ->
                    OrgNodeItem(node) { onClick(node) }
                }
            }
        }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Recent",
                color = Color.Gray,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            LazyColumn {
                items(nodes.sortedByDescending { it.datetime }.take(3)) { node ->
                    OrgNodeItem(node) { onClick(node) }
                }
            }
        }
    }
}

/* Clickable list of nodes that open edit/read view */
@Composable
fun FindNodeList(nodes: List<OrgNode>, searchString: String, onClick: (OrgNode) -> Unit) {
    if (searchString.trim() != "") {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(nodes.filter {
                searchString.lowercase() in it.title.lowercase()
            }.sortedBy {
                searchString.length / it.title.length
            }.take(5)) { node ->
                OrgNodeItem(node) { onClick(node) }
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
fun FindCreateField(text: String, onTextEntry: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = onTextEntry,
        label = { Text(text = "Find or Create") },
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