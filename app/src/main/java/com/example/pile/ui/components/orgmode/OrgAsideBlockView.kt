package com.example.pile.ui.components.orgmode


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lepisma.orgmode.OrgBlock

@Composable
fun OrgAsideBlockView(
    asideBlock: OrgBlock.OrgAsideBlock,
    modifier: Modifier = Modifier,
    openNodeById: (String) -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CardDefaults.elevatedShape)
    ) {
        for (chunk in asideBlock.body) {
            OrgChunkView(chunk, modifier = Modifier.padding(15.dp), openNodeById)
        }
    }
}