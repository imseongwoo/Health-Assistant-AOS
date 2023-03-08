package com.example.gymbeacon.repository.upper

import com.example.gymbeacon.model.Category

class CategoryRepository(
    private val remoteDatasource: CategoryRemoteDatasource,
) {

    suspend fun getCategories(): List<Category> {
        return remoteDatasource.getCategories()
    }
}