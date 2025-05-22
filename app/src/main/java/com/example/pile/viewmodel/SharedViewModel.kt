package com.example.pile.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pile.NodeDao
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.createNewNode
import com.example.pile.readFilesFromDirectory
import com.example.pile.writeFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
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
    fun CoroutineScope.launchWithLoading(block: suspend () -> Unit) {
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

    val nodeCount: StateFlow<Int> = nodeDao.countNodes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    val dailyNodes: StateFlow<List<OrgNode>> = nodeDao.getDailyNodes()
        .map { nodesFromDb ->
            withContext(Dispatchers.Default) {
                nodesFromDb.map { node ->
                    node.copy(file = DocumentFile.fromTreeUri(applicationContext, node.fileString.toUri()))
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val recentNodes: StateFlow<List<OrgNode>> = nodeDao.getRecentNodes(7)
        .map { nodesFromDb ->
            withContext(Dispatchers.Default) {
                nodesFromDb.map { node ->
                    node.copy(file = DocumentFile.fromTreeUri(applicationContext, node.fileString.toUri()))
                }
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
                        nodesFromDb.shuffled()
                            .take(3)
                            .map { node ->
                                node.copy(file = DocumentFile.fromTreeUri(applicationContext, node.fileString.toUri()))
                            }
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
                            nodesFromDb.map { node ->
                                node.copy(file = DocumentFile.fromTreeUri(applicationContext, node.fileString.toUri()))
                            }
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

    suspend fun getNode(id: String): OrgNode? {
        return withContext(Dispatchers.IO) {
            val nodeFromDb = nodeDao.getNodeById(id)
            nodeFromDb?.copy(file = DocumentFile.fromTreeUri(applicationContext, nodeFromDb.fileString.toUri()))
        }
    }

    fun createNode(title: String, nodeType: OrgNodeType, onCompletion: (OrgNode) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _rootUri.value?.let { uri ->
                createNewNode(applicationContext, title, uri, nodeType)?.let { node ->
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
                    // TODO: Currently performing full sync, fix this to make it faster
                    nodeDao.deleteAll()
                    val nodes = readFilesFromDirectory(applicationContext, _rootUri.value!!)
                    nodeDao.insertAll(*nodes.toTypedArray())
                }
            }
        }
    }

    private fun notify(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun write(file: DocumentFile, text: String) {
        writeFile(applicationContext, file, text)
        notify("File Saved")
    }

    fun togglePinned(node: OrgNode) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeDao.togglePinned(node.id, !node.pinned)
        }
    }

    fun updateTags(node: OrgNode, tags: List<String>) {
        val updatedNode = node.copy(tags = tags)

        viewModelScope.launch(Dispatchers.IO) {
            nodeDao.updateNode(updatedNode)
        }
    }
}