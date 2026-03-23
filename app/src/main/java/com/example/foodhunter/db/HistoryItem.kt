package com.example.foodhunter.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryItem(
    @PrimaryKey
    val dishId: String,
    val dishName: String,
    val dishThumb: String,
    val openedAt: Long = System.currentTimeMillis()
)
