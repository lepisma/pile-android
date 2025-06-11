package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgBlock
import com.example.pile.orgmode.OrgChunk

@Composable
fun OrgPageIntroView(
    pageIntroBlock: OrgBlock.OrgPageIntroBlock,
    modifier: Modifier = Modifier
) {
    val paragraph = pageIntroBlock.body.first() as OrgChunk.OrgParagraph
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
            .padding(bottom = 20.dp)
    ) {
        OrgParagraphView(paragraph, modifier = Modifier.padding(15.dp))
    }
}