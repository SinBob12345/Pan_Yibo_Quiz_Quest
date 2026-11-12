package com.yibopan.quizquest.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.ui.graphics.vector.ImageVector
import com.yibopan.quizquest.R

/**
 * The 4 primary destinations (Lecture 3: bottom bars suit apps with 3–5 primary destinations).
 * Shown in a NavigationBar on phones and a NavigationRail on large screens.
 */
enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(route = Screens.HomeScreen.route, labelRes = R.string.nav_home, icon = Icons.Rounded.Home),
    REVIEW(route = Screens.ReviewScreen.route, labelRes = R.string.nav_review, icon = Icons.Rounded.Style),
    STATS(route = Screens.StatsScreen.route, labelRes = R.string.nav_stats, icon = Icons.Rounded.BarChart),
    SETTINGS(route = Screens.SettingsScreen.route, labelRes = R.string.nav_settings, icon = Icons.Rounded.Settings),
}
