package com.example.gymbeacon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymbeacon.model.GymInfo
import com.example.gymbeacon.repository.GymInfoRemoteDatasource
import com.example.gymbeacon.repository.GymInfoRepository
import com.example.gymbeacon.ui.gyminfo.GymInfoViewModel

class ViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GymInfoViewModel::class.java) -> {
                val repository = GymInfoRepository(GymInfoRemoteDatasource(ApiClient.create()))
                GymInfoViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Failed to create viewmodel: ${modelClass.name}")
            }
        }
    }
}