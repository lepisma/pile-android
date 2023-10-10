package com.example.pile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pile.OrgParagraph

@Composable
fun OrgListText(orgList: OrgParagraph.OrgList) {
    var layoutWidth by remember { mutableFloatStateOf(0f) }

    Text(
        text = orgList.items.size.toString() + ".",
        modifier = Modifier.alpha(0f).onGloballyPositioned { position ->
            if (layoutWidth == 0f) {
                layoutWidth = position.size.width.toFloat()
            }
        }
    )

    val maxIndexWidth = with(LocalDensity.current) { layoutWidth.toDp() + 5.dp }

    Column {
        orgList.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${index + 1}.",
                    modifier = Modifier.width(maxIndexWidth).padding(end = 5.dp),
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                )
                Text(text = item.text)
            }
        }
    }
}