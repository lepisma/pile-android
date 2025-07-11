package com.example.pile.ui.components.orgmode

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import xyz.lepisma.orgmode.OrgInlineElem

/**
 * Trim elements so that the plain text items at both ends that match whitespaces are removed
 */
private fun trimOrgInlineElems(elements: List<OrgInlineElem>): List<OrgInlineElem> {
    fun isWhitespace(elem: OrgInlineElem): Boolean {
        // Note that there could be marked-up text that could also be whitespace. For example:
        // "/hell world   /" which has trailing whitespace inside emphasis marker. For now we don't
        // consider that.
        return elem is OrgInlineElem.Text && elem.text.trim().isBlank()
    }

    return elements
        .dropWhile { isWhitespace(it) }
        .dropLastWhile { isWhitespace(it) }
}

private fun buildOrgInlineString(
    elements: List<OrgInlineElem>,
    uriHandler: UriHandler,
    openNodeById: (String) -> Unit,
    trimWhitespaces: Boolean = true
): AnnotatedString {
    return buildAnnotatedString {
        (if (trimWhitespaces) { trimOrgInlineElems(elements) } else { elements }).forEachIndexed { i, elem ->
            when (elem) {
                is OrgInlineElem.Text -> append(elem.text)
                is OrgInlineElem.Link -> {
                    when (elem.type) {
                        "id" -> append("‹ ")
                        "attachment" -> append("[")
                        else -> { }
                    }
                    withLink(
                        LinkAnnotation.Url(
                            elem.target,
                            TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline))
                        ) {
                            val url = (it as LinkAnnotation.Url).url

                            if (url.startsWith("http")) {
                                uriHandler.openUri(url)
                            } else if (elem.type == "id") {
                                openNodeById(url)
                            } else if (elem.type == "attachment") {
                                // TODO:
                            }
                        }
                    ) {
                        if (elem.title != null) {
                            append(buildOrgInlineString(elem.title!!, uriHandler, openNodeById))
                        } else {
                            append(elem.target)
                        }
                    }

                    when (elem.type) {
                        "id" -> append(" ›")
                        "attachment" -> append("]")
                        else -> { }
                    }
                }
                else -> {  }
            }
        }
    }
}

/**
 * View a list of OrgInlineElem items
 */
@Composable
fun OrgInlineElemsView(
    elements: List<OrgInlineElem>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    openNodeById: (String) -> Unit,
    trimWhitespaces: Boolean = true
) {
    val localUriHandler = LocalUriHandler.current
    Text(
        text = buildOrgInlineString(
            elements,
            uriHandler = localUriHandler,
            openNodeById = openNodeById,
            trimWhitespaces = trimWhitespaces
        ),
        modifier = modifier,
        style = style
    )
}