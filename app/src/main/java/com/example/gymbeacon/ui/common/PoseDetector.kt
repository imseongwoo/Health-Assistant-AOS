package com.example.gymbeacon.ui.common

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object PoseDetector {
    fun detectSquatByAngle(outputFeature0: FloatArray): Boolean {
        val squatAngleLeft = calculateAngle(outputFeature0.get(33),outputFeature0.get(34),outputFeature0.get(39),outputFeature0.get(40),outputFeature0.get(45),outputFeature0.get(46))
        val squatAngleRight = calculateAngle(outputFeature0.get(36),outputFeature0.get(37),outputFeature0.get(42),outputFeature0.get(43),outputFeature0.get(48),outputFeature0.get(49))
        val squatLowThreshold = 60f
        val squatHighThreshold = 125f

        val isLeft = (squatAngleLeft > squatLowThreshold && squatAngleLeft < squatHighThreshold)
        val isRight = (squatAngleRight > squatLowThreshold && squatAngleRight < squatHighThreshold)
        val isSquat = isLeft || isRight
        return isSquat
    }
    // 거리를 계산하는 함수입니다.
    fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    // 각도를 계산하는 함수입니다.
    fun calculateAngle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
        val a = calculateDistance(x1, y1, x2, y2)
        val b = calculateDistance(x2, y2, x3, y3)
        val c = calculateDistance(x3, y3, x1, y1)
        return acos((a.pow(2) + b.pow(2) - c.pow(2)) / (2 * a * b)) * 180 / PI.toFloat()
    }
}