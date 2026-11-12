package com.yibopan.quizquest.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.yibopan.quizquest.views.HomeScreen
import com.yibopan.quizquest.views.PrivacyScreen
import com.yibopan.quizquest.views.QuizScreen
import com.yibopan.quizquest.views.ResultScreen
import com.yibopan.quizquest.views.ReviewScreen
import com.yibopan.quizquest.views.SettingsScreen
import com.yibopan.quizquest.views.StatsScreen
import com.yibopan.quizquest.views.WorldMapScreen

/**
 * Root composable of the app (Lecture 6: AppNavigation with NavController + NavHost).
 *
 * Lecture 3: the window size class decides the layout — a bottom NavigationBar on phones,
 * a NavigationRail on tablets, foldables and landscape. The tabs are hidden inside the
 * play flow (world map, quiz, result) so the quiz gets the whole screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigation(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showTabs = BottomNavItem.entries.any { it.route == currentRoute }
    val useNavigationRail = windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    Scaffold(
        bottomBar = {
            if (showTabs && !useNavigationRail) {
                AppBottomBar(currentRoute = currentRoute, onTabClicked = { navController.navigateToTab(it) })
            }
        },
        // Each screen has its own Scaffold + TopAppBar that handles the status bar,
        // so this outer Scaffold must not add those insets a second time.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            if (showTabs && useNavigationRail) {
                AppNavigationRail(currentRoute = currentRoute, onTabClicked = { navController.navigateToTab(it) })
            }
            AppNavHost(navController = navController, modifier = Modifier.weight(1f))
        }
    }
}

/** The navigation graph (Lecture 6: composable(route, arguments = listOf(navArgument(...)))). */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screens.HomeScreen.route,
        modifier = modifier,
    ) {
        composable(Screens.HomeScreen.route) {
            HomeScreen(
                onWorldClicked = { categoryId ->
                    navController.navigate(Screens.WorldMapScreen.createRoute(categoryId))
                },
                onReviewClicked = { navController.navigateToTab(Screens.ReviewScreen.route) },
            )
        }

        composable(
            route = Screens.WorldMapScreen.routeWithArgs,
            arguments = listOf(
                navArgument(Screens.ARG_CATEGORY_ID) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt(Screens.ARG_CATEGORY_ID) ?: 0
            WorldMapScreen(
                categoryId = categoryId,
                onBackPressed = { navController.popBackStack() },
                onLevelClicked = { level ->
                    navController.navigate(Screens.QuizScreen.createRoute(categoryId, level))
                },
            )
        }

        composable(
            route = Screens.QuizScreen.routeWithArgs,
            arguments = listOf(
                navArgument(Screens.ARG_CATEGORY_ID) { type = NavType.IntType },
                navArgument(Screens.ARG_LEVEL) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt(Screens.ARG_CATEGORY_ID) ?: 0
            val level = backStackEntry.arguments?.getInt(Screens.ARG_LEVEL) ?: 1
            QuizScreen(
                categoryId = categoryId,
                level = level,
                onBackPressed = { navController.popBackStack() },
                onQuizFinished = { sessionId ->
                    // Remove the finished quiz from the back stack, so Back on the Result
                    // screen returns to the world map instead of a finished quiz.
                    navController.navigate(Screens.ResultScreen.createRoute(sessionId)) {
                        popUpTo(Screens.WorldMapScreen.routeWithArgs) { inclusive = false }
                    }
                },
            )
        }

        composable(
            route = Screens.ResultScreen.routeWithArgs,
            arguments = listOf(
                navArgument(Screens.ARG_SESSION_ID) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong(Screens.ARG_SESSION_ID) ?: 0L
            ResultScreen(
                sessionId = sessionId,
                onBackToMapClicked = {
                    navController.popBackStack(Screens.WorldMapScreen.routeWithArgs, inclusive = false)
                },
                onNextLevelClicked = { categoryId, level ->
                    navController.navigate(Screens.QuizScreen.createRoute(categoryId, level)) {
                        popUpTo(Screens.WorldMapScreen.routeWithArgs) { inclusive = false }
                    }
                },
            )
        }

        composable(Screens.ReviewScreen.route) { ReviewScreen() }
        composable(Screens.StatsScreen.route) { StatsScreen() }
        composable(Screens.SettingsScreen.route) {
            SettingsScreen(onPrivacyClicked = { navController.navigate(Screens.PrivacyScreen.route) })
        }
        composable(Screens.PrivacyScreen.route) {
            PrivacyScreen(onBackPressed = { navController.popBackStack() })
        }
    }
}

/**
 * Switches tabs the Material way: one copy of each tab on the back stack, and each tab keeps
 * its own state (e.g. scroll position) when you come back to it.
 */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppBottomBar(currentRoute: String?, onTabClicked: (String) -> Unit) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onTabClicked(item.route) },
                // The visible label already names the tab, so the icon is decorative.
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(text = stringResource(item.labelRes)) },
            )
        }
    }
}

@Composable
private fun AppNavigationRail(currentRoute: String?, onTabClicked: (String) -> Unit) {
    NavigationRail {
        BottomNavItem.entries.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = { onTabClicked(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(text = stringResource(item.labelRes)) },
            )
        }
    }
}
