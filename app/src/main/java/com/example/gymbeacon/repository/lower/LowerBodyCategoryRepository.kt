package com.example.gymbeacon.repository.lower

import com.example.gymbeacon.model.LowerBodyCategory

class LowerBodyCategoryRepository(
    private val lowerBodyCategoryRemoteDatasource: LowerBodyCategoryRemoteDatasource
) {
    suspend fun getLowerCategories(): List<LowerBodyCategory> {
        return lowerBodyCategoryRemoteDatasource.getLowerCategories()
    }
}