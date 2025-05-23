package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OrgHorizontalLine() {
    Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 15.dp))
}