package xyz.lepisma.pile.ui.components.orgmode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lepisma.pile.viewmodel.SharedViewModel
import xyz.lepisma.orgmode.OrgBlock

@Composable
fun OrgEditsBlockView(
    editsBlock: OrgBlock.OrgEditsBlock,
    modifier: Modifier = Modifier,
    openNodeById: (String) -> Unit,
    viewModel: SharedViewModel
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EDITS",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(end = 10.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            )
        }
        for (chunk in editsBlock.body) {
            OrgChunkView(chunk, openNodeById = openNodeById, viewModel = viewModel)
        }
    }
}