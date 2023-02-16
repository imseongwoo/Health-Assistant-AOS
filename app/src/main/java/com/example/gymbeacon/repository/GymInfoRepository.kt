package com.example.gymbeacon.repository

import com.example.gymbeacon.model.GymInfo

class GymInfoRepository(
    private val remoteDatasource: GymInfoRemoteDatasource,
) {

    suspend fun getGyminfo(): List<GymInfo> {
        return remoteDatasource.getGyminfo()
    }
}