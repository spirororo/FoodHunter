package com.example.foodhunter.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY openedAt DESC")
    fun observeAll(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: HistoryItem)

    @Query("DELETE FROM watch_history WHERE dishId = :id")
    suspend fun deleteByDishId(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clear()
}
