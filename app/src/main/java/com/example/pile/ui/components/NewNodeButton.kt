package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.data.OrgNodeType

/**
 * Button for creating new CONCEPT node with given name. Usually used along with FindField.
 *
 * @param nodeName Name for the node to show on the button.
 * @param onClick Function taking node name and creating node (or doing something else).
 */
@Composable
fun NewNodeButton(nodeName: String, onClick: (nodeTitle: String, nodeType: OrgNodeType) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            onClick = { onClick(nodeName, OrgNodeType.CONCEPT) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("New Note")
                Spacer(modifier = Modifier.padding(4.dp))
                Icon(
                    Icons.Filled.Add,
                    modifier = Modifier.size(15.dp),
                    contentDescription = null
                )
            }
        }
    }
}