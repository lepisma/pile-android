package com.example.pile.ui.components.orgmode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.lepisma.orgmode.OrgBlock
import xyz.lepisma.orgmode.OrgChunk
import xyz.lepisma.orgmode.OrgList
import xyz.lepisma.orgmode.OrgSection

@Composable
fun OrgChunkView(chunk: OrgChunk, modifier: Modifier = Modifier) {
    when (chunk) {
        is OrgChunk.OrgParagraph -> OrgParagraphView(chunk, modifier)
        is OrgList.OrgUnorderedList -> OrgListView(chunk, modifier)
        is OrgList.OrgOrderedList -> OrgListView(chunk, modifier)
        is OrgSection -> OrgSectionView(chunk, modifier)
        is OrgBlock.OrgPageIntroBlock -> OrgPageIntroView(chunk, modifier)
        is OrgBlock.OrgQuoteBlock -> OrgQuoteBlockView(chunk, modifier)
        is OrgBlock.OrgEditsBlock -> OrgEditsBlockView(chunk, modifier)
        is OrgBlock.OrgSourceBlock -> OrgSourceBlockView(chunk, modifier)
        is OrgBlock.OrgAsideBlock -> OrgAsideBlockView(chunk, modifier)
        is OrgChunk.OrgHorizontalLine -> OrgHorizontalLineView(modifier)
        is OrgBlock.OrgVerseBlock -> OrgVerseBlockView(chunk, modifier)
        else -> { }
    }
}