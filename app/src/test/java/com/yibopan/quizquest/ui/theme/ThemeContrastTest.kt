package com.yibopan.quizquest.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [ContrastChecker] and Material 3 theme contrast ratios (A2 Commitment #7; WCAG 2.1 AA). */
class ThemeContrastTest {

    @Test
    fun `ContrastChecker calculates black white and same color ratios accurately`() {
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        val sameColor = 0xFF3F51B5.toInt()

        assertEquals(21.0, ContrastChecker.ratio(black, white), 0.05)
        assertEquals(1.0, ContrastChecker.ratio(sameColor, sameColor), 0.001)
    }

    @Test
    fun `LightColorScheme satisfies WCAG AA contrast thresholds`() {
        assertColorSchemeContrast(LightColorScheme, "Light")
    }

    @Test
    fun `DarkColorScheme satisfies WCAG AA contrast thresholds`() {
        assertColorSchemeContrast(DarkColorScheme, "Dark")
    }

    private fun assertColorSchemeContrast(colorScheme: ColorScheme, schemeName: String) {
        val pairs45 = listOf(
            "primary/onPrimary" to (colorScheme.primary to colorScheme.onPrimary),
            "primaryContainer/onPrimaryContainer" to (colorScheme.primaryContainer to colorScheme.onPrimaryContainer),
            "secondaryContainer/onSecondaryContainer" to (colorScheme.secondaryContainer to colorScheme.onSecondaryContainer),
            "tertiaryContainer/onTertiaryContainer" to (colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer),
            "errorContainer/onErrorContainer" to (colorScheme.errorContainer to colorScheme.onErrorContainer),
            "surface/onSurface" to (colorScheme.surface to colorScheme.onSurface),
            "surfaceVariant/onSurfaceVariant" to (colorScheme.surfaceVariant to colorScheme.onSurfaceVariant),
            "background/onBackground" to (colorScheme.background to colorScheme.onBackground),
        )

        for ((name, pair) in pairs45) {
            val fg = pair.first.toArgb()
            val bg = pair.second.toArgb()
            val ratio = ContrastChecker.ratio(fg, bg)
            assertTrue(
                "[$schemeName] $name contrast ratio $ratio is less than 4.5:1",
                ratio >= 4.5,
            )
        }

        // Primary on Surface >= 3.0:1
        val primaryFg = colorScheme.primary.toArgb()
        val surfaceBg = colorScheme.surface.toArgb()
        val primarySurfaceRatio = ContrastChecker.ratio(primaryFg, surfaceBg)
        assertTrue(
            "[$schemeName] primary/surface contrast ratio $primarySurfaceRatio is less than 3.0:1",
            primarySurfaceRatio >= 3.0,
        )
    }
}
