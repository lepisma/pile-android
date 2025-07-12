package xyz.lepisma.pile.ui.components.orgmode

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import xyz.lepisma.pile.viewmodel.SharedViewModel
import xyz.lepisma.orgmode.OrgChunk
import xyz.lepisma.orgmode.OrgInlineElem


@Composable
fun OrgParagraphView(
    paragraph: OrgChunk.OrgParagraph,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    openNodeById: (String) -> Unit,
    viewModel: SharedViewModel
) {
    val trimmedElems = trimOrgInlineElems(paragraph.items)

    // If a paragraph only has an attachment link, we show that in a special way
    if (trimmedElems.size == 1 &&
        trimmedElems.first() is OrgInlineElem.Link &&
        (trimmedElems.first() as OrgInlineElem.Link).type == "attachment") {
        OrgAttachmentView(trimmedElems.first() as OrgInlineElem.Link, viewModel = viewModel)
    } else {
        OrgInlineElemsView(
            trimmedElems,
            modifier = modifier,
            style = style,
            openNodeById = openNodeById,
            trimWhitespaces = false
        )
    }
}