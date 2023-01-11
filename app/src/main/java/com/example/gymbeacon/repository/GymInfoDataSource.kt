package com.example.gymbeacon.repository

import com.example.gymbeacon.model.GymInfo

interface GymInfoDataSource {

    suspend fun getGyminfo(): List<GymInfo>
}