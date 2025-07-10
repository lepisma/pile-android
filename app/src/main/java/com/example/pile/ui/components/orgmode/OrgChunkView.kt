package com.example.pile.ui.components.orgmode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.lepisma.orgmode.OrgBlock
import xyz.lepisma.orgmode.OrgChunk
import xyz.lepisma.orgmode.OrgList
import xyz.lepisma.orgmode.OrgSection

@Composable
fun OrgChunkView(chunk: OrgChunk, modifier: Modifier = Modifier, openNodeById: (String) -> Unit) {
    when (chunk) {
        is OrgChunk.OrgParagraph -> OrgParagraphView(chunk, modifier, openNodeById = openNodeById)
        is OrgList.OrgUnorderedList -> OrgListView(chunk, modifier, openNodeById)
        is OrgList.OrgOrderedList -> OrgListView(chunk, modifier, openNodeById)
        is OrgSection -> OrgSectionView(chunk, modifier, openNodeById)
        is OrgBlock.OrgPageIntroBlock -> OrgPageIntroView(chunk, modifier, openNodeById)
        is OrgBlock.OrgQuoteBlock -> OrgQuoteBlockView(chunk, modifier, openNodeById)
        is OrgBlock.OrgEditsBlock -> OrgEditsBlockView(chunk, modifier, openNodeById)
        is OrgBlock.OrgSourceBlock -> OrgSourceBlockView(chunk, modifier)
        is OrgBlock.OrgAsideBlock -> OrgAsideBlockView(chunk, modifier, openNodeById)
        is OrgChunk.OrgHorizontalLine -> OrgHorizontalLineView(modifier)
        is OrgBlock.OrgVerseBlock -> OrgVerseBlockView(chunk, modifier)
        else -> { }
    }
}