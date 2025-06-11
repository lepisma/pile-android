package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgBlock
import com.example.pile.orgmode.OrgChunk

@Composable
fun OrgQuoteBlockView(
    quoteBlock: OrgBlock.OrgQuoteBlock,
    modifier: Modifier = Modifier
) {
    val paragraph = quoteBlock.body.first() as OrgChunk.OrgParagraph
    val lineColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .padding(bottom = 20.dp)
            .drawBehind {
                drawRect(
                    color = lineColor,
                    topLeft = Offset(x = 0f, y = 0f),
                    size = Size(width = 4.dp.toPx(), height = size.height)
                )
            }
    ) {
        OrgParagraphView(
            paragraph,
            modifier = Modifier
                .padding(start = 20.dp, end = 10.dp, top = 10.dp, bottom = 10.dp))
    }
}