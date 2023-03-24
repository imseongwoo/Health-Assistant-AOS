package com.example.gymbeacon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymbeacon.network.ApiClient
import com.example.gymbeacon.repository.NaviMyPageRepository
import com.example.gymbeacon.repository.upper.CategoryRemoteDatasource
import com.example.gymbeacon.repository.upper.CategoryRepository
import com.example.gymbeacon.repository.lower.LowerBodyCategoryRemoteDatasource
import com.example.gymbeacon.repository.lower.LowerBodyCategoryRepository
import com.example.gymbeacon.ui.category.CategoryViewModel
import com.example.gymbeacon.ui.category.LowerBodyCategoryViewModel
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel

class ViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> {
                val repository = CategoryRepository(CategoryRemoteDatasource(ApiClient.create()))
                CategoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LowerBodyCategoryViewModel::class.java) -> {
                val repository = LowerBodyCategoryRepository(LowerBodyCategoryRemoteDatasource(ApiClient.create()))
                LowerBodyCategoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(NaviMyPageViewModel::class.java) -> {
                val repository = NaviMyPageRepository()
                NaviMyPageViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Failed to create viewmodel: ${modelClass.name}")
            }
        }
    }
}