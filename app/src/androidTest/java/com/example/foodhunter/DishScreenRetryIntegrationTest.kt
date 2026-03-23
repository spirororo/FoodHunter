package com.example.foodhunter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.foodhunter.model.DishDetails
import com.example.foodhunter.model.IngredientLine
import com.example.foodhunter.screens.DishScreen
import com.example.foodhunter.theme.FoodHunterTheme
import com.example.foodhunter.vm.DetailState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DishScreenRetryIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun retryFromErrorState_showsLoadedDishContent() {
        var retryCount = 0

        composeRule.setContent {
            var detailState by mutableStateOf<DetailState>(
                DetailState.Failure("Не удалось загрузить данные блюда: timeout")
            )

            FoodHunterTheme {
                DishScreen(
                    detailState = detailState,
                    onBack = {},
                    onRetry = {
                        retryCount += 1
                        detailState = DetailState.Ready(
                            DishDetails(
                                id = "7",
                                name = "Arrabiata",
                                thumb = "",
                                category = "Pasta",
                                area = "Italian",
                                instructions = "Boil pasta and add sauce",
                                youtube = null,
                                ingredients = listOf(
                                    IngredientLine("Tomato", "2 pcs")
                                )
                            )
                        )
                    }
                )
            }
        }

        composeRule.onNodeWithText("Повторить").performClick()

        assertEquals(1, retryCount)
        composeRule.onNodeWithText("Способ приготовления").assertIsDisplayed()
        composeRule.onNodeWithText("Boil pasta and add sauce").assertIsDisplayed()
    }
}
