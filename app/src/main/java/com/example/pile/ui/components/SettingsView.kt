package com.example.pile.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsView(refreshDatabase: () -> Unit, isLoading: Boolean) {
    FilledTonalButton (
        onClick = { refreshDatabase() },
        modifier = Modifier.padding(16.dp),
        enabled = !isLoading
    ) {
        Row (verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh Database"
            )
            Spacer(Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}