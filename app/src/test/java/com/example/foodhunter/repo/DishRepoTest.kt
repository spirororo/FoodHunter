package com.example.foodhunter.repo

import com.example.foodhunter.model.Dish
import com.example.foodhunter.model.DishDetails
import com.example.foodhunter.model.IngredientLine
import com.example.foodhunter.net.MealDbSearchResult
import com.example.foodhunter.net.MealDbService
import com.example.foodhunter.net.RawMeal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DishRepoTest {

    @Test
    fun search_mapsApiResponseToDishList() = runTest {
        val repository = DishRepo(
            mealDbService(
                mealsForSearch = listOf(
                    rawMeal(id = "1", name = "Tom Yum", category = "Soup"),
                    rawMeal(id = "2", name = "Tacos", category = "Street Food")
                )
            )
        )

        val dishes = repository.search("t")

        assertEquals(
            listOf(
                Dish(id = "1", name = "Tom Yum", thumb = "", category = "Soup"),
                Dish(id = "2", name = "Tacos", thumb = "", category = "Street Food")
            ),
            dishes
        )
    }

    @Test
    fun loadDetails_mapsIngredientsAndFallbackValues() = runTest {
        val repository = DishRepo(
            mealDbService(
                mealsForDetails = listOf(
                    rawMeal(
                        id = "9",
                        name = "Rice Bowl",
                        instructions = "Cook and serve",
                        ingredient1 = "Rice",
                        measure1 = "200 g",
                        ingredient2 = "  ",
                        measure2 = "skip",
                        ingredient3 = "Egg",
                        measure3 = "1 pc"
                    )
                )
            )
        )

        val details = repository.loadDetails("9")

        assertEquals(
            DishDetails(
                id = "9",
                name = "Rice Bowl",
                thumb = "",
                category = "—",
                area = "—",
                instructions = "Cook and serve",
                youtube = null,
                ingredients = listOf(
                    IngredientLine("Rice", "200 g"),
                    IngredientLine("Egg", "1 pc")
                )
            ),
            details
        )
    }

    @Test
    fun loadDetails_returnsNullWhenApiReturnsNoMeals() = runTest {
        val repository = DishRepo(mealDbService(mealsForDetails = emptyList()))

        val details = repository.loadDetails("404")

        assertNull(details)
    }

    private fun mealDbService(
        mealsForSearch: List<RawMeal>? = emptyList(),
        mealsForDetails: List<RawMeal>? = emptyList()
    ) = object : MealDbService {
        override suspend fun findByName(name: String): MealDbSearchResult {
            return MealDbSearchResult(mealsForSearch)
        }

        override suspend fun getById(id: String): MealDbSearchResult {
            return MealDbSearchResult(mealsForDetails)
        }
    }

    private fun rawMeal(
        id: String,
        name: String,
        category: String? = null,
        area: String? = null,
        instructions: String? = null,
        ingredient1: String? = null,
        measure1: String? = null,
        ingredient2: String? = null,
        measure2: String? = null,
        ingredient3: String? = null,
        measure3: String? = null
    ) = RawMeal(
        idMeal = id,
        strMeal = name,
        strCategory = category,
        strArea = area,
        strInstructions = instructions,
        strMealThumb = null,
        strYoutube = null,
        strIngredient1 = ingredient1,
        strMeasure1 = measure1,
        strIngredient2 = ingredient2,
        strMeasure2 = measure2,
        strIngredient3 = ingredient3,
        strMeasure3 = measure3,
        strIngredient4 = null,
        strMeasure4 = null,
        strIngredient5 = null,
        strMeasure5 = null,
        strIngredient6 = null,
        strMeasure6 = null,
        strIngredient7 = null,
        strMeasure7 = null,
        strIngredient8 = null,
        strMeasure8 = null,
        strIngredient9 = null,
        strMeasure9 = null,
        strIngredient10 = null,
        strMeasure10 = null,
        strIngredient11 = null,
        strMeasure11 = null,
        strIngredient12 = null,
        strMeasure12 = null,
        strIngredient13 = null,
        strMeasure13 = null,
        strIngredient14 = null,
        strMeasure14 = null,
        strIngredient15 = null,
        strMeasure15 = null,
        strIngredient16 = null,
        strMeasure16 = null,
        strIngredient17 = null,
        strMeasure17 = null,
        strIngredient18 = null,
        strMeasure18 = null,
        strIngredient19 = null,
        strMeasure19 = null,
        strIngredient20 = null,
        strMeasure20 = null
    )
}
