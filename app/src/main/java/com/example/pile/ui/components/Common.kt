package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.parseOrgBody
import com.example.pile.parseTitle

@Composable
fun OrgPreview(text: String) {
    OrgTitle(title = parseTitle(text))
    OrgBody(text = text)
}

@Composable
fun OrgTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(bottom = 20.dp)
    )
}

@Composable
fun OrgBody(text: String) {
    Text(text = parseOrgBody(text))
}