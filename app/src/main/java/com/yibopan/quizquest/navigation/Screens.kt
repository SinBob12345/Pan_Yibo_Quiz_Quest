package com.yibopan.quizquest.navigation

/**
 * Every destination of the app (Lecture 6: "Every time we need to add a new destination,
 * we'll add a new object here!"). Destinations with arguments also have a route pattern
 * (`routeWithArgs`) and a helper that builds the real route (`createRoute`).
 */
sealed class Screens(val route: String) {

    // ----- Tabs in the bottom navigation bar / navigation rail -----
    object HomeScreen : Screens("home")
    object ReviewScreen : Screens("review")
    object StatsScreen : Screens("stats")
    object SettingsScreen : Screens("settings")

    // ----- Other destinations -----
    object PrivacyScreen : Screens("privacy")

    object WorldMapScreen : Screens("worldMap") {
        val routeWithArgs = "$route/{$ARG_CATEGORY_ID}"
        fun createRoute(categoryId: Int) = "$route/$categoryId"
    }

    object QuizScreen : Screens("quiz") {
        val routeWithArgs = "$route/{$ARG_CATEGORY_ID}/{$ARG_LEVEL}"
        fun createRoute(categoryId: Int, level: Int) = "$route/$categoryId/$level"
    }

    object ResultScreen : Screens("result") {
        val routeWithArgs = "$route/{$ARG_SESSION_ID}/{$ARG_CATEGORY_ID}/{$ARG_LEVEL}"
        fun createRoute(sessionId: Long, categoryId: Int = 0, level: Int = 1) = "$route/$sessionId/$categoryId/$level"
    }

    companion object {
        const val ARG_CATEGORY_ID = "categoryId"
        const val ARG_LEVEL = "level"
        const val ARG_SESSION_ID = "sessionId"
    }
}
