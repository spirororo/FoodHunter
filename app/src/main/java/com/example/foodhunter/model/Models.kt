package com.example.foodhunter.model

// короткая модель для списка - только самое нужное
data class Dish(
    val id: String,
    val name: String,
    val thumb: String,
    val category: String?
)

// полная инфа о блюде когда открываем детали
data class DishDetails(
    val id: String,
    val name: String,
    val thumb: String,
    val category: String,
    val area: String,
    val instructions: String,
    val youtube: String?,
    val ingredients: List<IngredientLine>
)

// одна строчка ингредиента - что и сколько
data class IngredientLine(
    val what: String,
    val howMuch: String
)
