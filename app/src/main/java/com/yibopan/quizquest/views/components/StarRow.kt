package com.yibopan.quizquest.views.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Row of filled/empty stars. The icons are decorative (contentDescription = null): the parent
 * composable must describe the star count in words, e.g. with
 * `Modifier.semantics { stateDescription = "2 of 3 stars" }` (Lecture 3).
 */
@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 3,
    starSize: Dp = 20.dp,
) {
    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            Icon(
                imageVector = if (index < stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
