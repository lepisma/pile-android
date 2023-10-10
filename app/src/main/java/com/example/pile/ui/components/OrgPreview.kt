package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.pile.OrgParagraph
import com.example.pile.dropPreamble
import com.example.pile.parseOrg
import com.example.pile.parseOrgParagraphs
import com.example.pile.parseTitle

@Composable
fun OrgPreview(text: String, openNode: (String) -> Unit) {
    Column {
        OrgTitleText(title = parseTitle(text))
        OrgRefButton(text = text)
        val parsed = parseOrg(dropPreamble(text))
        Column {
            parseOrgParagraphs(parsed.file.preface).forEach {
                OrgParagraphText(it, openNode)
            }

            parsed.headsInList.forEach {
                val style = when (it.level) {
                    1 -> MaterialTheme.typography.headlineLarge
                    2 -> MaterialTheme.typography.headlineMedium
                    else -> MaterialTheme.typography.headlineSmall
                }

                Text(
                    text = it.head.title,
                    style = style,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                )
                parseOrgParagraphs(it.head.content).forEach { para ->
                    OrgParagraphText(para, openNode)
                }
            }
        }
    }
}

@Composable
fun OrgParagraphText(orgParagraph: OrgParagraph, openNode: (String) -> Unit) {
    when(orgParagraph) {
        is OrgParagraph.OrgHorizontalLine -> OrgHorizontalLine()
        is OrgParagraph.OrgTable -> Text(orgParagraph.text, fontFamily = FontFamily.Monospace)
        is OrgParagraph.OrgList -> OrgListText(orgParagraph)
        is OrgParagraph.OrgQuote -> OrgQuoteText(orgParagraph)
        is OrgParagraph.OrgBlock -> Text(orgParagraph.text, fontFamily = FontFamily.Monospace)
        else -> OrgText(orgParagraph.text, openNode)
    }
}