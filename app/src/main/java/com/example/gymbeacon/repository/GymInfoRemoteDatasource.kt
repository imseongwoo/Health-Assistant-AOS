package com.example.gymbeacon.repository

import com.example.gymbeacon.ApiClient
import com.example.gymbeacon.model.GymInfo

class GymInfoRemoteDatasource(private val apiClient: ApiClient): GymInfoDataSource {

    override suspend fun getGyminfo(): List<GymInfo> {
        return apiClient.getGyminfo()
    }
}