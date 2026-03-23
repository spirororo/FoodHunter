package com.example.foodhunter.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodhunter.screens.DishScreen
import com.example.foodhunter.screens.HistoryScreen
import com.example.foodhunter.screens.HomeScreen
import com.example.foodhunter.vm.FoodViewModel

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val DISH = "dish/{dishId}"
    fun dishRoute(id: String) = "dish/$id"
}

private data class Tab(val route: String, val label: String, val icon: @Composable () -> Unit)

private val TABS = listOf(
    Tab(Routes.HOME, "Поиск блюд") { Icon(Icons.Default.Home, contentDescription = "Поиск блюд") },
    Tab(Routes.HISTORY, "История") { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "История") }
)

@Composable
fun AppNavGraph() {
    val vm: FoodViewModel = hiltViewModel()
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val showBottomBar = currentRoute?.route in listOf(Routes.HOME, Routes.HISTORY)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = tab.icon,
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val query by vm.query.collectAsState()
        val searchState by vm.searchState.collectAsState()
        val detailState by vm.detailState.collectAsState()
        val history by vm.history.collectAsState()

        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    query = query,
                    searchState = searchState,
                    onQueryChange = vm::changeQuery,
                    onSearch = vm::doSearch,
                    onDishSelected = { dish ->
                        nav.navigate(Routes.dishRoute(dish.id))
                    }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    history = history,
                    onDishSelected = { dish ->
                        nav.navigate(Routes.dishRoute(dish.id))
                    },
                    onRemoveDish = vm::removeFromHistory,
                    onClearAll = vm::wipeHistory
                )
            }

            composable(
                route = Routes.DISH,
                arguments = listOf(navArgument("dishId") { type = NavType.StringType })
            ) { entry ->
                val dishId = entry.arguments?.getString("dishId")
                if (dishId != null) {
                    LaunchedEffect(dishId) {
                        vm.openDishById(dishId)
                    }
                } else {
                    LaunchedEffect(Unit) {
                        vm.showInvalidDishRequest()
                    }
                }
                DishScreen(
                    detailState = detailState,
                    onBack = { nav.popBackStack() },
                    onRetry = vm::retryOpenDish
                )
            }
        }
    }
}
