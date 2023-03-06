package com.example.gymbeacon.repository.lower

import com.example.gymbeacon.model.LowerBodyCategory
import com.example.gymbeacon.network.ApiClient

class LowerBodyCategoryRemoteDatasource(private val apiClient: ApiClient): LowerBodyCategoryDataSource {
    override suspend fun getLowerCategories(): List<LowerBodyCategory> {
        return apiClient.getLowerCategories()
    }
}