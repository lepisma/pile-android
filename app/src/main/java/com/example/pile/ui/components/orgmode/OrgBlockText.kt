package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgParagraph

@Composable
fun OrgBlockText(orgBlock: OrgParagraph.OrgBlock) {
    Text(
        orgBlock.text,
        fontFamily = FontFamily.Monospace,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}