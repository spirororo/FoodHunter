package com.example.foodhunter.db

import com.example.foodhunter.model.Dish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// обёртка над дао, чтобы вьюмодель не работала напрямую с entity
@Singleton
class HistoryStorage @Inject constructor(
    private val dao: HistoryDao
) {
    // достаём историю и сразу маппим в нормальные модели
    fun watchHistory(): Flow<List<Dish>> =
        dao.getHistory().map { list ->
            list.map { entry ->
                Dish(
                    id = entry.dishId,
                    name = entry.dishName,
                    thumb = entry.dishThumb,
                    category = null
                )
            }
        }

    // записываем что юзер посмотрел блюдо
    suspend fun recordView(dish: Dish) {
        dao.saveView(
            HistoryItem(
                dishId = dish.id,
                dishName = dish.name,
                dishThumb = dish.thumb
            )
        )
    }

    // удаляем одну запись
    suspend fun deleteOne(id: String) {
        dao.remove(id)
    }

    // ну и полная очистка если надо
    suspend fun clearAll() {
        dao.nukeAll()
    }
}
