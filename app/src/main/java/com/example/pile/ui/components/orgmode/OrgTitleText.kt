package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgInlineElem
import com.example.pile.orgmode.OrgLine

@Composable
fun OrgTitleText(title: OrgLine) {
    var rawTitle = title.items
        .filter { it is OrgInlineElem.Text }
        .joinToString("") { (it as OrgInlineElem.Text).text }
    Text(
        text = rawTitle,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier.padding(bottom = 20.dp)
    )
}