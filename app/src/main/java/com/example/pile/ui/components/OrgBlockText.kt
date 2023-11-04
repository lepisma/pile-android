package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.pile.OrgParagraph

@Composable
fun OrgBlockText(orgBlock: OrgParagraph.OrgBlock) {
    Text(
        orgBlock.text,
        fontFamily = FontFamily.Monospace,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.95,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}