package com.example.gymbeacon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymbeacon.network.ApiClient
import com.example.gymbeacon.repository.CategoryRemoteDatasource
import com.example.gymbeacon.repository.CategoryRepository
import com.example.gymbeacon.ui.category.CategoryViewModel

class ViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> {
                val repository = CategoryRepository(CategoryRemoteDatasource(ApiClient.create()))
                CategoryViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Failed to create viewmodel: ${modelClass.name}")
            }
        }
    }
}