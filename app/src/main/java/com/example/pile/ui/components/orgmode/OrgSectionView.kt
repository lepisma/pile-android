package com.example.pile.ui.components.orgmode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgHeading
import com.example.pile.orgmode.OrgInlineElem
import com.example.pile.orgmode.OrgSection

@Composable
fun OrgSectionHeadingView(heading: OrgHeading, modifier: Modifier = Modifier) {
    val style = when (heading.level.level) {
        1 -> MaterialTheme.typography.displayMedium
        2 -> MaterialTheme.typography.headlineLarge
        3 -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineSmall
    }.copy(color = MaterialTheme.colorScheme.onSurface)

    Text(
        text = heading.title.items
            .filter { it is OrgInlineElem.Text }
            .joinToString("") { (it as OrgInlineElem.Text).text },
        style = style,
        modifier = modifier.padding(top = 20.dp, bottom = 5.dp)
    )
}

@Composable
fun OrgSectionView(section: OrgSection, modifier: Modifier = Modifier) {
    OrgSectionHeadingView(section.heading)

    for (chunk in section.body) {
        OrgChunkView(chunk, modifier)
    }
}