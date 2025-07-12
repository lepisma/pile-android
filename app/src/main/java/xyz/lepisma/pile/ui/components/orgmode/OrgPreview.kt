package xyz.lepisma.pile.ui.components.orgmode

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
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.ui.unit.dp
import xyz.lepisma.pile.ui.components.nodescreen.ContentDrawer
import xyz.lepisma.pile.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.lepisma.orgmode.OrgDocument
import xyz.lepisma.orgmode.lexer.OrgLexer
import xyz.lepisma.orgmode.parse

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
fun OrgPreview(text: String, viewModel: SharedViewModel, openNodeById: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var document by remember { mutableStateOf<OrgDocument?>(null) }

    LaunchedEffect(text) {
        coroutineScope.launch(Dispatchers.Default) {
            val tokens = OrgLexer(text).tokenize()
            document = parse(tokens)
        }
    }

    if (document != null) {
        // Navigation drawer with table of contents
        ModalNavigationDrawer(
            drawerContent = { ContentDrawer(document!!) }
        ) {
            LazyColumn {
                item {
                    OrgPreambleView(
                        document!!.preamble,
                        viewModel = viewModel
                    )
                }

                items(document!!.preface.body) { chunk ->
                    OrgChunkView(
                        chunk,
                        modifier = Modifier.padding(vertical = 10.dp),
                        openNodeById = openNodeById,
                        viewModel = viewModel
                    )
                }

                items(document!!.content) { section ->
                    OrgSectionView(
                        section,
                        modifier = Modifier.padding(vertical = 10.dp),
                        openNodeById = openNodeById,
                        viewModel = viewModel
                    )
                }
            }
        }
    } else {
        TextLoadingBox()
    }
}