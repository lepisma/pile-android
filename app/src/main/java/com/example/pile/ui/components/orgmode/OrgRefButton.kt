package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.parseOrgRef
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bookmark

@Composable
fun OrgRefButton(text: String) {
    val localUriHandler = LocalUriHandler.current

    parseOrgRef(text)?.let {
        OutlinedButton(
            onClick = { localUriHandler.openUri(it) },
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Bookmark,
                    contentDescription = "Reference Icon",
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
                Text("Reference")
            }
        }
    }
}