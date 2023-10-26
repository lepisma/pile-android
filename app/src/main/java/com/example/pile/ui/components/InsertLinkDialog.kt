package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.pile.OrgNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertLinkDialog(nodes: List<OrgNode>, onClick: (OrgNode) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card {
            var text by remember { mutableStateOf("") }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                FindNodeList(nodes, text) { onClick(it) }
                FindField(text = text, onTextEntry = { text = it }, label = "Insert", placeholder = "Node Name")
            }
        }
    }
}