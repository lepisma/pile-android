package xyz.lepisma.pile.ui.components.mainscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SearchView() {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            text = "Deep Search",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "This feature is currently under development, please use title search in main page for now",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
        Spacer(Modifier.weight(1f))
    }
}