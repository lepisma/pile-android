package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrgHorizontalLineView() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 15.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline
    )
}