package com.example.pile.ui.components.orgmode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pile.viewmodel.SharedViewModel
import xyz.lepisma.orgmode.OrgBlock
import xyz.lepisma.orgmode.OrgChunk
import xyz.lepisma.orgmode.OrgList
import xyz.lepisma.orgmode.OrgSection

@Composable
fun OrgChunkView(chunk: OrgChunk, modifier: Modifier = Modifier, openNodeById: (String) -> Unit, viewModel: SharedViewModel) {
    when (chunk) {
        is OrgChunk.OrgParagraph -> OrgParagraphView(chunk, modifier, openNodeById = openNodeById, viewModel = viewModel)
        is OrgList.OrgUnorderedList -> OrgListView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgList.OrgOrderedList -> OrgListView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgSection -> OrgSectionView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgBlock.OrgPageIntroBlock -> OrgPageIntroView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgBlock.OrgQuoteBlock -> OrgQuoteBlockView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgBlock.OrgEditsBlock -> OrgEditsBlockView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgBlock.OrgSourceBlock -> OrgSourceBlockView(chunk, modifier)
        is OrgBlock.OrgAsideBlock -> OrgAsideBlockView(chunk, modifier, openNodeById, viewModel = viewModel)
        is OrgChunk.OrgHorizontalLine -> OrgHorizontalLineView(modifier)
        is OrgBlock.OrgVerseBlock -> OrgVerseBlockView(chunk, modifier)
        else -> { }
    }
}