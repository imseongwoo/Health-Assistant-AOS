package com.example.data.model

data class WeightData(
    val recentDate: String? = "",
    var countDeadLift: Int = 0,
    var countLegExtenstion: Int = 0,
    var countSquat: Int = 0,
    var countLatPullDown: Int = 0,
    var countInclineBench: Int = 0,
    var countBench: Int = 0
)
