package com.example.pile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.pile.ui.components.LandingScreen
import com.example.pile.ui.components.MainScreen
import com.example.pile.ui.components.NodeScreen
import com.example.pile.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var nodeDao: NodeDao
    private lateinit var viewModel: SharedViewModel
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialRootUri = loadRootPath(this)
        var currentRootUri by mutableStateOf(initialRootUri)

        val db = Room.databaseBuilder(
            applicationContext,
            PileDatabase::class.java, "pile-database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
        nodeDao = db.nodeDao()
        viewModel = SharedViewModel(
            nodeDao = nodeDao,
            rootUri = currentRootUri,
            applicationContext = this
        )

        folderPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.also { uri ->
                    saveRootPath(this, uri)
                    currentRootUri = uri
                    viewModel.setRootUri(uri)
                }
            } else {
                Toast.makeText(this, "Folder selection cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        val captureLink = if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        setContent {
            navController = rememberNavController()
            val startDestination = if (currentRootUri != null) "main-screen" else "landing-screen"

            NavHost(navController = navController!!, startDestination = startDestination) {
                composable(
                    "landing-screen",
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
                    LandingScreen(
                        onOpenFolderClicked = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            folderPickerLauncher.launch(intent)
                        }
                    )
                }
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
                    // Once currentRootUri is set, we navigate to main-screen
                    LaunchedEffect(currentRootUri) {
                        currentRootUri?.let { uri ->
                            if (navController!!.currentDestination?.route == "landing-screen") {
                                navController!!.navigate("main-screen") {
                                    popUpTo("landing-screen") { inclusive = true }
                                }
                            }
                        }
                    }

                    MainScreen(
                        viewModel = viewModel,
                        openNodeById = { navController!!.navigate("nodeScreen/${it}") },
                        createAndOpenNode = { title, nodeType, nodeRef, tags ->
                            CoroutineScope(Dispatchers.IO).launch {
                                currentRootUri?.let { uri ->
                                    createNewNode(this@MainActivity, title, uri, nodeType, nodeRef, tags)?.let { node ->
                                        nodeDao.insert(node)
                                        withContext(Dispatchers.Main) {
                                            navController!!.navigate("nodeScreen/${node.id}")
                                        }
                                    }
                                }
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
                    if (nodeId != null) {
                        NodeScreen(
                            nodeId = nodeId,
                            viewModel = viewModel,
                            openNodeById = { navController!!.navigate("nodeScreen/${it}") },
                            goBack = { navController!!.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}