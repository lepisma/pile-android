package com.example.pile.ui.theme

// In ui/theme/Textures.kt or wherever rememberSimpleFadedCardBackgrounds() is defined
// In ui/theme/Textures.kt or wherever rememberSimpleFadedCardBackgrounds() is defined

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush

@Composable
fun rememberSimpleFadedCardBackgrounds(): List<Brush> {
    val colorScheme = MaterialTheme.colorScheme

    return remember(colorScheme) {
        val fadedAlpha = 0.6f

        listOf(
            Brush.linearGradient(
                colors = listOf(
                    colorScheme.primaryContainer.copy(alpha = fadedAlpha),
                    colorScheme.primaryContainer.copy(alpha = fadedAlpha) // Provide the same color twice
                )
            ),
            // SECONDARY CONTAINER: Use linearGradient with TWO IDENTICAL COLORS
            Brush.linearGradient(
                colors = listOf(
                    colorScheme.secondaryContainer.copy(alpha = fadedAlpha),
                    colorScheme.secondaryContainer.copy(alpha = fadedAlpha)
                )
            ),
            // TERTIARY CONTAINER: Use linearGradient with TWO IDENTICAL COLORS
            Brush.linearGradient(
                colors = listOf(
                    colorScheme.tertiaryContainer.copy(alpha = fadedAlpha),
                    colorScheme.tertiaryContainer.copy(alpha = fadedAlpha)
                )
            ),
            // SURFACE CONTAINER: Use linearGradient with TWO IDENTICAL COLORS
            Brush.linearGradient(
                colors = listOf(
                    colorScheme.surfaceContainer.copy(alpha = fadedAlpha),
                    colorScheme.surfaceContainer.copy(alpha = fadedAlpha)
                )
            ),
            // SURFACE CONTAINER HIGH: Use linearGradient with TWO IDENTICAL COLORS
            Brush.linearGradient(
                colors = listOf(
                    colorScheme.surfaceContainerHigh.copy(alpha = fadedAlpha),
                    colorScheme.surfaceContainerHigh.copy(alpha = fadedAlpha)
                )
            )
        )
    }
}
