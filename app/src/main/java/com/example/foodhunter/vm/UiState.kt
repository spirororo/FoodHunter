package com.example.foodhunter.vm

import com.example.foodhunter.model.Dish
import com.example.foodhunter.model.DishDetails

// состояния экрана поиска - используем sealed чтоб when был исчерпывающий
sealed interface SearchState {
    data object Empty : SearchState
    data object Loading : SearchState
    data class Failure(val msg: String) : SearchState
    data class Found(val dishes: List<Dish>) : SearchState
}

// состояния экрана деталей
sealed interface DetailState {
    data object Loading : DetailState
    data class Failure(val msg: String) : DetailState
    data class Ready(val dish: DishDetails) : DetailState
}
