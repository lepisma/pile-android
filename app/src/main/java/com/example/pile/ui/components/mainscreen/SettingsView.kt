package com.example.pile.ui.components.mainscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.ui.formatUriForDisplay
import com.example.pile.viewmodel.SharedViewModel


@Composable
fun SettingsView(viewModel: SharedViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val nodeCount by viewModel.nodeCount.collectAsState()
    val rootUri by viewModel.rootUri.collectAsState()

    val context = LocalContext.current

    Column {
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Working Directory",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Row {
                    Text(
                        text = "Syncing from: "
                    )
                    Text(
                        text = formatUriForDisplay(context, rootUri),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Database",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Text(
                    text = "Total ${nodeCount} nodes in database",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 5.dp),
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Reset Database")
                    Spacer(Modifier.weight(1f))
                    OutlinedButton (
                        onClick = { viewModel.resetDatabase() },
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Reset Database"
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Sync Database")
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.syncDatabase() },
                        enabled = !isLoading,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Database"
                            )
                        }
                    }
                }
            }
        }
    }
}