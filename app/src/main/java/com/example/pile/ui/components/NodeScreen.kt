package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.readFile
import com.example.pile.ui.theme.PileTheme
import com.example.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Link

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeEdit(text: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = text,
        onValueChange = { onValueChange(it) },
        modifier = Modifier.padding(5.dp),
        textStyle = TextStyle(
            color = LocalContentColor.current,
            fontFamily = FontFamily.Monospace
        ),
        cursorBrush = SolidColor(LocalContentColor.current)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(node: OrgNode, viewModel: SharedViewModel, goBack: () -> Unit, openNode: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var isEditMode by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val fileContent = node.file?.let { readFile(context, it) } ?: "NA"
    var currentText by remember { mutableStateOf(fileContent) }

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
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Share, contentDescription = "Share") })
                        }
                    }
                )
            },
            bottomBar = {
                if (isEditMode) {
                    BottomAppBar(
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(FontAwesomeIcons.Solid.Link, modifier = Modifier.size(SwitchDefaults.IconSize), contentDescription = "Link another node")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Add current datetime")
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                node.file?.let {
                                    viewModel.fileToEdit.value = Pair(it, currentText)
                                }
                            }) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Save Node")
                            }
                        }
                    )
                }
            },

        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    SelectionContainer {
                        Column {
                            if (isEditMode) {
                                NodeEdit(text = currentText) { currentText = it }
                            } else {
                                OrgPreview(currentText, openNode)
                            }
                        }
                    }
                }
            }
        }
    }
}