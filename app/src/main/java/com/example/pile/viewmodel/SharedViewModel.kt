package com.example.pile.viewmodel

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.example.pile.NodeDao

class SharedViewModel(private val nodeDao: NodeDao, val writeFile: (DocumentFile, String) -> Unit) : ViewModel() {
}