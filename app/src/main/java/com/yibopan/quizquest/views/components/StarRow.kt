package com.yibopan.quizquest.views.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Row of filled/empty stars.
 * When [reduceMotion] is true, stars appear instantly. When false, stars appear sequentially.
 */
@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 3,
    starSize: Dp = 20.dp,
    reduceMotion: Boolean = false,
) {
    var visibleStars by remember(stars, reduceMotion) { mutableIntStateOf(if (reduceMotion) stars else 0) }

    LaunchedEffect(stars, reduceMotion) {
        if (reduceMotion) {
            visibleStars = stars
        } else {
            visibleStars = 0
            for (i in 1..stars) {
                delay(200L)
                visibleStars = i
            }
        }
    }

    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            Icon(
                imageVector = if (index < visibleStars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
