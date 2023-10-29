package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Button for creating new node with given name. Usually used along with FindField.
 *
 * @param nodeName Name for the node to show on the button.
 * @param onClick Function taking node name and creating node (or doing something else).
 */
@Composable
fun CreateNodeButton(nodeName: String, onClick: (String) -> Unit) {
    FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = { onClick(nodeName) }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Create ", color = Color.Gray)
            Text(nodeName)
        }
    }
}