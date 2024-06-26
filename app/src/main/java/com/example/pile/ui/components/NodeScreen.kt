package com.example.pile.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.parseNodeLinks
import com.example.pile.readFile
import com.example.pile.ui.theme.PileTheme
import com.example.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Link
import compose.icons.fontawesomeicons.solid.ProjectDiagram
import compose.icons.fontawesomeicons.solid.Thumbtack
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
 Insert given text in the text field at the cursor position
 */
fun insertText(textFieldValue: TextFieldValue, insertion: String): TextFieldValue {
    val preText = textFieldValue.text.substring(0, textFieldValue.selection.min)
    val postText = textFieldValue.text.substring(textFieldValue.selection.max, textFieldValue.text.length)

    return TextFieldValue(
        text = preText + insertion + postText,
        selection = TextRange((preText + insertion).length)
    )
}

fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "type/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, "Share Content"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(
    node: OrgNode,
    nodes: List<OrgNode>,
    viewModel: SharedViewModel,
    goBack: () -> Unit,
    openNodeById: (String) -> Unit,
    createNewNode: (String, OrgNodeType, (OrgNode) -> Unit) -> Unit,
    onNodeUpdated: (OrgNode) -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val context = LocalContext.current

    val fileContent = node.file?.let { readFile(context, it) } ?: "NA"

    var currentTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = fileContent,
                selection = TextRange(fileContent.length)
            )
        )
    }

    var showLinkDialog by remember { mutableStateOf(false) }

    PileTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(),
                    title = {
                        Text(node.title, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                        rememberTopAppBarState()
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.togglePinned(node) { onNodeUpdated(it) } }) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Thumbtack,
                                modifier = Modifier.size(18.dp),
                                contentDescription = "Pin",
                                tint = if (node.pinned) Color(0xFFFFA726) else Color.Gray
                            )
                        }
                        Switch(
                            checked = isEditMode,
                            onCheckedChange = { isEditMode = it },
                            thumbContent = {
                                if (isEditMode) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.Glasses,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    shareText(context, currentTextFieldValue.text)
                                    menuExpanded = false
                                },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Share, contentDescription = "Share") })
                        }
                    }
                )
            },
            bottomBar = {
                if (isEditMode) {
                    BottomAppBar(
                        actions = {
                            IconButton(enabled = true, onClick = {
                                showLinkDialog = true
                            }) {
                                Icon(
                                    FontAwesomeIcons.Solid.Link,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                    contentDescription = "Link another node"
                                )
                            }
                            IconButton(enabled = true, onClick = {
                                val currentTime = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                currentTextFieldValue = insertText(currentTextFieldValue, "[${currentTime.format(formatter)}]")
                            }) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    contentDescription = "Add current datetime"
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                node.file?.let { viewModel.writeFile(it, currentTextFieldValue.text) }
                            }) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Save Node"
                                )
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!isEditMode) {
                    FloatingActionButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            FontAwesomeIcons.Solid.ProjectDiagram,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            contentDescription = "Show linked nodes"
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    SelectionContainer {
                        Column {
                            if (isEditMode) {
                                NodeEditField(currentTextFieldValue) { currentTextFieldValue = it }
                                if (showLinkDialog) {
                                    FindNodeDialog(
                                        nodes,
                                        onClick = {
                                            currentTextFieldValue = insertText(currentTextFieldValue, "[[id:${it.id}][${it.title}]]")
                                            showLinkDialog = false
                                        },
                                        onDismiss = {
                                            showLinkDialog = false
                                        },
                                        onCreateClick = { title, nodeType ->
                                            createNewNode(title, nodeType) { newNode ->
                                                currentTextFieldValue = insertText(currentTextFieldValue, "[[id:${newNode.id}][${newNode.title}]]")
                                                showLinkDialog = false
                                            }
                                        }
                                    )
                                }
                            } else {
                                OrgPreview(currentTextFieldValue.text, openNodeById)
                                if (showBottomSheet) {
                                    ModalBottomSheet(
                                        onDismissRequest = { showBottomSheet = false },
                                        sheetState = sheetState
                                    ) {
                                        // TODO: Get backlinks too
                                        val linkedNodeIds = parseNodeLinks(currentTextFieldValue.text)

                                        NodeList(
                                            linkedNodeIds.mapNotNull {
                                                nodes.find { orgNode -> orgNode.id == it }
                                            },
                                            "Linked Nodes"
                                        ) { openNodeById(it.id) }
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