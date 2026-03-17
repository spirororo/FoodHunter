package com.example.foodhunter.net

import retrofit2.http.GET
import retrofit2.http.Query

// интерфейс для themealdb - всё бесплатно, ключей не надо
interface MealDbService {

    @GET("search.php")
    suspend fun findByName(@Query("s") name: String): MealDbSearchResult

    @GET("lookup.php")
    suspend fun getById(@Query("i") id: String): MealDbSearchResult
}
