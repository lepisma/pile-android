package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.ui.theme.PileTheme
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.solid.Book
import compose.icons.fontawesomeicons.solid.CalendarDay
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Heart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    nodeList: List<OrgNode>,
    isLoading: Boolean,
    selectedNavIndex: Int,
    setSelectedNavIndex: (Int) -> Unit,
    openNode: (OrgNode) -> Unit,
    createAndOpenNode: (String, OrgNodeType, String?) -> Unit,
    refreshDatabase: () -> Unit,
    captureLinkInitial: String?
) {
    val toCapture = captureLinkInitial != null

    var showCaptureSheet by remember { mutableStateOf(toCapture) }
    val captureSheetState = rememberModalBottomSheetState()

    var captureLink by remember { mutableStateOf(captureLinkInitial ?: "") }
    var captureTitle by remember { mutableStateOf("") }

    if (toCapture) {
        LaunchedEffect(captureLink) {
            captureTitle = try {
                withContext(Dispatchers.IO) {
                    val document = Jsoup.connect(captureLink).get()
                    document.title()
                }
            } catch (e: Exception) {
                "Link title resolution failed"
            }
        }
    }

    PileTheme {
        Scaffold (
            topBar = {
                Box {
                    TopAppBar(
                        title = { Text("") },
                        actions = {
                            IconButton(onClick = { refreshDatabase() }, enabled = !isLoading) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh Database"
                                )
                            }
                        }
                    )
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (selectedNavIndex) {
                        0 -> FindView(nodeList, openNode, createAndOpenNode)
                        1 -> JournalView(nodeList, openNode, createAndOpenNode)
                        2 -> SearchView()
                        3 -> BookmarksView(nodeList, openNode)
                    }

                    NavigationBar {
                        listOf(
                            Pair("Notes", FontAwesomeIcons.Solid.Book),
                            Pair("Journal", FontAwesomeIcons.Solid.CalendarDay),
                            Pair("Search", FontAwesomeIcons.Solid.Glasses),
                            Pair("Bookmarks", FontAwesomeIcons.Solid.Heart)
                        ).forEachIndexed { index, (label, icon) ->
                            NavigationBarItem(
                                selected = (selectedNavIndex == index),
                                onClick = { setSelectedNavIndex(index) },
                                icon = {
                                    Icon(
                                        icon,
                                        modifier = Modifier.size(18.dp),
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) })
                        }
                    }

                    if (showCaptureSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showCaptureSheet = false },
                            sheetState = captureSheetState
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 40.dp, vertical = 40.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Regular.Bookmark,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 10.dp),
                                        contentDescription = "Literature Node"
                                    )
                                    Text(
                                        "New Literature Node",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                                TextField(
                                    value = captureLink,
                                    onValueChange = { captureLink = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp)
                                )
                                OutlinedTextField(
                                    value = captureTitle,
                                    onValueChange = { captureTitle = it },
                                    label = { Text("Node title") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp)
                                )

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    FilledTonalButton(onClick = {
                                        createAndOpenNode(captureTitle, OrgNodeType.LITERATURE, captureLink)
                                    }) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Save",
                                                modifier = Modifier.padding(end = 10.dp)
                                            )
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}