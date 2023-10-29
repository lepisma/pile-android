package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Edit field for working on the node content.
 *
 * @param textFieldValue Value with selection and text content.
 * @param onValueChange Callback for change in selection or content.
 */
@Composable
fun NodeEditField(textFieldValue: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    BasicTextField(
        value = textFieldValue,
        onValueChange = { onValueChange(it) },
        modifier = Modifier.padding(5.dp),
        textStyle = TextStyle(
            color = LocalContentColor.current,
            fontFamily = FontFamily.Monospace
        ),
        cursorBrush = SolidColor(LocalContentColor.current)
    )
}