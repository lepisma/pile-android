package com.example.pile.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pile.data.LinkDao
import com.example.pile.data.NodeDao
import com.example.pile.data.NodeTag
import com.example.pile.data.NodeTagsDao
import com.example.pile.data.OrgNode
import com.example.pile.data.OrgNodeType
import com.example.pile.data.Tag
import com.example.pile.data.TagDao
import com.example.pile.data.createNewNode
import com.example.pile.data.nodeFilesFromDirectory
import com.example.pile.data.parseFileOrgNode
import com.example.pile.data.writeFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 View Model unifying data operations across 3 places:
 1. SQLite database
 2. Root directory where files are synced
 3. Cache storage for keeping things like root directory path
 */
class SharedViewModel(
    private val nodeDao: NodeDao,
    private val tagDao: TagDao,
    private val nodeTagsDao: NodeTagsDao,
    private val linkDao: LinkDao,
    private val applicationContext: Context
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _rootUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val rootUri: StateFlow<Uri?> = _rootUri.asStateFlow()

    fun setRootUri(uri: Uri) {
        _rootUri.value = uri
    }

    // Helper function to manage loading state
    private fun CoroutineScope.launchWithLoading(block: suspend () -> Unit) {
        launch {
            _isLoading.value = true
            try {
                block()
            } catch (e: Exception) {
                println("Error during operation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Convert node from nodes table to full node after recovering `file` and `tags`
     */
    private suspend fun recoverNode(nodeFromDb: OrgNode): OrgNode {
        val file = DocumentFile.fromSingleUri(applicationContext, nodeFromDb.fileString.toUri())
        val tags = nodeTagsDao.getTagsForNode(nodeFromDb.id).firstOrNull() ?: emptyList()
        return nodeFromDb.copy(file = file, tags = tags.map { it.name })
    }

    val nodeCount: StateFlow<Int> = nodeDao.countNodes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    val dailyNodes: StateFlow<List<OrgNode>> = nodeDao.getDailyNodes()
        .map { nodesFromDb ->
            withContext(Dispatchers.Default) {
                nodesFromDb.map { recoverNode(it) }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val recentNodes: StateFlow<List<OrgNode>> = nodeDao.getRecentNodes(5)
        .map { nodesFromDb ->
            withContext(Dispatchers.Default) {
                nodesFromDb.map { recoverNode(it) }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val pinnedNodes: StateFlow<List<OrgNode>> = nodeDao.getPinnedNodes()
        .map { nodesFromDb ->
            withContext(Dispatchers.Default) {
                nodesFromDb.map { recoverNode(it) }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _randomNodesTrigger = MutableStateFlow(Unit)

    @OptIn(ExperimentalCoroutinesApi::class)
    val randomNodes: StateFlow<List<OrgNode>> = _randomNodesTrigger
        .flatMapLatest { _ ->
            nodeDao.getAllNodes()
                .map { nodesFromDb ->
                    withContext(Dispatchers.Default) {
                        nodesFromDb
                            .filter { node -> node.nodeType == OrgNodeType.CONCEPT }
                            .shuffled()
                            .take(5)
                            .map { recoverNode(it) }
                    }
                }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val randomLiteratureNodes: StateFlow<List<OrgNode>> = _randomNodesTrigger
        .flatMapLatest { _ ->
            nodeDao.getAllNodes()
                .map { nodesFromDb ->
                    withContext(Dispatchers.Default) {
                        nodesFromDb
                            .filter { node -> node.nodeType == OrgNodeType.LITERATURE }
                            .shuffled()
                            .take(5)
                            .map { recoverNode(it) }
                    }
                }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun generateNewRandomNodes() {
        _randomNodesTrigger.value = Unit
    }

    private val _searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<OrgNode>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                nodeDao.searchNodesByTitle(query)
                    .map { nodesFromDb ->
                        withContext(Dispatchers.Default) {
                            nodesFromDb.map { recoverNode(it) }
                        }
                    }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun getNode(nodeId: String): OrgNode? {
        return withContext(Dispatchers.IO) {
            val nodeFromDb = nodeDao.getNodeById(nodeId)
            if (nodeFromDb != null) {
                recoverNode(nodeFromDb)
            } else {
                null
            }
        }
    }

    /**
     * Create new node in the filesystem AND the database with given metadata. On completion, run
     * the onCompletion block.
     */
    fun createNode(
        title: String,
        nodeType: OrgNodeType,
        ref: String? = null,
        tags: List<String>? = null,
        onCompletion: (OrgNode) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _rootUri.value?.let { uri ->
                createNewNode(
                    applicationContext,
                    title,
                    uri,
                    nodeType,
                    nodeRef = ref,
                    nodeTags = tags
                )?.let { node ->
                    nodeDao.insert(node)
                    withContext(Dispatchers.Main) {
                        onCompletion(node)
                    }
                }
            }
        }
    }

    fun resetDatabase() {
        viewModelScope.launchWithLoading {
            withContext(Dispatchers.IO) {
                nodeDao.deleteAll()
            }
        }
    }

    fun syncDatabase() {
        if (_rootUri.value == null) {
            notify("Root directory not set, aborting sync")
        } else {
            viewModelScope.launchWithLoading {
                withContext(Dispatchers.IO) {
                    val dbFileInfoMap = nodeDao.getFileInfo().associateBy { it.fileString }
                    val dirFiles = nodeFilesFromDirectory(applicationContext, _rootUri.value!!)
                    val nodesToDelete: MutableSet<String> = dbFileInfoMap.keys.toMutableSet()

                    var updateCount = 0
                    var insertCount = 0

                    for (file in dirFiles) {
                        val fileString = file.uri.toString()
                        if (dbFileInfoMap.contains(fileString)) {
                            // File already stored in db as node
                            val fileInfo = dbFileInfoMap[fileString]!!
                            if (fileInfo.lastModified < file.lastModified) {
                                // File in directory seems to be modified more recently than the
                                // snapshot stored in database. Let's update it.
                                val docFile = DocumentFile.fromSingleUri(applicationContext, file.uri)
                                val node = parseFileOrgNode(applicationContext, docFile!!)
                                if (node != null) {
                                    nodeDao.updateNode(node)
                                    updateCount += 1
                                } else {
                                    notify("Error in parsing $docFile")
                                }
                            }

                            nodesToDelete.remove(fileInfo.fileString)
                        } else {
                            // At this point, we need to do a full parse and insert node in the database
                            val docFile = DocumentFile.fromSingleUri(applicationContext, file.uri)
                            val node = parseFileOrgNode(applicationContext, docFile!!)
                            if (node != null) {
                                nodeDao.insert(node)
                                insertCount += 1
                            } else {
                                notify("Error in parsing $docFile")
                            }
                        }
                    }

                    if (insertCount + updateCount > 0) {
                        withContext(Dispatchers.Main) {
                            notify("Inserted $insertCount new notes, updated $updateCount notes")
                        }
                    }

                    // Delete all the nodes from db that are not reflected in the filesystem
                    for (fileString in nodesToDelete) {
                        nodeDao.deleteNodeById(dbFileInfoMap.get(fileString)!!.id)
                    }
                    if (nodesToDelete.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            notify("Deleted ${nodesToDelete.count()} notes")
                        }
                    }
                }
            }
        }
    }

    private fun notify(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun write(file: DocumentFile, text: String) {
        writeFile(applicationContext, file, text)
    }

    fun togglePinned(node: OrgNode) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeDao.togglePinned(node.id, !node.pinned)
        }
    }

    private fun updateNodeTags(nodeId: String, tags: List<String>) {
        nodeTagsDao.deleteTagsForNode(nodeId)

        val nodeTags = mutableListOf<NodeTag>()

        for (tagName in tags.distinct().filter { it.isNotBlank() }) {
            var tag = tagDao.getTagByName(tagName)

            if (tag == null) {
                val tagId = tagDao.insert(Tag(name = tagName))
                tag = Tag(id = tagId, name = tagName)
            }

            nodeTags.add(NodeTag(nodeId = nodeId, tagId = tag.id))
        }

        if (nodeTags.isNotEmpty()) {
            nodeTagsDao.insertAll(*nodeTags.toTypedArray())
        }
    }

    /**
     * Update a new node in the database AND in the file system. The assumption is to have the same
     * id as the node to be updated.
     */
    fun updateNode(newNode: OrgNode, newText: String? = null) {
        newNode.file?.let { documentFile ->
            if (newText != null) {
                write(documentFile, newText)
                viewModelScope.launch(Dispatchers.IO) {
                    nodeDao.updateNode(newNode.copy(lastModified = documentFile.lastModified()))
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    nodeDao.updateNode(newNode)
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                updateNodeTags(newNode.id, tags = newNode.tags)
            }
        }
    }
}