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
        val squatHighThreshold = 150f
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

            // 가장 낯게 내려갔을때의 엉덩이 높이
            if(nowDeep > minDeep){
                if(!upDownFlag){
                    minDeep = nowDeep
                }
            }

            // 허리 너무 숙였을 때 피드백
            if(!squatStateGood_Upper(outputFeature0) && !messageFlag){
                tts.speak("허리를 곧게 펴고 상체를 들어주세요", TextToSpeech.QUEUE_FLUSH, null, null)
                messageFlag = true
            }
            // 무릎 너무 나왔을 때 피드백
            if(!squatStateGood_Knee(outputFeature0) && !messageFlag){
                tts.speak("몸의 무게중심을 뒤로 당겨주세요", TextToSpeech.QUEUE_FLUSH, null, null)
                messageFlag = true
            }

            // 앉은 자세가 정확한지 확인
            validSquat = squatStateGood_Upper(outputFeature0) && squatStateGood_Hip(outputFeature0) && squatStateGood_Knee(outputFeature0) && !upDownFlag

            // 일어서는 동작 감지
            if(nowDeep < minDeep){
                if(validSquat && !upDownFlag){
                    minDeep = 0.0f
                    upDownFlag = true
                    return true
                }else if(!validSquat && !upDownFlag){
                    // 덜 앉았을 때 피드백
                    if(!messageFlag && !squatStateGood_Hip(outputFeature0)){
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
                        messageFlag = true
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
        val isLatPullDownDetected = isLeftDetected && isRightDetected

        // 랫풀다운 동작이 감지되었을 때
        if(isLatPullDownDetected){
            if(nowDeep > minDeep){
                if(!upDownFlag){
                    minDeep = nowDeep
                }
            }

            // 상체 기울어지면 피드백
            if(!isLatPullDownStateGood_Upper(outputFeature0) && !messageFlag){
                if(latPullDownLeftAngle > latPullDownRightAngle){
                    tts.speak("오른팔에 힘이 더 들어갔습니다.", TextToSpeech.QUEUE_FLUSH, null, null)
                    messageFlag = true
                } else{
                    tts.speak("왼팔에 힘이 더 들어갔습니다.", TextToSpeech.QUEUE_FLUSH, null, null)
                    messageFlag = true
                }
            }

            // 어깨 치우쳐져있으면 피드백
            if(!isLatPullDownStateGood_Arm(outputFeature0) && !messageFlag){
                if(outputFeature0.get(15) > outputFeature0.get(18)){
                    tts.speak("상체가 왼쪽으로 기울었습니다.", TextToSpeech.QUEUE_FLUSH, null, null)
                    messageFlag = true
                } else{
                    tts.speak("상체가 오른쪽으로 기울었습니다.", TextToSpeech.QUEUE_FLUSH, null, null)
                    messageFlag = true
                }
            }

            // 팔 당기는 자세가 정확한지 확인
            validLatPullDown = isLatPullDownStateGood_Shoulder(outputFeature0) && isLatPullDownStateGood_Arm(outputFeature0)
                    && isLatPullDownStateGood_Upper(outputFeature0) && !upDownFlag

            // 팔 올리는 동작 감지
            if(nowDeep < minDeep){
                if(validLatPullDown && !upDownFlag){
                    minDeep = 0.0f
                    upDownFlag = true
                    return true
                }else if(!validLatPullDown && !upDownFlag){
                    // 팔 덜 내렸을 때 피드백
                    if(!messageFlag && !isLatPullDownStateGood_Shoulder(outputFeature0)){
                        tts.speak("등에 자극이 올 때까지 팔을 더 당겨주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                        messageFlag = true
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

    // 랫풀다운에서 어깨 각도가 임계값에 도달했는지 확인하는 함수.
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

        return (angleShoulderLeft < 30f && angleShoulderRight < 30f)
    }

    // 랫풀다운에서 어깨 각도가 바른지 확인하는 함수.
    fun isLatPullDownStateGood_Arm(outputFeature0: FloatArray): Boolean {
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

        return (abs(angleShoulderLeft - angleShoulderRight) < 5f)
    }

    // 랫풀다운에서 상체 기울기가 바른지 확인하는 함수. - 상체가 한쪽으로 쏠리는지 확인
    fun isLatPullDownStateGood_Upper(outputFeature0: FloatArray): Boolean {
        return (calculateAngle(
            outputFeature0.get(16),
            outputFeature0.get(15),
            outputFeature0.get(19),
            outputFeature0.get(18),
            outputFeature0.get(19),
            outputFeature0.get(15)) < 10f)
    }

    // tts
    fun setTTS(ttsEngine: TextToSpeech){
        tts = ttsEngine
    }
}