package com.example.pile.ui.components.orgmode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronUp
import xyz.lepisma.orgmode.OrgHeading
import xyz.lepisma.orgmode.OrgSection

@Composable
fun OrgSectionHeadingView(
    heading: OrgHeading,
    isCollapsed: Boolean?,
    modifier: Modifier = Modifier,
    openNodeById: (String) -> Unit
) {
    val style = when (heading.level.level) {
        1 -> MaterialTheme.typography.displayMedium
        2 -> MaterialTheme.typography.headlineLarge
        3 -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineSmall
    }.copy(color = MaterialTheme.colorScheme.onSurface)

    Row(
        modifier = modifier.padding(top = 20.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrgInlineElemsView(
            heading.title.items,
            modifier = Modifier.weight(1f),
            style = style,
            openNodeById = openNodeById
        )

        if (isCollapsed != null) {
            val rotation by animateFloatAsState(targetValue = if (!isCollapsed) 0f else 180f, label = "arrowRotation")

            Icon(
                imageVector = FontAwesomeIcons.Solid.ChevronUp,
                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer(rotationZ = rotation)
                    .padding(start = 10.dp)
            )
        }
    }
}

@Composable
fun OrgSectionView(section: OrgSection, modifier: Modifier = Modifier, openNodeById: (String) -> Unit) {
    var isCollapsed by remember { mutableStateOf(true) }

    OrgSectionHeadingView(
        section.heading,
        isCollapsed = if (section.body.isNotEmpty()) { isCollapsed } else { null },
        modifier = Modifier
            .clickable { isCollapsed = !isCollapsed },
        openNodeById = openNodeById
    )

    AnimatedVisibility(
        visible = !isCollapsed,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            for (chunk in section.body) {
                OrgChunkView(chunk, modifier, openNodeById = openNodeById)
            }
        }
    }
}