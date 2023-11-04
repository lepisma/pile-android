package com.example.pile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgParagraph

@Composable
fun OrgQuoteText(orgQuote: OrgParagraph.OrgQuote, openNodeById: (String) -> Unit) {
    OrgText(
        text = orgQuote.text,
        openNodeById = openNodeById,
        modifier = Modifier
            .padding(start = 10.dp, top = 20.dp, bottom = 20.dp)
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(-10.dp.toPx(), 0F),
                    end = Offset(-10.dp.toPx(), size.height - 10.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
    )
}