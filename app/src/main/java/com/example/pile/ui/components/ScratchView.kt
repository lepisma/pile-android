package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.pile.ui.theme.PileTheme

@Composable
fun ScratchView() {
    var currentTextValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange(0)
            )
        )
    }

    PileTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionContainer {
                Column {
                    BasicTextField(
                        value = currentTextValue,
                        onValueChange = { currentTextValue = it },
                        modifier = Modifier.padding(5.dp),
                        textStyle = TextStyle(
                            color = LocalContentColor.current,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(LocalContentColor.current),
                        decorationBox = { innerTextField ->
                            if (currentTextValue.text.isEmpty()) {
                                Text(
                                    text = "Enter text here",
                                    style = TextStyle(
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}