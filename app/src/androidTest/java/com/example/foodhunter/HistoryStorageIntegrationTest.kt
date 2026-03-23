package com.example.foodhunter

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.foodhunter.db.AppDatabase
import com.example.foodhunter.db.HistoryStorage
import com.example.foodhunter.model.Dish
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryStorageIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var historyStorage: HistoryStorage

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        historyStorage = HistoryStorage(database.historyDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordView_replacesExistingDishWithoutCreatingDuplicates() = runTest {
        historyStorage.recordView(
            Dish(id = "1", name = "First name", thumb = "thumb-1", category = null)
        )
        historyStorage.recordView(
            Dish(id = "1", name = "Updated name", thumb = "thumb-2", category = null)
        )

        val history = historyStorage.watchHistory().first()

        assertEquals(1, history.size)
        assertEquals("Updated name", history.single().name)
        assertEquals("thumb-2", history.single().thumb)
    }
}
