package com.example.domain.model

data class NaviHomeEntity(
    val countsLower: Int = 0,
    val countsUpper: Int = 0,
    val countsBack: Int = 0,
    val entityArrayList: ArrayList<HealthEntity>,
    val isSuccess: Boolean
)
