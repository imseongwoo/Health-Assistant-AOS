package com.example.gymbeacon.ui.common

import android.speech.tts.TextToSpeech
import android.util.Log
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object PoseDetector {
    private var isLeft = false // 왼쪽 오른쪽 확인용 변수
    private var validSquat = false
    private var validLatPullDown = false
    private var minDeep = 0.0f
    private var upDownFlag = true
    private lateinit var tts: TextToSpeech
    private var messageFlag = true

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

    fun detectSquatByAngle(outputFeature0: FloatArray, tts: TextToSpeech): Boolean {
        val squatAngleLeft = calculateAngle(
            outputFeature0.get(34),
            outputFeature0.get(33),
            outputFeature0.get(40),
            outputFeature0.get(39),
            outputFeature0.get(46),
            outputFeature0.get(45)
        )
        val squatAngleRight = calculateAngle(
            outputFeature0.get(37),
            outputFeature0.get(36),
            outputFeature0.get(43),
            outputFeature0.get(42),
            outputFeature0.get(49),
            outputFeature0.get(48)
        )
        val squatLowThreshold = 10f
        val squatHighThreshold = 160f
        var nowDeep = 0.0f
        // 스쿼드 동작 인식
        val isLeftDetected = (squatAngleLeft > squatLowThreshold && squatAngleLeft < squatHighThreshold)
        val isRightDetected = (squatAngleRight > squatLowThreshold && squatAngleRight < squatHighThreshold)
        val isSquatDetected = isLeftDetected || isRightDetected
        // 스쿼트 동작이 감지되었을 때
        if(isSquatDetected) {
            // 왼쪽인지 오른쪽인지 판별
            if((outputFeature0.get(34) > outputFeature0.get(40)) || (outputFeature0.get(37) > outputFeature0.get(43))){
                isLeft = true // 엉덩이 x좌표가 무플 x좌표보다 크면 true 아니면 false
                nowDeep = outputFeature0.get(33)
            }else if((outputFeature0.get(34) < outputFeature0.get(40)) || (outputFeature0.get(37) < outputFeature0.get(43))){
                isLeft = false // 엉덩이 x좌표가 무플 x좌표보다 크면 true 아니면 false
                nowDeep = outputFeature0.get(36)
            }
            Log.e("isLeft", "isLeft=$isLeft")
            Log.e("nowDeep", "nowDeep=$nowDeep")
            // 가장 낯게 내려갔을때의 엉덩이 높이
            if(nowDeep > minDeep){
                if(!upDownFlag){
                    minDeep = nowDeep
                    Log.e("minDeep", "minDeep=$minDeep")
                }
            }
            Log.e("upDownFlag", "upDownFlag=$upDownFlag")

            // 임시 로그
            val fun_result_upper = squatStateGood_Upper(outputFeature0)
            Log.e("fun_result_upper", "fun_result_upper=$fun_result_upper")
            val fun_result_hip = squatStateGood_Hip(outputFeature0)
            Log.e("fun_result_hip", "fun_result_hip=$fun_result_hip")
            val fun_result_knee = squatStateGood_Knee(outputFeature0)
            Log.e("fun_result_knee", "fun_result_knee=$fun_result_knee")

            // 허리 너무 숙였을 때 피드백
            if(!squatStateGood_Upper(outputFeature0) && messageFlag){
                tts.speak("허리를 곧게 펴고 상체를 들어주세요", TextToSpeech.QUEUE_FLUSH, null, null)
                messageFlag = true
            }
            // 무릎 너무 나왔을 때 피드백
            if(!squatStateGood_Knee(outputFeature0) && !messageFlag){
                tts.speak("몸의 무게중심을 뒤로 당겨주세요", TextToSpeech.QUEUE_FLUSH, null, null)
                messageFlag = true
            }

            // 앉은 자세가 정확한지 확인
            if (squatStateGood_Upper(outputFeature0) && squatStateGood_Hip(outputFeature0) && squatStateGood_Knee(outputFeature0) && !upDownFlag) {
                validSquat = true
                Log.e("validSquat", "validSquat=$validSquat")
            } else{
                validSquat = false
            }
            // 일어서는 동작 감지
            if(nowDeep < minDeep){
                if(validSquat && !upDownFlag){
                    minDeep = 0.0f
                    upDownFlag = true
                    return true
                }else if(!validSquat && !upDownFlag){
                    // 덜 앉았을 때 피드백
                    if(!messageFlag){
                        if(isLeft){ // 왼쪽
                            // 엉덩이 높이가 무릎 높이보다 높을 때
                            if(outputFeature0.get(33) < outputFeature0.get(39)){
                                tts.speak("무릎과 엉덩이의 높이가 수평이 되도록 더 앉아주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                                messageFlag = true
                            }else{ // 엉덩이 높이가 무릎 높이보다 낮을 때
                                tts.speak("너무 많이 앉았습니다. 무릎과 엉덩이의 높이가 수평이 되게 해주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                                messageFlag = true
                            }
                        }else{ // 오른쪽
                            // 엉덩이 높이가 무릎 높이보다 높을 때
                            if(outputFeature0.get(36) < outputFeature0.get(42)){
                                tts.speak("무릎과 엉덩이의 높이가 수평이 되도록 더 앉아주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                                messageFlag = true
                            }else{ // 엉덩이 높이가 무릎 높이보다 낮을 때
                                tts.speak("너무 많이 앉았습니다. 무릎과 엉덩이의 높이가 수평이 되게 해주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                                messageFlag = true
                            }
                        }
                    }
                    minDeep = 0.0f
                    upDownFlag = true
                    return false
                }
            }
        }else{
            upDownFlag = false
            messageFlag = false
        }
        return false
    }

    // 스쿼트의 상체 자세가 바른지 확인하는 함수.
    fun squatStateGood_Upper(outputFeature0: FloatArray): Boolean {
        if(isLeft){ // 왼쪽
            return (calculateAngle(
                outputFeature0.get(16),
                outputFeature0.get(15),
                outputFeature0.get(34),
                outputFeature0.get(33),
                outputFeature0.get(16),
                outputFeature0.get(33)) > 45f)
        } else{ // 오른쪽
            return (calculateAngle(
                outputFeature0.get(19),
                outputFeature0.get(18),
                outputFeature0.get(37),
                outputFeature0.get(36),
                outputFeature0.get(19),
                outputFeature0.get(36)) > 45f)
        }
    }

    // 스쿼트의 하체 엉덩이 자세가 바른지 확인하는 함수.
    fun squatStateGood_Hip(outputFeature0: FloatArray): Boolean {
        if(isLeft){ // 왼쪽
            return (calculateAngle(
                outputFeature0.get(34),
                outputFeature0.get(33),
                outputFeature0.get(40),
                outputFeature0.get(39),
                outputFeature0.get(34),
                outputFeature0.get(39)) < 10f)
        } else{ // 오른쪽
            return (calculateAngle(
                outputFeature0.get(37),
                outputFeature0.get(36),
                outputFeature0.get(43),
                outputFeature0.get(42),
                outputFeature0.get(37),
                outputFeature0.get(42)) < 10f)
        }
    }

    // 스쿼트의 하체 무릎 자세가 바른지 확인하는 함수.
    fun squatStateGood_Knee(outputFeature0: FloatArray): Boolean {
        if(isLeft){ // 왼쪽
            return (calculateAngle(
                outputFeature0.get(40),
                outputFeature0.get(39),
                outputFeature0.get(46),
                outputFeature0.get(45),
                outputFeature0.get(40),
                outputFeature0.get(45)) > 50f)
        } else{ // 오른쪽
            return (calculateAngle(
                outputFeature0.get(43),
                outputFeature0.get(42),
                outputFeature0.get(49),
                outputFeature0.get(48),
                outputFeature0.get(43),
                outputFeature0.get(48)) > 50f)
        }
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
        val latPullDownLeftAngle = calculateAngle(
            outputFeature0.get(22),
            outputFeature0.get(21),
            outputFeature0.get(16),
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(33))

        val latPullDownRightAngle = calculateAngle(
            outputFeature0.get(25),
            outputFeature0.get(24),
            outputFeature0.get(19),
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(36))

        val latPullDownLowThreshold = 1f
        val latPullDownHighThreshold = 120f
        val nowDeep = (outputFeature0.get(21) + outputFeature0.get(24)) / 2
        // 랫풀다운 동작 감지
        val isLeftDetected = (latPullDownLeftAngle > latPullDownLowThreshold && latPullDownLeftAngle < latPullDownHighThreshold)
        val isRightDetected = (latPullDownRightAngle > latPullDownLowThreshold && latPullDownRightAngle < latPullDownHighThreshold)
        val isLatPullDownDetected = isLeftDetected || isRightDetected
        // 랫풀다운 동작이 감지되었을 때
        if(isLatPullDownDetected){
            if(nowDeep > minDeep){
                if(!upDownFlag){
                    minDeep = nowDeep
                }
            }
            // 팔 당기는 자세가 정확한지 확인
            if (isLatPullDownStateGood_Arm(outputFeature0) && isLatPullDownStateGood_Shoulder(outputFeature0) && isLatPullDownStateGood_Upper(outputFeature0) && !upDownFlag) {
                validLatPullDown = true
            }
            if(nowDeep < minDeep){
                if(validLatPullDown && !upDownFlag){
                    validLatPullDown = false
                    minDeep = 0.0f
                    upDownFlag = true
                    return true
                }
            }
        }else{
        upDownFlag = false
    }

        return false
    }

    // 랫풀다운에서 팔 기울기가 바른지 확인하는 함수.
    fun isLatPullDownStateGood_Arm(outputFeature0: FloatArray): Boolean {
        val gradientArm = calculateGradient(
            outputFeature0.get(22),
            outputFeature0.get(21),
            outputFeature0.get(25),
            outputFeature0.get(24))
        if(Math.abs(gradientArm) > 1){
            // 임시
            return false
        }
        // 임시
        return false
    }

    // 랫풀다운에서 어깨 각도가 바른지 확인하는 함수.
    fun isLatPullDownStateGood_Shoulder(outputFeature0: FloatArray): Boolean {
        val angleShoulderLeft = calculateAngle(
            outputFeature0.get(22),
            outputFeature0.get(21),
            outputFeature0.get(16),
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(33))
        val angleShoulderRight = calculateAngle(
            outputFeature0.get(25),
            outputFeature0.get(24),
            outputFeature0.get(19),
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(36))

        if(abs(angleShoulderLeft - angleShoulderRight) > 5f){
            if(angleShoulderLeft > angleShoulderRight){
                tts.speak(".", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            return false
        }else{
            return (angleShoulderLeft < 60f && angleShoulderRight < 60f)
        }
    }

    // 랫풀다운에서 상체 기울기가 바른지 확인하는 함수.
    fun isLatPullDownStateGood_Upper(outputFeature0: FloatArray): Boolean {
        return (calculateAngle(
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(40),
            outputFeature0.get(33)) > 150f)
    }

    // 기울기 계산 함수
    fun calculateGradient(x1: Float, y1: Float, x2: Float, y2: Float): Float{
        return (y2 - y1) / (x2 - x1)
    }

    // tts
    fun setTTS(ttsEngine: TextToSpeech){
        tts = ttsEngine
    }

    // 레그 익스텐션 자세 추정 함수
    fun detectLegExtension(outputFeature0: FloatArray) : Boolean {

        val legExLeftAngle = calculateAngle(        // 왼쪽 레그 익스텐션 각도
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(45),
            outputFeature0.get(46)
        )

        val legExRightAngle = calculateAngle(       // 오른쪽 레그 익스텐션 각도
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43),
            outputFeature0.get(48),
            outputFeature0.get(49)
            )


        // 레그 익스텐션의 왼쪽 기준 각도와 오른쪽 기준 각도로 판단
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

        return false
    }

    // 데드리프트 자세 추정 함수
    fun detectDeadLift(outputFeature0: FloatArray) : Boolean {

        val deadLiftLeftAngle = calculateAngle(     // 데드리프트 왼쪽 각도
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40)
        )

        val deadLiftRightAngle = calculateAngle(    // 데드리프트 오른쪽 각도
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43)
        )

        // 데드리프트의 왼쪽 기준 각도와 오른쪽 기준 각도로 판단
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

        return false
    }

    // 벤치프레스 자세 추정 함수
    fun detectBenchPress(outputFeature0: FloatArray) : Boolean {

        // 상체 각도
        val benchUpperLeftAngle = calculateAngle(       // 벤치프레스 수행시 상체의 왼쪽 각도
            outputFeature0.get(15),
            outputFeature0.get(16),
            outputFeature0.get(21),
            outputFeature0.get(22),
            outputFeature0.get(27),
            outputFeature0.get(28)
        )

        val benchUpperRightAngle = calculateAngle(      // 벤치프레스 수행시 상체의 오른쪽 각도
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(24),
            outputFeature0.get(25),
            outputFeature0.get(30),
            outputFeature0.get(31)
        )

        // 하체 각도
        val benchLowerLeftAngle = calculateAngle(       // 벤치프레스 수행시 하체의 왼쪽 각도
            outputFeature0.get(33),
            outputFeature0.get(34),
            outputFeature0.get(39),
            outputFeature0.get(40),
            outputFeature0.get(45),
            outputFeature0.get(46)
        )

        val benchLowerRightAngle = calculateAngle(      // 벤치프레스 수행시 하체의 오른쪽 각도
            outputFeature0.get(36),
            outputFeature0.get(37),
            outputFeature0.get(42),
            outputFeature0.get(43),
            outputFeature0.get(48),
            outputFeature0.get(49)
        )

        // 벤치 각도 (인클라인 벤치프레스와 구분하기 위함)
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

        // 벤치프레스 수행 시, 상체, 하체, 벤치 각도를 모두 고려하여 왼쪽/오른쪽 기준으로 자세 판단
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