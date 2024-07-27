package com.example.pile.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.OrgParagraph
import com.example.pile.orgmode.dropPreamble
import com.example.pile.orgmode.parseOrg
import com.example.pile.orgmode.parseOrgParagraphs
import com.example.pile.orgmode.parseTags
import com.example.pile.orgmode.parseTitle
import com.orgzly.org.parser.OrgParsedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ShimmerBox(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "")
    val animatedProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.LightGray, Color.LightGray),
                    start = Offset(10f, 10f),
                    end = Offset(300f, 300f)
                ),
                alpha = animatedProgress,
                shape = RoundedCornerShape(5.dp)
            )
    )
}

@Composable
fun TextLoadingBox() {
    Column {
        ShimmerBox(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 20.dp)
                .size(width = 200.dp, height = 40.dp)
        )

        val randomWidths = remember { List(6) { (100..300).random().dp } }

        randomWidths.forEach {
            ShimmerBox(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .size(width = it, height = 10.dp)
            )
        }
    }
}


@Composable
fun OrgPreview(text: String, openNodeById: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var parsed by remember { mutableStateOf<OrgParsedFile?>(null) }

    LaunchedEffect(text) {
        coroutineScope.launch(Dispatchers.Default) {
            val parsedData = parseOrg(dropPreamble(text))
            withContext(Dispatchers.Main) {
                parsed = parsedData
            }
        }
    }

    if (parsed != null) {
        LazyColumn {
            item {
                OrgTitleText(title = parseTitle(text))
                OrgRefButton(text = text)
                OrgTags(tags = parseTags(text))
            }

            items(parseOrgParagraphs(parsed!!.file.preface)) {
                OrgParagraphText(it, openNodeById)
            }

            parsed!!.headsInList.forEach { head ->
                item {
                    OrgHeadingText(head, openNodeById)
                }

                items(parseOrgParagraphs(head.head.content)) { para ->
                    OrgParagraphText(para, openNodeById)
                }
            }
        }
    } else {
        TextLoadingBox()
    }
}

@Composable
fun OrgParagraphText(orgParagraph: OrgParagraph, openNodeById: (String) -> Unit) {
    when(orgParagraph) {
        is OrgParagraph.OrgHorizontalLine -> OrgHorizontalLine()
        is OrgParagraph.OrgTable -> Text(orgParagraph.text, fontFamily = FontFamily.Monospace)
        is OrgParagraph.OrgList -> OrgListText(orgParagraph, openNodeById)
        is OrgParagraph.OrgQuote -> OrgQuoteText(orgParagraph, openNodeById)
        is OrgParagraph.OrgBlock -> OrgBlockText(orgParagraph)
        is OrgParagraph.OrgLogBook -> Text("")
        else -> OrgText(orgParagraph.text, openNodeById)
    }
}