package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

@Composable
fun SearchView() {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            text = "Under Development",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Gray,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}