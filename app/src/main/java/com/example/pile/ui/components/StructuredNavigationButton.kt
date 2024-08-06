package com.example.pile.ui.components

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowsAlt
import kotlin.math.max
import kotlin.math.min


enum class StructuredNavigationDirection {
    UP, DOWN
}

/**
 * Button with swipe gestures that let's you navigate and scroll to meaningful structures.
 *
 * This specific button is supposed to navigate headlines in Org mode documents. It keeps track of
 * headline level to work on and triggers callback to go up or down in that level. Actual navigation
 * is left to the user of this component.
 */
@Composable
fun StructuredNavigationButton(onSwipe: (dir: StructuredNavigationDirection, level: Int) -> Unit) {
    var dragDetected by remember { mutableStateOf(false) }
    var currentLevel by remember { mutableIntStateOf(1) }

    IconButton(
        enabled = true,
        onClick = {},
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragDetected = false
                    },
                    onDragEnd = {
                        dragDetected = false
                    },
                    onDragCancel = {
                        dragDetected = false
                    }
                ) { _, dragAmount ->
                    if (!dragDetected) {
                        dragDetected = true
                        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                            currentLevel = if (dragAmount.x > 0) {
                                // Max level is set at 4
                                min(4, currentLevel + 1)
                            } else {
                                max(1, currentLevel - 1)
                            }
                        } else {
                            if (dragAmount.y > 0) {
                                onSwipe(StructuredNavigationDirection.DOWN, currentLevel)
                            } else {
                                onSwipe(StructuredNavigationDirection.UP, currentLevel)
                            }
                        }
                    }
                }
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                FontAwesomeIcons.Solid.ArrowsAlt,
                modifier = Modifier
                    .size(SwitchDefaults.IconSize)
                    .padding(end = 5.dp),
                contentDescription = "Structured Scrolling"
            )
            Text("H$currentLevel")
        }
    }
}
