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

    fun toggleBookmark(node: OrgNode, onCompletion: (OrgNode) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeDao.toggleBookmark(node.id, !node.bookmarked)

            withContext(Dispatchers.Main) {
                onCompletion(node.copy(bookmarked = !node.bookmarked))
            }
        }
    }

}