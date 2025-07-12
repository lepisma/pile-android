package xyz.lepisma.pile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Text field with search icon, supposed to be used for finding stuff.
 */
@ExperimentalMaterial3Api
@Composable
fun FindField(
    text: String,
    onTextEntry: (String) -> Unit,
    label: String? = "Find",
    placeholder: String? = null,
    showButton: Boolean = false,
    onButtonClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TextField(
        value = text,
        onValueChange = onTextEntry,
        maxLines = 1,
        label = if (label != null) {
            { Text(text = label) }
        } else null,
        placeholder = if (placeholder != null) {
            { Text(text = placeholder) }
        } else null,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(60.dp),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        trailingIcon = if (showButton) { {
            IconButton(onClick = { if (onButtonClick != null) onButtonClick(text) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        } } else null
    )
}