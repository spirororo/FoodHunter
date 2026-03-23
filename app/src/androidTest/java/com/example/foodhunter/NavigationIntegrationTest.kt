package com.example.foodhunter

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.foodhunter.model.Dish
import com.example.foodhunter.model.DishDetails
import com.example.foodhunter.screens.DishScreen
import com.example.foodhunter.screens.HomeScreen
import com.example.foodhunter.theme.FoodHunterTheme
import com.example.foodhunter.vm.DetailState
import com.example.foodhunter.vm.SearchState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectingDishFromSearch_navigatesToDetailsForTheSameId() {
        val dish = Dish(id = "42", name = "Tom Yum", thumb = "", category = "Soup")

        composeRule.setContent {
            FoodHunterTheme {
                SearchToDetailsTestHost(dish = dish)
            }
        }

        composeRule.onAllNodesWithText("Instruction for 42").assertCountEquals(0)
        composeRule.onNodeWithText("Tom Yum").performClick()
        composeRule.onNodeWithText("Instruction for 42").assertIsDisplayed()
    }

    @Composable
    private fun SearchToDetailsTestHost(dish: Dish) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    query = "",
                    searchState = SearchState.Found(listOf(dish)),
                    onQueryChange = {},
                    onSearch = {},
                    onDishSelected = { selectedDish ->
                        navController.navigate("detail/${selectedDish.id}")
                    }
                )
            }

            composable(
                route = "detail/{dishId}",
                arguments = listOf(navArgument("dishId") { type = NavType.StringType })
            ) { backStackEntry ->
                val dishId = backStackEntry.arguments?.getString("dishId").orEmpty()
                DishScreen(
                    detailState = DetailState.Ready(createDetails(dishId)),
                    onBack = {},
                    onRetry = {}
                )
            }
        }
    }

    private fun createDetails(id: String) = DishDetails(
        id = id,
        name = "Dish $id",
        thumb = "",
        category = "Soup",
        area = "Thai",
        instructions = "Instruction for $id",
        youtube = null,
        ingredients = emptyList()
    )
}
