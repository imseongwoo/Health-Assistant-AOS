package com.example.gymbeacon.repository

import com.example.gymbeacon.model.Category
import com.example.gymbeacon.network.ApiClient

class CategoryRemoteDatasource(private val apiClient: ApiClient): CategoryDataSource {
    override suspend fun getCategories(): List<Category> {
        return apiClient.getCategories()
    }

}