package com.example.gymbeacon.ui.common

import android.util.Log
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object PoseDetector {

    // 레그 익스텐션
    var status_legex = "stand"      // 상태 = state or legex
    var isLegEx = false

    // 데드리프트
    var status_dead = "stand"        // 상태 = state or dead
    var isDeadLift = false

    // 벤치프레스
    var status_bench = "initial"      // 상태 = initial or bench
    var isBenchPress = false

    // 인클라인 벤치프레스
    var status_incline = "initial"      // 상태 = initial or incline
    var isInclineBench = false

    fun detectSquatByAngle(outputFeature0: FloatArray): Boolean {
        val squatAngleLeft = calculateAngle(outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(45),
            outputFeature0.get(46))
        val squatAngleRight = calculateAngle(outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43),
            outputFeature0.get(48),
            outputFeature0.get(49))
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

    fun detectLatPullDown(outputFeature0: FloatArray): Boolean {
        Log.e("test", "detect lat pull down")
        val latPullDownLeftAngle = calculateAngle(outputFeature0.get(27),
            outputFeature0.get(28),
            outputFeature0.get(21),
            outputFeature0.get(22),
            outputFeature0.get(15),
            outputFeature0.get(16))

        val latPullDownRightAngle = calculateAngle(outputFeature0.get(30),
            outputFeature0.get(31),
            outputFeature0.get(24),
            outputFeature0.get(25),
            outputFeature0.get(18),
            outputFeature0.get(19))

        Log.e("test", "${latPullDownLeftAngle}, ${latPullDownRightAngle}")
        val latPullDownLowThreshold = 30f
        val latPullDownHighThreshold = 90f
        val isLeftLatPullDown =
            (latPullDownLeftAngle > latPullDownLowThreshold && latPullDownLeftAngle < latPullDownHighThreshold)
        val isRightLatPullDown =
            (latPullDownRightAngle > latPullDownLowThreshold && latPullDownRightAngle < latPullDownHighThreshold)
        val isLatPullDown = isLeftLatPullDown && isRightLatPullDown
        return isLatPullDown
    }

    // 레그 익스텐션 추정 함수
    fun detectLegExtension(outputFeature0: FloatArray) : Boolean {
        Log.e("test", "detect lat pull down")

        val legExLeftAngle = calculateAngle(
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(45),
            outputFeature0.get(46)
        )

        val legExRightAngle = calculateAngle(
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43),
            outputFeature0.get(48),
            outputFeature0.get(49)
            )

        val legExLowThreshold = 60f
        val legExHighThreshold = 125f

        if ( (legExLeftAngle < 180f && legExLeftAngle > 160f) || (legExRightAngle < 180f && legExRightAngle > 160f) ) {
            status_legex = "legex"
        }
        else if ((legExLeftAngle >= 80f && legExLeftAngle < 100f) || (legExRightAngle >= 80f && legExRightAngle < 100f) ) {
            if ( status_legex == "legex" ) {
                isLegEx = true
                status_legex = "stand"

                return isLegEx
            }

        }

//        val isLeftLegEx = legExLeftAngle > legExLowThreshold && legExLeftAngle < legExHighThreshold
//        val isRightLegEx = legExRightAngle > legExLowThreshold && legExRightAngle < legExHighThreshold
//
//        val isLegEx = isLeftLegEx || isRightLegEx
//
//        return isLegEx
        return false
    }

    fun detectDeadLift(outputFeature0: FloatArray) : Boolean {
        Log.e("test", "detect DeadLift")

        val deadLiftLeftAngle = calculateAngle(
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40)
        )

        val deadLiftRightAngle = calculateAngle(
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43)
        )
        Log.e("status : ", status_dead)
        Log.e("dead 각도 : " ,deadLiftLeftAngle.toString())
        Log.e("isDeadLift : ", isDeadLift.toString())

        if ( deadLiftLeftAngle < 80f || deadLiftRightAngle < 80f ) {
            status_dead = "dead"
        }
        else if (deadLiftLeftAngle >= 160f || deadLiftRightAngle >= 160f) {
            if ( status_dead == "dead" ) {
                isDeadLift = true
                status_dead = "stand"

                return isDeadLift
            }

        }

        Log.e("status : ", status_dead)
        Log.e("dead 각도 : " ,deadLiftLeftAngle.toString())
        Log.e("isDeadLift : ", isDeadLift.toString())


//        val deadLiftLowThreshold = 45f
//        val deadLiftHighThreshold = 180f
//
//        val isLeftDeadLift = deadLiftLeftAngle > deadLiftLowThreshold && deadLiftLeftAngle < deadLiftHighThreshold
//        val isRightDeadLift = deadLiftRightAngle > deadLiftLowThreshold && deadLiftRightAngle < deadLiftHighThreshold
//
//        val isDeadLift = isLeftDeadLift || isRightDeadLift
        //return isDeadLift

        return false
    }

    // 벤치프레스 자세 추정 함수
    fun detectBenchPress(outputFeature0: FloatArray) : Boolean {
        Log.e("test", "detect BenchPress")

        // 상체 각도
        val benchUpperLeftAngle = calculateAngle(
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(21),
            outputFeature0.get(22),
            outputFeature0.get(27),
            outputFeature0.get(28)
        )

        val benchUpperRightAngle = calculateAngle(
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(24),
            outputFeature0.get(25),
            outputFeature0.get(30),
            outputFeature0.get(31)
        )

        // 하체 각도
        val benchLowerLeftAngle = calculateAngle(
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(45),
            outputFeature0.get(46)
        )

        val benchLowerRightAngle = calculateAngle(
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43),
            outputFeature0.get(48),
            outputFeature0.get(49)
        )

        // 벤치 각도 (인클라인과 구분하기 위함)
        val benchLeftAngle = calculateAngle(        
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40)
        )
        val benchRightAngle = calculateAngle(
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43)
        )

        
        if ( (benchLowerLeftAngle > 80f && benchLowerLeftAngle < 100f) || (benchLowerRightAngle > 80f && benchLowerRightAngle < 100f) ) {

            if ( (benchLeftAngle > 175f && benchLeftAngle < 185f) || (benchRightAngle > 175f && benchRightAngle < 185f) ) {     // 벤치 각도

                if ((benchUpperLeftAngle >= 85f && benchUpperLeftAngle <= 100f) || (benchUpperRightAngle >= 85f && benchUpperRightAngle <= 100f)) {
                    status_bench = "bench"
                } else if ((benchUpperLeftAngle >= 165f && benchUpperLeftAngle <= 180f) || (benchUpperRightAngle >= 165f && benchUpperRightAngle <= 180f)) {
                    if (status_bench == "bench") {
                        isBenchPress = true
                        status_bench = "initial"

                        return isBenchPress
                    }
                }

            }
        }


        return false
    }

    // 인클라인 벤치프레스 자세 추정 함수
    fun detectInclineBenchPress(outputFeature0: FloatArray) : Boolean {
        Log.e("test", "detect BenchPress")

        // 상체 각도
        val benchUpperLeftAngle = calculateAngle(
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(21),
            outputFeature0.get(22),
            outputFeature0.get(27),
            outputFeature0.get(28)
        )

        val benchUpperRightAngle = calculateAngle(
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(24),
            outputFeature0.get(25),
            outputFeature0.get(30),
            outputFeature0.get(31)
        )

        // 하체 각도
        val benchLowerLeftAngle = calculateAngle(
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(45),
            outputFeature0.get(46)
        )

        val benchLowerRightAngle = calculateAngle(
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43),
            outputFeature0.get(48),
            outputFeature0.get(49)
        )

        // 벤치 각도 (인클라인과 그냥 벤치 구분하기 위함)
        val benchLeftAngle = calculateAngle(
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40)
        )
        val benchRightAngle = calculateAngle(
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43)
        )


        if ( (benchLowerLeftAngle > 80f && benchLowerLeftAngle < 100f) || (benchLowerRightAngle > 80f && benchLowerRightAngle < 100f) ) {

            if ( (benchLeftAngle > 125f && benchLeftAngle < 170f) || (benchRightAngle > 125f && benchRightAngle < 170f) ) {     // 벤치 각도
                if ((benchUpperLeftAngle >= 85f && benchUpperLeftAngle <= 100f) || (benchUpperRightAngle >= 85f && benchUpperRightAngle <= 100f)) {
                    status_incline = "incline"
                } else if ((benchUpperLeftAngle >= 165f && benchUpperLeftAngle <= 180f) || (benchUpperRightAngle >= 165f && benchUpperRightAngle <= 180f)) {
                    if (status_incline == "incline") {
                        isInclineBench = true
                        status_incline = "initial"

                        return isInclineBench
                    }
                }
            }
        }


        return false
    }

}