package com.example.pile.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pile.OrgListType
import com.example.pile.OrgParagraph

fun measureTextWidth(text: String, textStyle: TextStyle): Float {
    val paint = Paint()
    paint.textSize = textStyle.fontSize.value
    paint.typeface = Typeface.MONOSPACE
    return paint.measureText(text)
}

// TODO: The width calculation is wrong
@Composable
fun OrgListText(orgList: OrgParagraph.OrgList, openNode: (String) -> Unit) {
    val maxBullet = if (orgList.type == OrgListType.UNORDERED) "\u2022" else "${orgList.items.size}."
    val textWidthPixels = measureTextWidth(maxBullet, LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace))
    val maxIndexWidth = with(LocalDensity.current) { textWidthPixels.toDp() }

    Column {
        orgList.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (orgList.type == OrgListType.UNORDERED) "\u2022" else "${index + 1}.",
                    modifier = Modifier
                        .width(maxIndexWidth + 20.dp)
                        .padding(end = 5.dp),
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                )
                Column {
                    item.items.forEach {
                        OrgParagraphText(it, openNode)
                    }
                }
            }
        }
    }
}