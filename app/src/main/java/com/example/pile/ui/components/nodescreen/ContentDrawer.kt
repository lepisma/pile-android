package com.example.pile.ui.components.nodescreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lepisma.orgmode.OrgDocument
import xyz.lepisma.orgmode.OrgInlineElem
import xyz.lepisma.orgmode.OrgSection

fun collectSections(document: OrgDocument): List<OrgSection> {
    return document.content.map { collectSections(it) }.flatten()
}

fun collectSections(section: OrgSection): List<OrgSection> {
    return listOf(section) + section.body.filter { it is OrgSection }.map { collectSections(it as OrgSection) }.flatten()
}

@Composable
fun ContentDrawer(document: OrgDocument) {
    ModalDrawerSheet {
        LazyColumn {
            item {
                Text(
                    "Contents",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()
            }

            items(collectSections(document)) { section ->
                val sectionTitle = section.heading.title.items
                    .filter { it is OrgInlineElem.Text }
                    .joinToString("") { (it as OrgInlineElem.Text).text }

                NavigationDrawerItem(
                    label = { Text(sectionTitle) },
                    selected = false,
                    onClick = { },
                    modifier = Modifier.padding(start = (15 * section.heading.level.level).dp)
                )
            }
        }
    }
}