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
        val squatLowThreshold = 40f
        val squatHighThreshold = 140f
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
            if(!squatStateGood_Upper(outputFeature0)){
                tts.speak("허리를 곧게 펴고 상체를 들어주세요", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            // 무릎 너무 나왔을 때 피드백
            if(!squatStateGood_Knee(outputFeature0)){
                tts.speak("몸의 무게중심을 뒤로 당겨주세요", TextToSpeech.QUEUE_FLUSH, null, null)
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
                    if(isLeft){ // 왼쪽
                        // 엉덩이 높이가 무릎 높이보다 높을 때
                        if(outputFeature0.get(33) < outputFeature0.get(39)){
                            tts.speak("무릎과 엉덩이의 높이가 수평이 되도록 더 앉아주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                        }else{ // 엉덩이 높이가 무릎 높이보다 낮을 때
                            tts.speak("너무 많이 앉았습니다. 무릎과 엉덩이의 높이가 수평이 되게 해주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }else{ // 오른쪽
                        // 엉덩이 높이가 무릎 높이보다 높을 때
                        if(outputFeature0.get(36) < outputFeature0.get(42)){
                            tts.speak("무릎과 엉덩이의 높이가 수평이 되도록 더 앉아주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                        }else{ // 엉덩이 높이가 무릎 높이보다 낮을 때
                            tts.speak("너무 많이 앉았습니다. 무릎과 엉덩이의 높이가 수평이 되게 해주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                    minDeep = 0.0f
                    upDownFlag = true
                    return false
                }
            }
        }else{
            upDownFlag = false
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
                outputFeature0.get(39)) < 15f)
        } else{ // 오른쪽
            return (calculateAngle(
                outputFeature0.get(37),
                outputFeature0.get(36),
                outputFeature0.get(43),
                outputFeature0.get(42),
                outputFeature0.get(37),
                outputFeature0.get(42)) < 15f)
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
}