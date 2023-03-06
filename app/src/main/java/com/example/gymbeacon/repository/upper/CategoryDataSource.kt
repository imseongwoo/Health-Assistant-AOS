package com.example.gymbeacon.repository.upper

import com.example.gymbeacon.model.Category

interface CategoryDataSource {

    suspend fun getCategories(): List<Category>
}