package com.example.gymbeacon.di

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitImpl {
    private const val url = "url 입력"

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: RetrofitService = retrofit.create(RetrofitService::class.java)
}