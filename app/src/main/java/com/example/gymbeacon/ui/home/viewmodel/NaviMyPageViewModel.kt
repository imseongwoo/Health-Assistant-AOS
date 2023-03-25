package com.example.gymbeacon.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gymbeacon.repository.NaviMyPageRepository
import com.example.gymbeacon.model.HealthEntity

class NaviMyPageViewModel(
    private val naviMyPageRepository: NaviMyPageRepository
) : ViewModel() {
    val dbData = MutableLiveData<MutableList<HealthEntity>>()
    val exerciseCountMapLiveData = MutableLiveData<MutableMap<String, Int>>()

    init {
        getDbData(nowTimeStamp="2023-03-19")
    }

    fun getDbData(nowTimeStamp: String): LiveData<MutableList<HealthEntity>> {
        val mutableData = MutableLiveData<MutableList<HealthEntity>>()
        naviMyPageRepository.getDatabaseData(nowTimeStamp).observeForever{
            mutableData.value = it
            dbData.value = it
        }
        return mutableData
    }

    fun getExerciseCountMap(): MutableMap<String, Int> {
        exerciseCountMapLiveData.value = naviMyPageRepository.getExerciseCountMap()
        return naviMyPageRepository.getExerciseCountMap()
    }

}