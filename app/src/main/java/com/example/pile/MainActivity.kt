package com.example.pile

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.pile.ui.theme.PileTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PileTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        OrgNodeList(nodes = listOf(OrgNode("Test", ""), OrgNode("Test B", "")))
                        SearchCreateField()
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SearchCreateField() {
    var text by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(text = "Search or Create") },
            placeholder = { Text(text = "Node name") }
        )
    }
}

@Composable
fun OrgNodeList(nodes: List<OrgNode>) {
    LazyColumn {
        items(nodes) { node -> OrgNodeItem(node) }
    }
}

@Composable
fun OrgNodeItem(node: OrgNode) {
    Text(node.title)
}
