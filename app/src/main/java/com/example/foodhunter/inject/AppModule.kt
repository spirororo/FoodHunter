package com.example.foodhunter.inject

import android.content.Context
import androidx.room.Room
import com.example.foodhunter.db.AppDatabase
import com.example.foodhunter.db.HistoryDao
import com.example.foodhunter.net.MealDbService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// один модуль на все зависимости - и сеть и база тут
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val MEALDB_URL = "https://www.themealdb.com/api/json/v1/1/"

    // собираем okhttp с логами для дебага
    @Provides
    @Singleton
    fun httpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(MEALDB_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun mealApi(retrofit: Retrofit): MealDbService =
        retrofit.create(MealDbService::class.java)

    // база данных для истории просмотров
    @Provides
    @Singleton
    fun database(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "foodhunter.db")
            .build()

    @Provides
    fun historyDao(db: AppDatabase): HistoryDao = db.historyDao()
}
