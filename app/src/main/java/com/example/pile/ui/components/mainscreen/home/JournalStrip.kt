package com.example.pile.ui.components.mainscreen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CalendarDay
import java.time.DayOfWeek

/**
 * Calendar strip that allows working with org-roam daily entries
 */
@Composable
fun JournalStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            enabled = false,
            onClick = {  },
            modifier = Modifier
                .padding(end = 10.dp, top = 20.dp),
        ) {
            Icon(
                FontAwesomeIcons.Solid.CalendarDay,
                modifier = Modifier.size(18.dp),
                contentDescription = "Journal"
            )
        }
        LazyRow {
            items(DayOfWeek.entries) { day ->
                FilledTonalButton(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = { }
                ) {
                    Column {
                        Text(
                            text = day.name.take(3),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Box {
                            Text(
                                "12",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}