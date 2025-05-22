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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Reactive list of all nodes
    val nodes: StateFlow<List<OrgNode>> = nodeDao.getAllNodes()
        .map { nodesFromDb ->
            withContext(Dispatchers.Default) {
                nodesFromDb.map { node ->
                    // We need to recover file object from Uri to make well-formed OrgNodes
                    node.copy(file = DocumentFile.fromTreeUri(applicationContext, node.fileString.toUri()))
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setRootUri(uri: Uri) {
        rootUri = uri
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
                val newNodes = readFilesFromDirectory(applicationContext, uri).associateBy { it.id }
                val existingNodes = nodes.value.associateBy { it.id }

                for (newNode in newNodes.values) {
                    existingNodes[newNode.id]?.let { existingNode ->
                        val updatedNode = newNode.copy(pinned = existingNode.pinned)
                        nodeDao.updateNode(updatedNode)
                    } ?: run {
                        nodeDao.insert(newNode)
                    }
                }

                val nodesToDelete = existingNodes.filterKeys { it !in newNodes.keys }
                nodesToDelete.values.forEach { nodeDao.deleteNode(it) }
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