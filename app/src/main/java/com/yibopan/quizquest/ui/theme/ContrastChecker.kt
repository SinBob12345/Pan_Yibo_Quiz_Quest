package com.yibopan.quizquest.ui.theme

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Pure Kotlin WCAG 2.1 color contrast checker (A2 commitment #7; WCAG 2.1 AA guidelines).
 */
object ContrastChecker {

    fun relativeLuminance(argb: Int): Double {
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0

        fun adjust(c: Double): Double {
            return if (c <= 0.04045) {
                c / 12.92
            } else {
                ((c + 0.055) / 1.055).pow(2.4)
            }
        }

        val rL = adjust(r)
        val gL = adjust(g)
        val bL = adjust(b)

        return 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
    }

    fun ratio(foreground: Int, background: Int): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        val brighter = max(l1, l2)
        val darker = min(l1, l2)
        return (brighter + 0.05) / (darker + 0.05)
    }
}
