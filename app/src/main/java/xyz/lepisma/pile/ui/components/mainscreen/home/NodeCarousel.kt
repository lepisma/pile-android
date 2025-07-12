package xyz.lepisma.pile.ui.components.mainscreen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.lepisma.pile.data.OrgNode

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun NodeCarousel(
    title: String,
    nodes: List<OrgNode>,
    compact: Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemSpacing = 8.dp
    val contentHorizontalPadding = 16.dp

    Column(
        modifier = modifier
    ) {
        Text(
            title,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 36.dp)
        )

        if (nodes.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),
                contentPadding = PaddingValues(horizontal = contentHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                items(nodes) { node ->
                    OrgNodeCard(
                        node = node,
                        compact = compact,
                        onClick = onClick,
                    )
                }
            }
        }
    }
}