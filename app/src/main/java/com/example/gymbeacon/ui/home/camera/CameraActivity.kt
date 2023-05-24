package com.example.gymbeacon.ui.home.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Range
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityCameraBinding
import com.example.gymbeacon.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.model.HealthEntity
import com.example.gymbeacon.ui.common.PoseDetector
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.*


class CameraActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var binding: ActivityCameraBinding
    lateinit var cameraManager: CameraManager
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var bitmap: Bitmap
    lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
    lateinit var imageProcessor: ImageProcessor
    lateinit var tts : TextToSpeech
    lateinit var maxNum : String
    lateinit var selectedExerciseName : String

    val paint = Paint()
    var count = 0
    private var previousTtsData: String = ""
    var database = Firebase.database
    val myRef = database.getReference("health/momentum")


    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
            tts = TextToSpeech(this,this)
        } else {
            val installIntent: Intent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.lifecycleOwner = this

        getIntentData()

        imageProcessor =
            ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.setColor(Color.YELLOW)

        // tts 초기화
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageStatus: Int = tts.setLanguage(Locale.KOREAN)

                if(languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                    languageStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this,"언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val data: String = count.toString()
                    var speechStatus: Int = 0

                    if (data != previousTtsData && data != "0") {
                        speechStatus = tts.speak(data,TextToSpeech.QUEUE_FLUSH,null,null)
                        if (speechStatus == TextToSpeech.ERROR) {
                            Toast.makeText(this, "음성전환 에러입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    previousTtsData = data
                }
            } else {
                Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                Log.e("test","onSurfaceTextureDestroyed 실행")
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = binding.textureView.bitmap!!
                var tensorImage = TensorImage(DataType.UINT8)
                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // model.process() 메소드 비동기 실행
                CoroutineScope(Dispatchers.Main).launch {
                    val inputFeature0 =
                        TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                    inputFeature0.loadBuffer(tensorImage.buffer)

                    val outputs = model.process(inputFeature0)
                    val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                    // 결과 값을 가져온 후 이미지 처리 및 그림 그리는 작업 수행
                    withContext(Dispatchers.Default) {
                        var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        var canvas = Canvas(mutable)
                        var h = bitmap.height
                        var w = bitmap.width
                        var x = 0

                        var circleDrawn = false
                        var circleCount = 0

                        while (x <= 49) {
                            if (outputFeature0.get(x + 2) > 0.45) {
                                canvas.drawCircle(outputFeature0.get(x + 1) * w, outputFeature0.get(x) * h, 10f, paint)
                                circleDrawn = true
                                circleCount += 1
                            } else {
                                Log.d("Circle not drawn", "outputFeature0[$x+2] = ${outputFeature0.get(x + 2)}")
                            }
                            x += 3
                        }

                        if (circleDrawn && circleCount >= 3) {
                            if (outputFeature0.get(35) > 0.3 && outputFeature0.get(38) > 0.3 && outputFeature0.get(41) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(47) > 0.3 && outputFeature0.get(50) > 0.3) {
                                val result = PoseDetector.detectSquatByAngle(outputFeature0)
                                val intent: Intent = Intent()
                                intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                                countSquat(result)
                                activityResult.launch(intent)
                                Log.e("result","${result},${count}")
                            }
                        }

                        withContext(Dispatchers.Main) {
                            binding.imageView.setImageBitmap(mutable)
                        }
                    }
                }
            }
        }
    }
 // 종료 시 count 된 값 서버에 전송
    override fun onDestroy() {
        super.onDestroy()
        var healthEntity = HealthEntity(CommonUtil.getUid(),CommonUtil.getTime(System.currentTimeMillis()),count.toString(),selectedExerciseName)
        Log.e("test","ondestroy 실행")
        myRef.push().setValue(healthEntity)
        tts?.let {
            it.stop()
            it.shutdown()
        }
        model.close()
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0],
            object : CameraDevice.StateCallback() {

                override fun onOpened(p0: CameraDevice) {
                    var captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                    // 최소 및 최대 프레임 속도 설정
                    captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(10, 10))

                    var surface = Surface(binding.textureView.surfaceTexture)
                    captureRequest.addTarget(surface)

                    p0.createCaptureSession(listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(p0: CameraCaptureSession) {
                                p0.setRepeatingRequest(captureRequest.build(), null, null)
                            }

                            override fun onConfigureFailed(p0: CameraCaptureSession) {
                            }

                        },
                        handler)
                }

                override fun onDisconnected(p0: CameraDevice) {
                }

                override fun onError(p0: CameraDevice, p1: Int) {
                }

            },
            handler)
    }



    fun countSquat(result: Boolean) {
        if (result) {
            count += 1
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageStatus: Int = tts.setLanguage(Locale.KOREAN)

            if(languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                languageStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this,"언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val data: String = count.toString()
                var speechStatus: Int = 0

                if (data != previousTtsData && data != "0") {
                    if (data.toInt() >= maxNum.toInt()) {
                        tts.speak("세트가 끝났습니다",TextToSpeech.QUEUE_FLUSH,null,null)
                        GlobalScope.launch {
                            delay(2000) // 2초 대기
                            finish() // 종료
                        }

                    } else {
                        speechStatus = tts.speak(data,TextToSpeech.QUEUE_FLUSH,null,null)
                        if (speechStatus == TextToSpeech.ERROR) {
                            Toast.makeText(this, "음성전환 에러입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                previousTtsData = data
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun getIntentData() {
        val cameraIntent = intent
        maxNum = cameraIntent.getStringExtra("maxnum")!!
        selectedExerciseName = cameraIntent.getStringExtra("selectedExerciseName")!!
        Log.e("maxnum","${maxNum}")
    }
}