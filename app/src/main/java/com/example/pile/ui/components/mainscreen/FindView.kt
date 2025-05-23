package com.example.pile.ui.components.mainscreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pile.data.OrgNode
import com.example.pile.data.OrgNodeType
import com.example.pile.data.isDailyNode
import com.example.pile.data.isLiteratureNode
import com.example.pile.data.isUnsortedNode
import com.example.pile.ui.components.CreateNodeButton
import com.example.pile.ui.components.FindField
import com.example.pile.ui.components.NodeList
import com.example.pile.ui.formatRelativeTime
import com.example.pile.ui.theme.rememberSimpleFadedCardBackgrounds
import com.example.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.regular.Calendar
import compose.icons.fontawesomeicons.solid.Bookmark
import compose.icons.fontawesomeicons.solid.CalendarDay
import compose.icons.fontawesomeicons.solid.Thumbtack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

/* Main search view that comes up as the first page */
@ExperimentalMaterial3Api
@Composable
fun FindView(
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    createAndOpenNode: (nodeTitle: String, nodeType: OrgNodeType, refLink: String?, tags: List<String>?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showPinnedDialog by remember { mutableStateOf(false) }

    val recentNodes by viewModel.recentNodes.collectAsState()
    val randomNodes by viewModel.randomNodes.collectAsState()
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
                    RandomNodeList(randomNodes, openNodeById)
                    Spacer(Modifier.padding(10.dp))
                    RecentNodeList(recentNodes, openNodeById)
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
                IconButton(
                    enabled = false,
                    onClick = {  },
                    modifier = Modifier
                        .padding(end = 10.dp, top = 20.dp),
                ) {
                    Icon(
                        FontAwesomeIcons.Solid.CalendarDay,
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Journal"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomNodeList(nodes: List<OrgNode>, onClick: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val cardWidth = (screenWidth * 0.7f)
    val cardHeight = 120.dp
    val itemSpacing = 8.dp
    val contentHorizontalPadding = 16.dp

    if (nodes.isNotEmpty()) {
        Text(
            "Random Notes",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 36.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            contentPadding = PaddingValues(horizontal = contentHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            items(nodes) { node ->
                OrgNodeCard(
                    node = node,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    showModification = false,
                    onClick = onClick
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun OrgNodeCard(node: OrgNode, cardWidth: Dp, cardHeight: Dp, showModification: Boolean, onClick: (String) -> Unit) {
    val allTextures = rememberSimpleFadedCardBackgrounds()

    val textureBrush = remember(node.id, allTextures) {
        val index = node.id.hashCode().absoluteValue % allTextures.size
        allTextures[index]
    }
    val cardShape = RoundedCornerShape(10.dp)

    Card (
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .padding(5.dp),
        shape = cardShape,
        onClick = { onClick(node.id) },
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(textureBrush)
                .clip(cardShape)
                .padding(20.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    if (isLiteratureNode(node)) {
                        Icon(
                            imageVector = if (isUnsortedNode(node)) FontAwesomeIcons.Regular.Bookmark else FontAwesomeIcons.Solid.Bookmark,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 5.dp),
                            contentDescription = "Literature Node",
                            tint = Color.Gray
                        )
                    } else if (isDailyNode(node)) {
                        Icon(
                            imageVector = FontAwesomeIcons.Regular.Calendar,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 5.dp),
                            contentDescription = "Daily Node",
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = node.title,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.padding(5.dp))
                if (showModification) {
                    Text(
                        text = "Modified ${formatRelativeTime(node.lastModified)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun RecentNodeList(nodes: List<OrgNode>, onClick: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val cardWidth = (screenWidth * 0.7f)
    val cardHeight = 150.dp
    val itemSpacing = 8.dp
    val contentHorizontalPadding = 16.dp

    if (nodes.isNotEmpty()) {
        Text(
            "Recently Modified",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 36.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            contentPadding = PaddingValues(horizontal = contentHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            items(nodes) { node ->
                OrgNodeCard(
                    node = node,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    showModification = true,
                    onClick = onClick
                )
            }
        }
    }
}