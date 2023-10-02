package com.example.pile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.InetAddresses
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.renderscript.RSRuntimeException
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.node.modifierElementOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pile.ui.theme.PileTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_OPEN_FOLDER = 1234
    }

    @OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun setupContent(uri: Uri) {
        val context = this

        setContent {
            var isLoading by remember { mutableStateOf(true) }
            var nodeList by remember { mutableStateOf(listOf<OrgNode>()) }

            PileTheme {
                Scaffold (
                    topBar = {
                        TopAppBar (
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("pile-android")
                            },
                            actions = {
                                IconButton(onClick = { println("clicked") }, enabled = !isLoading) {
                                    Icon (
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
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!isLoading) {
                            Surface (
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Column (
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    SearchView(nodeList)
                                }
                            }
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            LaunchedEffect(uri) {
                nodeList = withContext(Dispatchers.IO) {
                    readFilesFromDirectory(context, uri)
                }
                isLoading = false
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = loadRootPath(this)

        if (uri != null) {
            println(":: Found path in shared preference: ${uri.toString()}")
            setupContent(uri)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_CODE_OPEN_FOLDER)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Deprecated("?")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val context = this
        if (requestCode == REQUEST_CODE_OPEN_FOLDER && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                saveRootPath(this, uri)
                setupContent(uri)
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
    var showDialog by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<OrgNode?>(null) }

    LazyColumn {
        items(nodes.filter {
            searchString.lowercase() in it.title.lowercase()
        }.sortedBy {
            searchString.length / it.title.length
        }.take(10)) {node ->
            OrgNodeItem(node) {
                selectedNode = it
                showDialog = true
            }
        }
    }

    if (showDialog && selectedNode != null) {
        NodeDialog(node = selectedNode!!, onClose = { showDialog = false })
    }
}

@Composable
fun NodeDialog(node: OrgNode, onClose: () -> Unit) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onClose) {
        val context = LocalContext.current
        val fileContent = readOrgContent(context, node.file)
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(5.dp)) {
            Text(
                node.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 20.dp)
            )
            Text(text = fileContent)
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
fun OrgNodeItem(node: OrgNode, onClick: (OrgNode) -> Unit) {
    Column( modifier = Modifier
        .padding(5.dp)
        .clickable { onClick(node) }
    ) {
        Text(node.title)
        Text(node.datetime.toString(), fontSize = 10.sp, color = Color.Gray)
    }
}

fun saveRootPath(context: Context, uri: Uri) {
    val sharedPref = context.getSharedPreferences("pile", Context.MODE_PRIVATE)
    with (sharedPref.edit()) {
        putString("root-path", uri.toString())
        apply()
    }

    /* Also take persistent permissions */
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
}

/* Return the saved root path if any */
fun loadRootPath(context: Context): Uri? {
    val saved = context.getSharedPreferences("pile", Context.MODE_PRIVATE).getString("root-path", null)

    return if (saved != null) {
        val uri = Uri.parse(saved)
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        uri
    } else {
        null
    }
}