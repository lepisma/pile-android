package com.example.pile.ui.components.mainscreen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.data.OrgNodeType
import com.example.pile.viewmodel.SharedViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendar strip that allows working with org-roam daily entries
 */
@Composable
fun JournalStrip(
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyNodes by viewModel.dailyNodes.collectAsState()

    val today = LocalDate.now()
    val lastWeek = (0L until 7).map { i ->
        val date = today.minusDays(6 - i)
        date
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 1.dp)
        ) {
            items(lastWeek) { date ->
                val isToday = date == today
                val node = dailyNodes.find { it.datetime.toLocalDate() == date }

                ElevatedCard(
                    enabled = isToday || node != null,
                    modifier = Modifier
                        .width(50.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (isToday) 4.dp else 0.dp
                    ),
                    colors = if (node != null) {
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        CardDefaults.elevatedCardColors()
                    },
                    onClick = {
                        if (node != null) {
                            openNodeById(node.id)
                        } else if (isToday) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            viewModel.createNode(
                                title = date.format(formatter),
                                nodeType = OrgNodeType.DAILY,
                                ref = null,
                                tags = null
                            ) { node ->
                                openNodeById(node.id)
                            }
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isToday) { FontWeight.Bold } else { FontWeight.Normal }
                        )
                    }
                }
            }
        }
    }
}