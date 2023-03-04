package com.example.gymbeacon.repository

import com.example.gymbeacon.model.Category

interface CategoryDataSource {

    suspend fun getCategories(): List<Category>
}