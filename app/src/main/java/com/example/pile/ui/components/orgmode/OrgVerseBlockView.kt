package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgBlock

@Composable
fun OrgVerseBlockView(
    verseBlock: OrgBlock.OrgVerseBlock,
    modifier: Modifier = Modifier
) {
    Text(
        text = verseBlock.body.trim(),
        modifier = modifier
            .padding(horizontal = 10.dp),
        fontStyle = FontStyle.Italic
    )
}