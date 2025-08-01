package xyz.lepisma.pile.ui.components.nodescreen

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import xyz.lepisma.pile.data.OrgNode
import xyz.lepisma.pile.data.readFile
import xyz.lepisma.pile.orgmode.parseTags
import xyz.lepisma.pile.ui.components.NodeEditField
import xyz.lepisma.pile.ui.components.NodeList
import xyz.lepisma.pile.ui.components.StructuredNavigationButton
import xyz.lepisma.pile.ui.components.StructuredNavigationDirection
import xyz.lepisma.pile.ui.components.orgmode.OrgPreview
import xyz.lepisma.pile.ui.theme.PileTheme
import xyz.lepisma.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Camera
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Link
import compose.icons.fontawesomeicons.solid.ProjectDiagram
import compose.icons.fontawesomeicons.solid.Thumbtack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

/**
 * Find position of the directed structure (headline in our case) based on the direction and level
 * provided. Return the integer position to jump to. If nothing valid found, return null.
 *
 * This is not very correct since it doesn't restrict jumping outside of a higher heading when you
 * are working at a lower level. But it's good enough to start.
 */
fun findStructure(textFieldValue: TextFieldValue, dir: StructuredNavigationDirection, level: Int?): Int? {
    val currentPosition = textFieldValue.selection.end

    val pattern = if (level == null) Regex("""\n^(\*+) """, RegexOption.MULTILINE) else Regex("""^(\*{$level}) """, RegexOption.MULTILINE)

    // To offset the \n match in case of anyHeadingMode
    val offset = if (level == null) 1 else 0

    return when (dir) {
        StructuredNavigationDirection.UP -> {
            val searchText = textFieldValue.text.substring(0, currentPosition)
            pattern.findAll(searchText).lastOrNull()?.range?.start?.plus(offset)
        }
        StructuredNavigationDirection.DOWN -> {
            if (currentPosition == textFieldValue.text.length) {
                null
            } else {
                val searchText = textFieldValue.text.substring(currentPosition + 1)
                pattern.find(searchText)?.range?.start?.plus(currentPosition + 1 + offset)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NodeScreen(
    nodeId: String,
    viewModel: SharedViewModel,
    goBack: () -> Unit,
    openNodeById: (String) -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentNode by remember { mutableStateOf<OrgNode?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    LaunchedEffect(nodeId) {
        currentNode = viewModel.getNode(nodeId)
        viewModel.setCurrentNodeId(currentNode?.id)
    }

    currentNode?.let { node ->
        val fileContent = node.file?.let { readFile(context, it) } ?: "NA"

        var currentTextFieldValue by remember(node.id) {
            mutableStateOf(
                TextFieldValue(
                    text = fileContent,
                    selection = TextRange(fileContent.length)
                )
            )
        }

        suspend fun generateNewPhotoFile(): DocumentFile? {
            val timestamp = System.currentTimeMillis()
            val fileName = "photo_$timestamp.png"
            return viewModel.getAttachmentDir()?.createFile("image/png", fileName)
        }

        // DocumentFile where an incoming photo from camera will be stored
        var photoFile: DocumentFile? by remember { mutableStateOf(null) }

        val takePictureLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    photoFile?.let { file ->
                        coroutineScope.launch {
                            currentTextFieldValue = insertText(
                                currentTextFieldValue,
                                "[[attachment:${file.name}]]"
                            )

                            photoFile = null
                        }
                    }
                } else {
                    // Delete the file since there was some issue in writing to it
                    photoFile?.delete()
                    photoFile = null
                }
            }
        )

        val camPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    coroutineScope.launch {
                        val file = generateNewPhotoFile()
                        if (file != null) {
                            takePictureLauncher.launch(file.uri)
                            // Save the file since it will be picked up later for processing
                            photoFile = file
                        }
                    }
                } else {
                    // TODO: Handle the case where the user denies the permission
                }
            }
        )

        var lastSavedText by remember(node.id) { mutableStateOf(fileContent) }

        // Autosave text
        DisposableEffect(node.id) {
            val autoSaveInterval = 1000L
            val saveScope = CoroutineScope(Dispatchers.Default)
            val job = saveScope.launch {
                snapshotFlow { currentTextFieldValue.text }
                    .debounce(autoSaveInterval)
                    .distinctUntilChanged()
                    .filter { it.isNotBlank() }
                    .onEach { debouncedText ->
                        if (debouncedText != lastSavedText) {
                            val newNode = node.copy(tags = parseTags(debouncedText))
                            withContext(Dispatchers.Main) {
                                viewModel.updateNode(newNode, newText = debouncedText)
                                lastSavedText = debouncedText
                            }
                        }
                    }
                    .launchIn(this)
            }

            onDispose {
                job.cancel()
                val finalText = currentTextFieldValue.text
                if (finalText != lastSavedText) {
                    val newNode = node.copy(tags = parseTags(finalText))
                    viewModel.updateNode(newNode, newText = finalText)
                    lastSavedText = finalText
                }
                saveScope.cancel()
            }
        }

        var showLinkDialog by remember { mutableStateOf(false) }
        PileTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = topAppBarColors(),
                        title = {
                            Text(
                                node.title,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { goBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                        rememberTopAppBarState()
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.togglePinned(node) }, enabled = false) {
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
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    shareText(context, currentTextFieldValue.text)
                                    menuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share"
                                    )
                                })
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
                                currentTextFieldValue = insertText(
                                    currentTextFieldValue,
                                    "[${currentTime.format(formatter)}]"
                                )
                            }) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    contentDescription = "Add current datetime"
                                )
                            }
                            IconButton(enabled = true, onClick = {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context, android.Manifest.permission.CAMERA
                                    ) -> {
                                        coroutineScope.launch {
                                            val file = generateNewPhotoFile()
                                            if (file != null) {
                                                takePictureLauncher.launch(file.uri)
                                                // Save the file since it will be picked up later
                                                // for processing
                                                photoFile = file
                                            }
                                        }
                                    }
                                    else -> {
                                        camPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    }
                                }
                            }) {
                                Icon(
                                    FontAwesomeIcons.Solid.Camera,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                    contentDescription = "Insert camera shot"
                                )
                            }
                            StructuredNavigationButton { dir, level ->
                                val jumpPosition = findStructure(currentTextFieldValue, dir, level)

                                if (jumpPosition != null) {
                                    currentTextFieldValue = currentTextFieldValue.copy(
                                        selection = TextRange(jumpPosition)
                                    )
                                } else {
                                    Toast.makeText(context, "Nowhere to jump", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        },
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
                                        viewModel,
                                        onClick = { node ->
                                            currentTextFieldValue = insertText(
                                                currentTextFieldValue,
                                                "[[id:${node.id}][${node.title}]]"
                                            )
                                            showLinkDialog = false
                                        },
                                        onDismiss = {
                                            showLinkDialog = false
                                        },
                                        onCreateClick = { title, nodeType ->
                                            viewModel.createNode(title, nodeType) { newNode ->
                                                currentTextFieldValue = insertText(
                                                    currentTextFieldValue,
                                                    "[[id:${newNode.id}][${newNode.title}]]"
                                                )
                                                showLinkDialog = false
                                            }
                                        }
                                    )
                                }
                            } else {
                                OrgPreview(currentTextFieldValue.text, viewModel, openNodeById)
                                if (showBottomSheet) {
                                    ModalBottomSheet(
                                        onDismissRequest = { showBottomSheet = false },
                                        sheetState = sheetState
                                    ) {
                                        // TODO: Get forward and backward links
                                        NodeList(
                                            emptyList(),
                                            "Linked Nodes",
                                            { node -> openNodeById(node.id) }
                                        )
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