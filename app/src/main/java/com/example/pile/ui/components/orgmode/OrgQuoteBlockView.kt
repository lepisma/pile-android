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
import com.example.pile.viewmodel.SharedViewModel
import xyz.lepisma.orgmode.OrgBlock

@Composable
fun OrgQuoteBlockView(
    quoteBlock: OrgBlock.OrgQuoteBlock,
    modifier: Modifier = Modifier,
    openNodeById: (String) -> Unit,
    viewModel: SharedViewModel
) {
    val lineColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .drawBehind {
                drawRect(
                    color = lineColor,
                    topLeft = Offset(x = 0f, y = 0f),
                    size = Size(width = 4.dp.toPx(), height = size.height)
                )
            }
    ) {
        for (chunk in quoteBlock.body) {
            OrgChunkView(
                chunk,
                modifier = Modifier.padding(start = 20.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                openNodeById = openNodeById,
                viewModel = viewModel
            )
        }
    }
}