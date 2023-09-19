package com.example.pile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pile.ui.theme.PileTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_OPEN_FOLDER = 1234
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_OPEN_FOLDER)

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Deprecated("?")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val context = this
        if (requestCode == REQUEST_CODE_OPEN_FOLDER && resultCode == Activity.RESULT_OK) {
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
                            data?.data?.also {
                                it -> SearchView(readFilesFromDirectory(context, it))
                            }
                        }
                    }
                }
            }
            data?.data?.also {
                uri -> println(uri)
            }
        }
    }
}

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun SearchView(nodes: List<OrgNode>) {
    var text by remember { mutableStateOf("") }

    Column {
        OrgNodeList(nodes, text)
        SearchCreateField(text = text, onTextEntry = { text = it })
    }
}

/* Clickable list of nodes that open edit/read view */
@Composable
fun OrgNodeList(nodes: List<OrgNode>, searchString: String) {
    LazyColumn {
        items(nodes.filter {
            searchString.lowercase() in it.title.lowercase()
        }.sortedBy {
            searchString.length / it.title.length
        }) {
            node -> OrgNodeItem(node)
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
        placeholder = { Text(text = "Node name") }
    )
}

/* View for one node */
@Composable
fun OrgNodeItem(node: OrgNode) {
    Column( modifier = Modifier.padding(5.dp)) {
        Text(node.title)
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}
