package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OrgHorizontalLine() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 15.dp),
        thickness = 1.dp,
        color = Color.Gray
    )
}