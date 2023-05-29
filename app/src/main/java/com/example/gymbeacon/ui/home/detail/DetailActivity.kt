package com.example.gymbeacon.ui.home.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Range
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityDetailBinding
import com.example.gymbeacon.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
import com.example.gymbeacon.model.BodyPart
import com.example.gymbeacon.model.HealthEntity
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.ui.common.PoseDetector
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityDetailBinding
    lateinit var cameraManager: CameraManager
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var bitmap: Bitmap
    lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
    lateinit var imageProcessor: ImageProcessor
    lateinit var tts: TextToSpeech

    // 안내창 다이어로그
    private var infoDialog: InfoDialogActivity? = null
    //private var dialog_gif: ImageView? = null

    //    lateinit var maxNum : String
    private lateinit var selectedExerciseName: String

    var maxNum: String = "999"
    val paint = Paint()
    var count = 0
    private var previousTtsData: String = ""
    var database = Firebase.database
    val myRef = database.getReference("health/momentum")

    // 카메라 전면, 후면 변환 여부 (05/22 추가)
    private var isFrontCamera = false // 전면 카메라 여부를 나타내는 변수

    // 녹화 관련 (04-05 추가)
    private lateinit var mMediaRecorder: MediaRecorder
    private var mNextVideoAbsolutePath: String? = null
    private var isRecording : Boolean = false     // 녹화 토글 버튼 확인용

    // 원하는 폴더이름 생성
    private val DETAIL_PATH = "DCIM/Koreatech/"
    private var cameraDevice: CameraDevice? = null
    var mCameraCaptureSession: CameraCaptureSession? = null
    var mCaptureRequestBuilder: CaptureRequest.Builder? = null

    private val requirePermissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val bodyJoints = listOf(
        Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            tts = TextToSpeech(this, this)
        } else {
            val installIntent: Intent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.lifecycleOwner = this


        val detailIntent = intent
        selectedExerciseName = detailIntent.getStringExtra("upper")!!

        isRecording = binding.recordToggle.isChecked

        // 운동별로 안내창 다이어로그 띄어주기

        if (selectedExerciseName == "벤치프레스") {
            infoDialog = InfoDialogActivity(this, selectedExerciseName,
                getString(R.string.dialog_common_text) +
                        getString(R.string.dialog_bench_text))
        }
        else if (selectedExerciseName == "랫 풀 다운") {
            infoDialog = InfoDialogActivity(this, selectedExerciseName,
                getString(R.string.dialog_common_text) +
                        getString(R.string.dialog_latpulldown_text))
        }
        else if (selectedExerciseName == "인클라인 벤치프레스") {
            infoDialog = InfoDialogActivity(this, selectedExerciseName,
                getString(R.string.dialog_common_text) +
                        getString(R.string.dialog_incline_text))
        }
        else if (selectedExerciseName == "스쿼트") {
            infoDialog = InfoDialogActivity(this, selectedExerciseName,
                getString(R.string.dialog_common_text) +
                        getString(R.string.dialog_squat_text))
        }
        else if (selectedExerciseName == "데드리프트") {
            infoDialog = InfoDialogActivity(this, selectedExerciseName,
                getString(R.string.dialog_common_text) +
                        getString(R.string.dialog_dead_text))
        }
        else if (selectedExerciseName == "레그 익스텐션") {
            infoDialog = InfoDialogActivity(this, selectedExerciseName,
                getString(R.string.dialog_common_text) +
                        getString(R.string.dialog_legex_text))
        }
        infoDialog!!.show()

        // 오디오 권한 요청
        requestPermissions(requirePermissions, REQUEST_RECORD_AUDIO_PERMISSION)
        //

        with(binding) {
            textViewExerciseName.text = selectedExerciseName
        }
        //setupMediaRecorder()
        initEvent()

        imageProcessor =
            ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.setColor(Color.YELLOW)
        paint.strokeWidth = 5f      // 선 두께 설정

        initTTS()


        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                Log.e("test", "onSurfaceTextureDestroyed 실행")
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

                        // 원 그리기
                        while (x <= 49) {
                            if (outputFeature0.get(x + 2) > 0.35) {
                                val circleX = outputFeature0.get(x + 1) * w
                                val circleY = outputFeature0.get(x) * h
                                canvas.drawCircle(circleX, circleY, 10f, paint)
                                circleDrawn = true
                                circleCount += 1
                            } else {
                                Log.d("Circle not drawn",
                                    "outputFeature0[$x+2] = ${outputFeature0.get(x + 2)}")
                            }
                            x += 3
                        }

                        // 각 원의 x좌표 y좌표를 맵에 저장
                        val circleCoordinates = mutableMapOf<Int, Pair<Float, Float>>()
                        for (i in 0 until 51 step 3) {
                            if (outputFeature0[i + 2] > 0.45) {
                                circleCoordinates[i] =
                                    Pair(outputFeature0[i + 1] * w, outputFeature0[i] * h)
                            }
                        }

                        // 원을 연결하는 선 그리기
                        for (joint in bodyJoints) {
                            val start = joint.first.ordinal * 3
                            val end = joint.second.ordinal * 3

                            val startCoordinates = circleCoordinates[start]
                            val endCoordinates = circleCoordinates[end]

                            if (startCoordinates != null && endCoordinates != null) {
                                canvas.drawLine(
                                    startCoordinates.first, startCoordinates.second,
                                    endCoordinates.first, endCoordinates.second,
                                    paint
                                )
                            }
                        }

                        // 원이 그려지고 그려진 원의 수가 3개 이상일 때 스쿼트 탐지 함수 실행
                        if (circleDrawn && circleCount >= 3) {
                            if (selectedExerciseName == "스쿼트") {
                                if (outputFeature0.get(35) > 0.3 && outputFeature0.get(38) > 0.3 && outputFeature0.get(
                                        41) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(
                                        47) > 0.3 && outputFeature0.get(50) > 0.3
                                ) {
                                    var result = PoseDetector.detectSquatByAngle(outputFeature0, tts)

                                    val intent: Intent = Intent()
                                    intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

                                    countExercise(result)
                                    activityResult.launch(intent)
                                    Log.e("result", "${result},${count}")
                                }
                            } else if (selectedExerciseName == "랫 풀 다운") {
                                if (outputFeature0.get(35) > 0.3 && outputFeature0.get(38) > 0.3 && outputFeature0.get(
                                        41) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(
                                        47) > 0.3 && outputFeature0.get(50) > 0.3
                                ) {
                                    var result = PoseDetector.detectLatPullDown(outputFeature0)

                                    val intent: Intent = Intent()
                                    intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

                                    countExercise(result)
                                    activityResult.launch(intent)
                                    Log.e("result", "${result},${count}")
                                }
                            }
                            else if (selectedExerciseName == "레그 익스텐션") {
                                if (outputFeature0.get(35) > 0.3 && outputFeature0.get(38) > 0.3 && outputFeature0.get(
                                        41) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(
                                        47) > 0.3 && outputFeature0.get(50) > 0.3
                                ) {
                                    var result = PoseDetector.detectLegExtension(outputFeature0)

                                    val intent: Intent = Intent()
                                    intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

                                    countExercise(result)
                                    activityResult.launch(intent)
                                    Log.e("result", "${result},${count}")
                                }
                            }
                            else if (selectedExerciseName == "데드리프트") {
                                if (outputFeature0.get(17) > 0.3 && outputFeature0.get(20) > 0.3 && outputFeature0.get(
                                        35) > 0.3 && outputFeature0.get(38) > 0.3 && outputFeature0.get(
                                        41) > 0.3 && outputFeature0.get(44) > 0.3
                                ) {
                                    var result = PoseDetector.detectDeadLift(outputFeature0)

                                    val intent: Intent = Intent()
                                    intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

                                    countExercise(result)
                                    activityResult.launch(intent)
                                    Log.e("result", "${result},${count}")
                                }
                            }
                            else if (selectedExerciseName == "벤치프레스") {
                                if ( outputFeature0.get(17) > 0.3 && outputFeature0.get(23) > 0.3 && outputFeature0.get(29) > 0.3 &&
                                    outputFeature0.get(20) > 0.3 && outputFeature0.get(26) > 0.3 && outputFeature0.get(32) > 0.3 &&
                                    outputFeature0.get(35) > 0.3 && outputFeature0.get(41) > 0.3 && outputFeature0.get(47) > 0.3 &&
                                    outputFeature0.get(38) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(50) > 0.3
                                ) {
                                    var result = PoseDetector.detectBenchPress(outputFeature0, tts)

                                    val intent: Intent = Intent()
                                    intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

                                    countExercise(result)
                                    activityResult.launch(intent)
                                    Log.e("result", "${result},${count}")
                                }
                            }
                            else if (selectedExerciseName == "인클라인 벤치프레스") {
                                if ( outputFeature0.get(17) > 0.3 && outputFeature0.get(23) > 0.3 && outputFeature0.get(29) > 0.3 &&
                                    outputFeature0.get(20) > 0.3 && outputFeature0.get(26) > 0.3 && outputFeature0.get(32) > 0.3 &&
                                    outputFeature0.get(35) > 0.3 && outputFeature0.get(41) > 0.3 && outputFeature0.get(47) > 0.3 &&
                                    outputFeature0.get(38) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(50) > 0.3
                                ) {
                                    var result = PoseDetector.detectInclineBenchPress(outputFeature0, tts)

                                    val intent: Intent = Intent()
                                    intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

                                    countExercise(result)
                                    activityResult.launch(intent)
                                    Log.e("result", "${result},${count}")
                                }
                            }

                        }

                        withContext(Dispatchers.Main) {
                            binding.imageView.setImageBitmap(mutable)
                        }
                    }
                }
            }
        }

//        // 04-05 추가
//        binding.buttonDetailRecord.setOnClickListener {
//            startRecording()
//        }
//        binding.buttonStopRecording.setOnClickListener {
//            stopRecordingVideo()
//        }

        // 녹화 버튼의 클릭 리스너 설정
        binding.recordToggle.setOnClickListener {
            isRecording = !isRecording
            print(binding.recordToggle.isChecked.toString())
//            // 토글 버튼의 상태에 따라
//            if (isRecording == true) {
//                binding.recordToggle.setTextColor(Color.WHITE)
//            }
        }

    }

    private fun initTTS() {
        // tts 초기화
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageStatus: Int = tts.setLanguage(Locale.KOREAN)

                if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                    languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Toast.makeText(this, "언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val data: String = count.toString()
                    var speechStatus: Int = 0

                    if (data != previousTtsData && data != "0") {
                        speechStatus = tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, null)
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
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initEvent() {
        with(binding) {
            minusButton.setOnClickListener {
                val currentCount = textViewDetailPageCount.text.toString()
                val nowCount = currentCount.toInt() - 1
                textViewDetailPageCount.text = nowCount.toString()
            }

            plusButton.setOnClickListener {
                val currentCount = textViewDetailPageCount.text.toString()
                val nowCount = currentCount.toInt() + 1
                textViewDetailPageCount.text = nowCount.toString()
            }

            imageViewDetailStart.setOnClickListener {
                //3. 운동 시작 버튼 눌렀을 때 갯수 카운트 초기화
                count = 0
                maxNum = textViewDetailPageCount.text.toString()

                if (binding.recordToggle.isChecked == true) {
                    startRecording()
                    Log.d("녹화 시작함 ??", binding.recordToggle.isChecked.toString())
                    print(binding.recordToggle.isChecked.toString())
                }
            }

//            buttonDetailRecord.setOnClickListener {
//                startRecording()
//            }
//
//            buttonStopRecording.setOnClickListener {
//                stopRecordingVideo()
//            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageStatus: Int = tts.setLanguage(Locale.KOREAN)

            if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                //Toast.makeText(this, "언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val data: String = count.toString()
                var speechStatus: Int = 0

                if (data != previousTtsData && data != "0") {
                    if (data.toInt() >= maxNum.toInt()) {
                        // 2. 설정한 카운트 개수 말하고 세트 종료 메세지 출력하기
                        //tts.speak(maxNum, TextToSpeech.QUEUE_FLUSH, null, null)
                        count = maxNum.toInt()

                        tts.speak(maxNum, TextToSpeech.QUEUE_FLUSH, null, null)
                        Thread.sleep(1000)
                        tts.speak("세트가 끝났습니다.", TextToSpeech.QUEUE_FLUSH, null, null)
                        //isRecording = false     // 녹화 종료

                        if (binding.recordToggle.isChecked == true) {
                            stopRecordingVideo()
                        }

                        Thread.sleep(2000) // 2초 대기
                        initCount()

//                        GlobalScope.launch {
//                            delay(2000) // 2초 대기
////                            finish() // 종료
//                            initCount()
//
//                        }

                    } else {
                        speechStatus = tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, null)
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

    fun initCount() {
        val healthEntity = HealthEntity(CommonUtil.getUid(),
            CommonUtil.getTime(System.currentTimeMillis()), count.toString(), selectedExerciseName)
        myRef.push().setValue(healthEntity)
        count = 0
    }

    // 종료 시 count 된 값 서버에 전송
    override fun onDestroy() {
        super.onDestroy()
        tts?.let {
            it.stop()
            it.shutdown()
        }
//        model.close()
    }

    // 04-05 추가
    // 권한 요청 결과 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 다음 두 가지 조건을 만족해야 오디오 권한 요청을 사용자가 허가했다고 판단한다.
        // 1. 요청 코드가 requestPermissions()의 파라미터로 전달한 요청코드인 경우
        // 2. 허가한 요청 중 첫 번째 권한을 승인한 경우 (권한이 많으면 grantResult안의 권한은 반복해서 확인해야 함)
        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0],
            object : CameraDevice.StateCallback() {

                @RequiresApi(Build.VERSION_CODES.P)
                override fun onOpened(p0: CameraDevice) {
                    cameraDevice = p0
                    startPreview(p0)
                }

                override fun onDisconnected(p0: CameraDevice) {
                }

                override fun onError(p0: CameraDevice, p1: Int) {
                }

            },
            handler)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startPreview(cameraDevice: CameraDevice) {
        val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        // 최소 및 최대 프레임 속도 설정
        captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(10, 10))

        val surface = Surface(binding.textureView.surfaceTexture)
        captureRequest.addTarget(surface)

        // 카메라 미리보기를 위한 CaptureSession 생성
        cameraDevice.createCaptureSession(listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(p0: CameraCaptureSession) {
                    p0.setRepeatingRequest(captureRequest.build(), null, null)
                }

                override fun onConfigureFailed(p0: CameraCaptureSession) {
                }

            },
            handler)
    }

    //영상녹화 설정 (04-05 추가)
    @Throws(IOException::class)
    private fun setUpMediaRecorder() {

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath!!.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(binding.textViewExerciseName.text.toString())
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath)
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024)
        mMediaRecorder.setVideoFrameRate(30)
        mMediaRecorder.setVideoSize(1920, 1080)

        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mMediaRecorder.setOrientationHint(90);    // 녹화 영상 출력 방향

        mMediaRecorder.prepare()
        mMediaRecorder.start()
    }

    //파일 이름 및 저장경로를 생성, 04-05 추가
    private fun getVideoFilePath(exerciseName: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        val dir = Environment.getExternalStorageDirectory().absoluteFile
        val path = dir.path + "/" + DETAIL_PATH
        val dst = File(path)
        if (!dst.exists()) dst.mkdirs()

        // 8. 캘린더페이지 녹화 영상 이름에 운동이름 + 운동 횟수 : 우선도 낮음
//        if (count == 0) {
//            return path + timeStamp + "_" + exerciseName + " -회" + ".mp4"
//        }

        //return path + timeStamp + "_" + exerciseName + " -회" + ".mp4"
        return path + timeStamp + "_" + exerciseName + " " + maxNum + "회" + ".mp4"

    }

    // 녹화 시작 04-05 추가
    @RequiresApi(Build.VERSION_CODES.P)
    fun startRecording() {

        mMediaRecorder = MediaRecorder()        // 미디어레코더 객체 생성

        try {
            closePreviewSession()
            setUpMediaRecorder()                // 미디어레코더를 통한 비디오, 오디오 출력 형식, 스트림 등 설정
            val texture: SurfaceTexture = binding.textureView.getSurfaceTexture()!!     // textureView의 SurfaceTexture 가져옴
            val captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD) // 카메라 장치에서 비디오 녹화를 위한 요청 생성

            mCaptureRequestBuilder = captureRequest         // 요청을 빌드하기 위해 캡처 요청 빌더 설정
            val surfaces: ArrayList<Surface> = ArrayList()  // Surface 목록을 가지는 ArrayList 객체 생성
            val previewSurface = Surface(texture)       // SurfaceTexture를 가지고 미리보기 Surface 생성
            surfaces.add(previewSurface)        // 미리보기 Surface 목록에 추가
            mCaptureRequestBuilder!!.addTarget(previewSurface)  // 빌드를 위해 미리보기 Surface를 캡처 요청 빌더에 추가
            val recordSurface = mMediaRecorder!!.surface      // 미디어레코더의 Surface를 가져옴
            surfaces.add(recordSurface)     // 녹화된 화면을 저장할 Surface 목록에 추가
            mCaptureRequestBuilder!!.addTarget(recordSurface)   // 빌드를 위해 MediaRecorder Surface를 캡처 요청 빌더에 추가

//            binding.buttonDetailRecord.setText("녹화 중..")

            cameraDevice!!.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    mCameraCaptureSession = session     // 생성된 세션을 클래스 변수에 저장
                    mCameraCaptureSession!!.setRepeatingRequest(mCaptureRequestBuilder!!.build(), null, null)  // 세션에서 지속적으로 비디오 프레임을 캡처하도록 반복 요청 설정
                    Toast.makeText(this@DetailActivity, "녹화를 시작합니다.", Toast.LENGTH_SHORT).show()   // 녹화 시작 버튼을 누르면 "녹화 중"이라는 메세지 출력
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                }
            },
                handler)
            //timer()
        } catch (e: CameraAccessException) {    // 카메라 접근 권한 예외 처리
            e.printStackTrace()
        } catch (e: IOException) {      // 파일 입출력 예외 처리
            e.printStackTrace()
        }
    }

    //녹화 중지 04-05 추가
    private fun stopRecordingVideo() {
//        binding.buttonDetailRecord.setText("녹화 시작")

        Toast.makeText(this, "녹화가 종료되었습니다.", Toast.LENGTH_SHORT).show()                 // 녹화 종료 메세지
        Toast.makeText(this, "Video saved: $mNextVideoAbsolutePath", Toast.LENGTH_SHORT).show()     // 저장 메세지 출력
        mMediaRecorder?.stop()               // 미디어레코더 녹음 중지
        mMediaRecorder?.reset()
//        mMediaRecorder?.release()
//        mMediaRecorder = null

        mNextVideoAbsolutePath = null
        cameraDevice?.close()
        cameraDevice = null

        //녹화 종료 후 카메라 미리보기 재개
        openCamera()

//        if (mMediaRecorder != null) {
//            try {
//                mMediaRecorder!!.stop()
//                mMediaRecorder!!.reset()
//                mMediaRecorder!!.release()
//            } catch (e: IllegalStateException) {
//                // 예외 처리
//            } finally {
//                mMediaRecorder = null
//            }
//        }
//
//        Toast.makeText(this, "녹화가 종료되었습니다.", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, "Video saved: $mNextVideoAbsolutePath", Toast.LENGTH_SHORT).show()
//
//        mNextVideoAbsolutePath = null
//        cameraDevice?.close()
//        cameraDevice = null
//
//        // 녹화 종료 후 카메라 미리보기 재개
//        openCamera()
    }

    // 04-05 추가
    private fun closePreviewSession() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession!!.close()
            mCameraCaptureSession = null
        }
    }

    fun countExercise(result: Boolean) {
        if (result) {
            count += 1
        }
    }


    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }

}