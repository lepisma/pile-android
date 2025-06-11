package com.example.pile.ui.components.orgmode

import androidx.compose.runtime.Composable
import com.example.pile.orgmode.OrgBlock
import com.example.pile.orgmode.OrgChunk
import com.example.pile.orgmode.OrgList
import com.example.pile.orgmode.OrgSection

@Composable
fun OrgChunkView(chunk: OrgChunk) {
    when (chunk) {
        is OrgChunk.OrgParagraph -> OrgParagraphView(chunk)
        is OrgList.OrgUnorderedList -> OrgListView(chunk)
        is OrgSection -> OrgSectionView(chunk)
        is OrgBlock.OrgPageIntroBlock -> OrgPageIntroView(chunk)
        else -> { }
    }
}