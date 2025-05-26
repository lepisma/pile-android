package com.example.pile.ui.components.mainscreen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pile.data.OrgNode
import com.example.pile.data.isDailyNode
import com.example.pile.data.isLiteratureNode
import com.example.pile.data.isUnsortedNode
import com.example.pile.ui.formatRelativeTime
import com.example.pile.ui.theme.rememberSimpleFadedCardBackgrounds
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Bookmark
import compose.icons.fontawesomeicons.regular.Calendar
import compose.icons.fontawesomeicons.solid.Bookmark
import java.net.URI
import kotlin.math.absoluteValue


/**
 * Format node reference for display on cards
 */
fun formatRef(ref: String): String? {
    if (ref.startsWith("http")) {
        val uri = URI(ref)
        val domain: String = uri.host
        return domain.removePrefix("www.")
    } else {
        return null
    }
}

/**
 * Card for home screen carousels
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun OrgNodeCard(
    node: OrgNode,
    compact: Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTextures = rememberSimpleFadedCardBackgrounds()

    val textureBrush = remember(node.id, allTextures) {
        val index = node.id.hashCode().absoluteValue % allTextures.size
        allTextures[index]
    }
    val cardShape = RoundedCornerShape(10.dp)

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val cardWidth = (screenWidth * 0.7f)
    val cardHeight = if (compact) 120.dp else 150.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .padding(5.dp),
        shape = cardShape,
        onClick = { onClick(node.id) },
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(textureBrush)
                .clip(cardShape)
                .padding(20.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    if (isLiteratureNode(node)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUnsortedNode(node)) FontAwesomeIcons.Regular.Bookmark else FontAwesomeIcons.Solid.Bookmark,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 5.dp),
                                contentDescription = "Literature Node",
                                tint = Color.Gray
                            )
                            val host = formatRef(node.ref!!)
                            if (host != null) {
                                Text(
                                    text = host,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }
                    } else if (isDailyNode(node)) {
                        Icon(
                            imageVector = FontAwesomeIcons.Regular.Calendar,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 5.dp),
                            contentDescription = "Daily Node",
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = node.title,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.padding(5.dp))
                if (!compact) {
                    Text(
                        text = "Modified ${formatRelativeTime(node.lastModified)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}