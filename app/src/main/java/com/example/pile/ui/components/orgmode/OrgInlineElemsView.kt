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

private fun buildOrgInlineString(
    elements: List<OrgInlineElem>,
    uriHandler: UriHandler,
    openNodeById: (String) -> Unit
): AnnotatedString {
    return buildAnnotatedString {
        elements.forEachIndexed { i, elem ->
            // TODO: Skip last line break

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
    openNodeById: (String) -> Unit
) {
    val localUriHandler = LocalUriHandler.current
    Text(
        text = buildOrgInlineString(elements, localUriHandler, openNodeById),
        modifier = modifier,
        style = style
    )
}