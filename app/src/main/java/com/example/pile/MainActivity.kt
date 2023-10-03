package com.example.pile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pile.ui.theme.PileTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_OPEN_FOLDER = 1234
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
    private fun setupContent(uri: Uri) {
        val context = this

        setContent {
            val navController = rememberNavController()

            var isLoading by remember { mutableStateOf(true) }
            var nodeList by remember { mutableStateOf(listOf<OrgNode>()) }

            NavHost(navController = navController, startDestination = "main-screen") {
                composable("main-screen") {

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
                                        .padding(horizontal = 20.dp, vertical = 20.dp),
                                    color = MaterialTheme.colorScheme.background
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
                composable("nodeScreen/{nodeId}") { navBackStackEntry ->
                    val nodeId = navBackStackEntry.arguments?.getString("nodeId")
                    val node = nodeList.find { it.id == nodeId }
                    if (node != null) {
                        NodeScreen(node)
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
        }.take(10)) {node ->
            OrgNodeItem(node) {
                navController.navigate("nodeScreen/${node.id}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(node: OrgNode) {
    val context = LocalContext.current
    val fileContent = readOrgContent(context, node.file)

    PileTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(node.title)
                    },
                    actions = {
                        IconButton(onClick = { println("clicked") }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share"
                            )
                        }
                    },
                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                        rememberTopAppBarState()
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { println("pressed save") }) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Save")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = fileContent)
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