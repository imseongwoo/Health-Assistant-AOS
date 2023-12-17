package com.example.domain.repository

import androidx.lifecycle.LiveData
import com.example.domain.base.RespResult
import com.example.domain.model.HealthEntity
import com.example.domain.model.NaviHomeEntity

interface NaviRepository {
    fun getDatabaseData(nowTimeStamp: String): LiveData<MutableList<HealthEntity>>

    fun setExerciseCountMap(map: MutableMap<String, Int>)

    fun getExerciseCountMap(): MutableMap<String, Int>

    suspend fun getHomeWeightData(onChanged: (NaviHomeEntity) -> Unit)
}