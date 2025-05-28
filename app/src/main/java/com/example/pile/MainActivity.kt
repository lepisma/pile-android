package com.example.pile

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.example.pile.data.LinkDao
import com.example.pile.data.MIGRATION_1_2
import com.example.pile.data.MIGRATION_2_3
import com.example.pile.data.MIGRATION_3_4
import com.example.pile.data.MIGRATION_4_5
import com.example.pile.data.MIGRATION_6_7
import com.example.pile.data.Migration_5_6
import com.example.pile.data.NodeDao
import com.example.pile.data.NodeTagsDao
import com.example.pile.data.PileDatabase
import com.example.pile.data.TagDao
import com.example.pile.data.loadRootPath
import com.example.pile.data.saveRootPath
import com.example.pile.ui.components.landingscreen.LandingScreen
import com.example.pile.ui.components.mainscreen.MainScreen
import com.example.pile.ui.components.nodescreen.NodeScreen
import com.example.pile.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    private lateinit var nodeDao: NodeDao
    private lateinit var tagDao: TagDao
    private lateinit var nodeTagsDao: NodeTagsDao
    private lateinit var linkDao: LinkDao

    private lateinit var viewModel: SharedViewModel
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>
    private var navController: NavHostController? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialRootUri = loadRootPath(this)
        var currentRootUri by mutableStateOf(initialRootUri)

        val db = Room.databaseBuilder(
            applicationContext,
            PileDatabase::class.java, "pile-database"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                Migration_5_6(applicationContext),
                MIGRATION_6_7
            )
            .build()
        nodeDao = db.nodeDao()
        tagDao = db.tagDao()
        nodeTagsDao = db.nodeTagsDao()
        linkDao = db.linkDao()

        viewModel = SharedViewModel(
            nodeDao = nodeDao,
            tagDao = tagDao,
            nodeTagsDao = nodeTagsDao,
            linkDao = linkDao,
            applicationContext = this
        )
        currentRootUri?.let { viewModel.setRootUri(it) }

        // Sync database on start
        viewModel.syncDatabase()

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