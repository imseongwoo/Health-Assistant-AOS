package com.example.gymbeacon.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.base.RespResult
import com.example.domain.model.HealthEntity
import com.example.domain.model.NaviHomeEntity
import com.example.domain.repository.NaviRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NaviViewModel @Inject constructor(
    private val repository: NaviRepository,
) : ViewModel() {
    val dbData = MutableLiveData<MutableList<HealthEntity>>()
    val exerciseCountMapLiveData = MutableLiveData<MutableMap<String, Int>>()

    private val _pieChartData = MutableLiveData<NaviHomeEntity>()
    val pieChartData: MutableLiveData<NaviHomeEntity> get() = _pieChartData

    init {
        getDbData(nowTimeStamp = "2023-03-19")
    }

    fun getDbData(nowTimeStamp: String): LiveData<MutableList<HealthEntity>> {
        val mutableData = MutableLiveData<MutableList<HealthEntity>>()
        repository.getDatabaseData(nowTimeStamp).observeForever {
            mutableData.value = it
            dbData.value = it
            Log.d("data", "${it}")
        }
        return mutableData
    }

    fun getExerciseCountMap(): MutableMap<String, Int> {
        exerciseCountMapLiveData.value = repository.getExerciseCountMap()
        return repository.getExerciseCountMap()
    }

    fun getHomeWeightData() {
        viewModelScope.launch {
            repository.getHomeWeightData {
                if (it.isSuccess) {
                    _pieChartData.value = it
                }

            }

        }

    }

}