package com.example.gymbeacon.repository.lower

import com.example.gymbeacon.model.LowerBodyCategory

interface LowerBodyCategoryDataSource {
    suspend fun getLowerCategories(): List<LowerBodyCategory>
}