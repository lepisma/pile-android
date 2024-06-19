package com.example.pile.viewmodel

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pile.NodeDao
import com.example.pile.OrgNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedViewModel(private val nodeDao: NodeDao, val writeFile: (DocumentFile, String) -> Unit) : ViewModel() {

    fun togglePinned(node: OrgNode, onCompletion: (OrgNode) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeDao.togglePinned(node.id, !node.pinned)

            withContext(Dispatchers.Main) {
                onCompletion(node.copy(pinned = !node.pinned))
            }
        }
    }

}