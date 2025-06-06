package com.example.pile.ui.components.orgmode

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pile.orgmode.OrgChunk
import com.example.pile.orgmode.OrgInlineElem
import com.example.pile.orgmode.unfillText


@Composable
fun OrgParagraphView(paragraph: OrgChunk.OrgParagraph, modifier: Modifier = Modifier) {
    val rawText = unfillText(paragraph.items
        .filter { it is OrgInlineElem.Text }
        .joinToString("") { (it as OrgInlineElem.Text).text }
    )

    Text(
        text = rawText,
        modifier = modifier
    )
}