package com.example.foodhunter.repo

import com.example.foodhunter.model.Dish
import com.example.foodhunter.model.DishDetails
import com.example.foodhunter.model.IngredientLine
import com.example.foodhunter.net.MealDbService
import com.example.foodhunter.net.RawMeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepo @Inject constructor(
    private val api: MealDbService
) {
    suspend fun search(query: String): List<Dish> = withContext(Dispatchers.IO) {
        val response = api.findByName(query)
        response.meals?.map(RawMeal::toDish).orEmpty()
    }

    suspend fun loadDetails(id: String): DishDetails? = withContext(Dispatchers.IO) {
        val response = api.getById(id)
        response.meals?.firstOrNull()?.toDetails()
    }
}

private fun RawMeal.toDish() = Dish(
    id = idMeal,
    name = strMeal,
    thumb = strMealThumb ?: "",
    category = strCategory
)

private fun RawMeal.toDetails(): DishDetails {
    val ingredients = listOf(
        strIngredient1 to strMeasure1,
        strIngredient2 to strMeasure2,
        strIngredient3 to strMeasure3,
        strIngredient4 to strMeasure4,
        strIngredient5 to strMeasure5,
        strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7,
        strIngredient8 to strMeasure8,
        strIngredient9 to strMeasure9,
        strIngredient10 to strMeasure10,
        strIngredient11 to strMeasure11,
        strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13,
        strIngredient14 to strMeasure14,
        strIngredient15 to strMeasure15,
        strIngredient16 to strMeasure16,
        strIngredient17 to strMeasure17,
        strIngredient18 to strMeasure18,
        strIngredient19 to strMeasure19,
        strIngredient20 to strMeasure20
    )
        .filter { (name, _) -> !name.isNullOrBlank() }
        .map { (name, measure) ->
            IngredientLine(
                what = name!!.trim(),
                howMuch = measure?.trim() ?: ""
            )
        }

    return DishDetails(
        id = idMeal,
        name = strMeal,
        thumb = strMealThumb ?: "",
        category = strCategory ?: "—",
        area = strArea ?: "—",
        instructions = strInstructions ?: "",
        youtube = strYoutube,
        ingredients = ingredients
    )
}
