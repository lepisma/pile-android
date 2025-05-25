package com.example.pile.ui.components.mainscreen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.data.OrgNodeType
import com.example.pile.ui.components.CreateNodeButton
import com.example.pile.ui.components.FindField
import com.example.pile.ui.components.NodeList
import com.example.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Thumbtack

/* Main search view that comes up as the first page */
@RequiresApi(Build.VERSION_CODES.S)
@ExperimentalMaterial3Api
@Composable
fun HomeView(
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    createAndOpenNode: (nodeTitle: String, nodeType: OrgNodeType, refLink: String?, tags: List<String>?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showPinnedDialog by remember { mutableStateOf(false) }

    val recentNodes by viewModel.recentNodes.collectAsState()
    val randomNodes by viewModel.randomNodes.collectAsState()
    val randomLiteratureNodes by viewModel.randomLiteratureNodes.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    viewModel.generateNewRandomNodes()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp),
    ) {
        if (recentNodes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (text == "") {
                    Text(
                        text = "Pile",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 36.dp)
                    )
                    Text(
                        text = "Welcome to your second brain",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 36.dp, vertical = 10.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    NodeCarousel(
                        title = "Random Notes",
                        nodes = randomNodes,
                        compact = true,
                        onClick = openNodeById,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    NodeCarousel(
                        title = "Random Annotatables",
                        nodes = randomLiteratureNodes,
                        compact = true,
                        onClick = openNodeById,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    NodeCarousel(
                        title = "Recently Modified",
                        nodes = recentNodes,
                        compact = false,
                        onClick = openNodeById
                    )
                    JournalStrip(modifier = Modifier.padding(horizontal = 20.dp))
                } else {
                    NodeList(
                        nodes = searchResults,
                        heading = null,
                        onClick = openNodeById
                    )
                }
            }

            if (showPinnedDialog) {
                PinnedDialog(
                    emptyList(),
                    { showPinnedDialog = false },
                    openNodeById
                )
            }

            if (text != "") {
                CreateNodeButton(text) { nodeTitle, nodeType ->
                    createAndOpenNode(
                        nodeTitle,
                        nodeType,
                        null,
                        null
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showPinnedDialog = true },
                    modifier = Modifier
                        .padding(end = 10.dp, top = 20.dp),
                ) {
                    Icon(
                        FontAwesomeIcons.Solid.Thumbtack,
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Pinned Nodes"
                    )
                }
                FindField(
                    text = text,
                    onTextEntry = {
                        text = it
                        viewModel.setSearchQuery(it)
                    },
                    label = "Find or Create",
                    placeholder = "Node Name"
                )
            }
        }
    }
}

