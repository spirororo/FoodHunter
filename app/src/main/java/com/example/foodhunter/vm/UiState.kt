package com.example.foodhunter.vm

import com.example.foodhunter.model.Dish
import com.example.foodhunter.model.DishDetails

sealed interface SearchState {
    data object Empty : SearchState
    data object Loading : SearchState
    data class Failure(val msg: String) : SearchState
    data class Found(val dishes: List<Dish>) : SearchState
}

sealed interface DetailState {
    data object Idle : DetailState
    data object Loading : DetailState
    data class Failure(val msg: String) : DetailState
    data class Ready(val dish: DishDetails) : DetailState
}
