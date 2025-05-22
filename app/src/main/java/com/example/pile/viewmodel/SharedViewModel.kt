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
import com.example.pile.writeFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class SharedViewModel(
    private val nodeDao: NodeDao,
    private var rootUri: Uri?,
    private val applicationContext: Context
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val recentNodes: StateFlow<List<OrgNode>> = nodeDao.getRecentNodes(5)
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
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun setRootUri(uri: Uri) {
        rootUri = uri
    }

    suspend fun getNode(id: String): OrgNode? {
        return withContext(Dispatchers.IO) {
            val nodeFromDb = nodeDao.getNodeById(id)
            nodeFromDb?.copy(file = DocumentFile.fromTreeUri(applicationContext, nodeFromDb.fileString.toUri()))
        }
    }

    fun createNode(title: String, nodeType: OrgNodeType, onCompletion: (OrgNode) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            rootUri?.let { uri ->
                createNewNode(applicationContext, title, uri, nodeType)?.let { node ->
                    nodeDao.insert(node)
                    withContext(Dispatchers.Main) {
                        onCompletion(node)
                    }
                }
            }
        }
    }

    fun refreshDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            rootUri?.let { uri ->
//                val newNodes = readFilesFromDirectory(applicationContext, uri).associateBy { it.id }
//                val existingNodes = nodes.value.associateBy { it.id }
//
//                for (newNode in newNodes.values) {
//                    existingNodes[newNode.id]?.let { existingNode ->
//                        val updatedNode = newNode.copy(pinned = existingNode.pinned)
//                        nodeDao.updateNode(updatedNode)
//                    } ?: run {
//                        nodeDao.insert(newNode)
//                    }
//                }
//
//                val nodesToDelete = existingNodes.filterKeys { it !in newNodes.keys }
//                nodesToDelete.values.forEach { nodeDao.deleteNode(it) }
            }
        }
    }

    fun write(file: DocumentFile, text: String) {
        writeFile(applicationContext, file, text)
        Toast.makeText(applicationContext, "File Saved", Toast.LENGTH_SHORT).show()
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