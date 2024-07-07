package com.example.pile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.pile.ui.components.MainScreen
import com.example.pile.ui.components.NodeScreen
import com.example.pile.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_OPEN_FOLDER = 1234
    }

    private lateinit var nodeDao: NodeDao
    private lateinit var viewModel: SharedViewModel

    private var navController: NavHostController? = null
    private var nodeList = mutableStateListOf<OrgNode>()

    private fun setupContent(uri: Uri, captureLink: String? = null) {
        val context = this

        setContent {
            navController = rememberNavController()
            var isLoading by remember { mutableStateOf(true) }
            var currentNode by remember { mutableStateOf<OrgNode?>(null) }
            var selectedNavIndex by remember { mutableIntStateOf(0) }

            NavHost(navController = navController!!, startDestination = "main-screen") {
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
                    MainScreen(
                        nodeList = nodeList,
                        isLoading = isLoading,
                        selectedNavIndex = selectedNavIndex,
                        setSelectedNavIndex = { selectedNavIndex = it },
                        openNode = { navController!!.navigate("nodeScreen/${it.id}") },
                        createAndOpenNode = { title, nodeType, nodeRef, tags ->
                            CoroutineScope(Dispatchers.IO).launch {
                                createNewNode(context, title, uri, nodeType, nodeRef, tags)?.let { node ->
                                    nodeDao.insert(node)
                                    withContext(Dispatchers.Main) {
                                        nodeList.add(node)
                                        navController!!.navigate("nodeScreen/${node.id}")
                                    }
                                }
                            }
                        },
                        refreshDatabase = {
                            CoroutineScope(Dispatchers.IO).launch {
                                isLoading = true
                                refreshDatabase(context, uri, nodeDao)
                                nodeList.clear()
                                nodeList.addAll(loadNodes(context, nodeDao))
                                isLoading = false
                            }
                        },
                        captureLinkInitial = captureLink
                    )
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
                    currentNode = nodeList.find { it.id == nodeId }
                    if (currentNode != null) {
                        NodeScreen(
                            node = currentNode!!,
                            nodes = nodeList,
                            viewModel = viewModel,
                            goBack = { navController!!.popBackStack() },
                            openNodeById = {
                                navController!!.navigate("nodeScreen/${it}")
                            },
                            createNewNode = { title, nodeType, callback ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    createNewNode(context, title, uri, nodeType)?.let { node ->
                                        nodeDao.insert(node)
                                        withContext(Dispatchers.Main) {
                                            nodeList.add(node)
                                            callback(node)
                                        }
                                    }
                                }
                            },
                            onNodeUpdated = { updatedNode ->
                                currentNode = updatedNode
                                nodeList.replaceAll { if (it.id == updatedNode.id) updatedNode else it }
                            }
                        )
                    }
                }
            }

            LaunchedEffect(uri) {
                nodeList.addAll(withContext(Dispatchers.IO) {
                    loadNodes(context, nodeDao)
                })
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
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        nodeDao = db.nodeDao()
        viewModel = SharedViewModel(nodeDao) { file, text ->
            writeFile(this, file, text)
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show()
        }

        if (uri != null) {
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val captureLink = intent.getStringExtra(Intent.EXTRA_TEXT)
                setupContent(uri, captureLink)
            } else {
                setupContent(uri)
            }
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_CODE_OPEN_FOLDER)
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