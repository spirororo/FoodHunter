package com.example.foodhunter.model

data class Dish(
    val id: String,
    val name: String,
    val thumb: String,
    val category: String?
)

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

data class IngredientLine(
    val what: String,
    val howMuch: String
)
