package com.example.pile.ui.components.orgmode

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import xyz.lepisma.orgmode.OrgChunk


@Composable
fun OrgParagraphView(
    paragraph: OrgChunk.OrgParagraph,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    openNodeById: (String) -> Unit
) {
    OrgInlineElemsView(paragraph.items, modifier = modifier, style = style, openNodeById = openNodeById)
}