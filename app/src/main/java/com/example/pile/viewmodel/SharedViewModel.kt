package com.example.pile.viewmodel

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val fileToEdit: MutableLiveData<Pair<DocumentFile, String>> = MutableLiveData()
}