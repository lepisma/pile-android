package com.example.pile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.pile.ui.components.NodeScreen
import com.example.pile.ui.components.SearchScreen
import com.example.pile.viewmodel.SharedViewModel
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_OPEN_FOLDER = 1234
    }

    private lateinit var nodeDao: NodeDao
    private val viewModel = SharedViewModel()

    private fun setupContent(uri: Uri) {
        val context = this

        setContent {
            val navController = rememberNavController()
            var isLoading by remember { mutableStateOf(true) }
            var nodeList by remember { mutableStateOf(listOf<OrgNode>()) }

            NavHost(navController = navController, startDestination = "main-screen") {
                composable(
                    "main-screen",
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left
                        )
                    },
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right
                        )
                    }
                ) {
                    SearchScreen(
                        nodeList,
                        isLoading,
                        { navController.navigate("nodeScreen/${it}") },
                        {
                            CoroutineScope(Dispatchers.IO).launch {
                                createNewNode(context, it, uri)?.let { node ->
                                    nodeDao.insert(node)
                                    withContext(Dispatchers.Main) {
                                        nodeList = nodeList + listOf(node)
                                        navController.navigate("nodeScreen/${node.id}")
                                    }
                                }
                            }
                        },
                        {
                            CoroutineScope(Dispatchers.IO).launch {
                                isLoading = true
                                refreshDatabase(context, uri, nodeDao)
                                nodeList = loadNodes(context, nodeDao)
                                isLoading = false
                            }
                        })
                }
                composable(
                    "nodeScreen/{nodeId}",
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right
                        )
                    }
                ) { navBackStackEntry ->
                    val nodeId = navBackStackEntry.arguments?.getString("nodeId")
                    val node = nodeList.find { it.id == nodeId }
                    if (node != null) {
                        NodeScreen(node, viewModel, { navController.popBackStack() }) {
                            navController.navigate("nodeScreen/${it}")
                        }
                    }
                }
            }

            LaunchedEffect(uri) {
                nodeList = withContext(Dispatchers.IO) {
                    loadNodes(context, nodeDao)
                }
                isLoading = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = loadRootPath(this)

        val db = Room.databaseBuilder(
            applicationContext,
            PileDatabase::class.java, "pile-database"
        ).build()
        nodeDao = db.nodeDao()

        if (uri != null) {
            setupContent(uri)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_CODE_OPEN_FOLDER)
        }

        viewModel.fileToEdit.observe(this) { (file, text) ->
            writeFile(this, file, text)
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show()
        }
    }

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