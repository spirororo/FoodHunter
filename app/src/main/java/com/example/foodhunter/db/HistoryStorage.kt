package com.example.foodhunter.db

import com.example.foodhunter.model.Dish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryStorage @Inject constructor(
    private val dao: HistoryDao
) {
    fun watchHistory(): Flow<List<Dish>> =
        dao.observeAll().map { items ->
            items.map { item ->
                Dish(
                    id = item.dishId,
                    name = item.dishName,
                    thumb = item.dishThumb,
                    category = null
                )
            }
        }

    suspend fun recordView(dish: Dish) {
        dao.upsert(
            HistoryItem(
                dishId = dish.id,
                dishName = dish.name,
                dishThumb = dish.thumb
            )
        )
    }

    suspend fun deleteOne(id: String) {
        dao.deleteByDishId(id)
    }

    suspend fun clearAll() {
        dao.clear()
    }
}
