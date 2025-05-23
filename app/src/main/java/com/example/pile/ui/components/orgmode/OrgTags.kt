package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OrgTags(tags: List<String>) {
    Row {
        tags.map {
            SuggestionChip(
                onClick = { /*TODO*/ },
                label = { Text("#$it") },
                modifier = Modifier.padding(end = 5.dp),
                border = SuggestionChipDefaults.suggestionChipBorder(borderColor = Color.DarkGray)
            )
        }
    }
}