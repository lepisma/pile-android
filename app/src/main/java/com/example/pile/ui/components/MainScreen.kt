package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.ui.theme.PileTheme
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Book
import compose.icons.fontawesomeicons.solid.CalendarDay
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Heart
import compose.icons.fontawesomeicons.solid.PenNib


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    nodeList: List<OrgNode>,
    isLoading: Boolean,
    selectedNavIndex: Int,
    setSelectedNavIndex: (Int) -> Unit,
    openNode: (OrgNode) -> Unit,
    createAndOpenNode: (String) -> Unit,
    refreshDatabase: () -> Unit
) {
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
                        1 -> JournalView(nodeList, openNode)
                        2 -> SearchView()
                        3 -> BookmarksView(nodeList, openNode)
                        4 -> ScratchView()
                    }

                    NavigationBar {
                        listOf(
                            Pair("Notes", FontAwesomeIcons.Solid.Book),
                            Pair("Journal", FontAwesomeIcons.Solid.CalendarDay),
                            Pair("Search", FontAwesomeIcons.Solid.Glasses),
                            Pair("Bookmarks", FontAwesomeIcons.Solid.Heart),
                            Pair("Scratch", FontAwesomeIcons.Solid.PenNib)
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
                }
            }
        }
    }
}