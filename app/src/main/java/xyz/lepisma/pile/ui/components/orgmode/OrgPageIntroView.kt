package xyz.lepisma.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lepisma.pile.viewmodel.SharedViewModel
import xyz.lepisma.orgmode.OrgBlock

@Composable
fun OrgPageIntroView(
    pageIntroBlock: OrgBlock.OrgPageIntroBlock,
    modifier: Modifier = Modifier,
    openNodeById: (String) -> Unit,
    viewModel: SharedViewModel
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
    ) {
        for (chunk in pageIntroBlock.body) {
            OrgChunkView(chunk, modifier = Modifier.padding(15.dp), openNodeById = openNodeById, viewModel = viewModel)
        }
    }
}