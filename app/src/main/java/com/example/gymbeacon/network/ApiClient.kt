package com.example.gymbeacon.network

import com.example.gymbeacon.model.Category
import com.example.gymbeacon.model.LowerBodyCategory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiClient {
    @GET("a/categories.json")
    suspend fun getCategories(): List<Category>

    @GET("lowercategories.json")
    suspend fun getLowerCategories(): List<LowerBodyCategory>

    companion object {

        private const val baseUrl = "https://health-assistant-39e16-default-rtdb.asia-southeast1.firebasedatabase.app/"

        fun create(): ApiClient {

            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiClient::class.java)
        }
    }
}