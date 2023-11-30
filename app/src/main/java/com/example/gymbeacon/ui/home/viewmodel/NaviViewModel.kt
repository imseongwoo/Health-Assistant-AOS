package com.example.gymbeacon.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WeightData
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

    private val _recentWeightData = MutableLiveData<WeightData>()
    val recentWeightData: MutableLiveData<WeightData> get() = _recentWeightData

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
                    _recentWeightData.value = getRecentWeightData(it.entityArrayList)

                }
            }
        }
    }

    fun getRecentWeightData(data: ArrayList<HealthEntity>): WeightData {
        val recentDate = data.last().timestamp
        var countDeadLift = 0
        var countLegExtenstion = 0
        var countSquat = 0
        var countLatPullDown = 0
        var countInclineBench = 0
        var countBench = 0
        for (recentEntity in data) {
            if (recentEntity.timestamp == recentDate) {
                Log.e("datetest", "${recentEntity}]")
                when (recentEntity.exercise) {
                    "데드리프트" -> countDeadLift += recentEntity.count?.toInt() ?: 0
                    "레그 익스텐션" -> countLegExtenstion += recentEntity.count?.toInt() ?: 0
                    "스쿼트" -> countSquat += recentEntity.count?.toInt() ?: 0
                    "랫 풀 다운" -> countLatPullDown += recentEntity.count?.toInt() ?: 0
                    "인클라인 벤치프레스" -> countInclineBench += recentEntity.count?.toInt() ?: 0
                    "벤치프레스" -> countBench += recentEntity.count?.toInt() ?: 0
                }

            }
        }
        return WeightData(recentDate,
            countDeadLift,
            countLegExtenstion,
            countSquat,
            countLatPullDown,
            countInclineBench,
            countBench)
    }

}