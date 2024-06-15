package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNodeType
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Bookmark

/**
 * Button for creating new node with given name. Usually used along with FindField.
 *
 * @param nodeName Name for the node to show on the button.
 * @param onClick Function taking node name and creating node (or doing something else).
 */
@Composable
fun CreateNodeButton(nodeName: String, onClick: (nodeTitle: String, nodeType: OrgNodeType) -> Unit) {
    FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = { onClick(nodeName, OrgNodeType.CONCEPT) }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Create ", color = Color.Gray)
            Text(nodeName)
            Spacer(modifier = Modifier.weight(1f))
            ElevatedButton(onClick = { onClick(nodeName, OrgNodeType.LITERATURE) }) {
                Icon(
                    imageVector = FontAwesomeIcons.Regular.Bookmark,
                    modifier = Modifier.size(18.dp),
                    contentDescription = "Literature Node"
                )
            }
        }
    }
}