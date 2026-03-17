package com.example.foodhunter.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // получаем историю отсортированную по дате - свежие сверху
    @Query("SELECT * FROM watch_history ORDER BY openedAt DESC")
    fun getHistory(): Flow<List<HistoryItem>>

    // сохраняем просмотр, если уже было - обновляем время
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveView(item: HistoryItem)

    // чистим одну запись
    @Query("DELETE FROM watch_history WHERE dishId = :id")
    suspend fun remove(id: String)

    // чистим вообще всё
    @Query("DELETE FROM watch_history")
    suspend fun nukeAll()
}
