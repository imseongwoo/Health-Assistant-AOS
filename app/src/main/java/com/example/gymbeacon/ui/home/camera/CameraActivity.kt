package com.example.gymbeacon.ui.home.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.Surface
import android.view.TextureView
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityCameraBinding
import com.example.gymbeacon.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    lateinit var cameraManager: CameraManager
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var bitmap: Bitmap
    lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
    lateinit var imageProcessor: ImageProcessor
    val paint = Paint()
    var isSqaut = false
    var count = 0
    var temp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.lifecycleOwner = this

        imageProcessor =
            ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.setColor(Color.YELLOW)

        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
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
                            Log.d("Circle not drawn", "outputFeature0[$x+2] = ${outputFeature0.get(x + 2)}")
                            x += 3
                        }

                        if (circleDrawn && circleCount >= 3) {
                            if (outputFeature0.get(35) > 0.3 && outputFeature0.get(38) > 0.3 && outputFeature0.get(41) > 0.3 && outputFeature0.get(44) > 0.3 && outputFeature0.get(47) > 0.3 && outputFeature0.get(50) > 0.3) {
                                val result = detectSquatByAngle(outputFeature0)
                                countSquat(result)
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

    override fun onDestroy() {
        super.onDestroy()
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

    fun detectSquatByAngle(outputFeature0: FloatArray): Boolean {
        val squatAngleLeft = calculateAngle(outputFeature0.get(33),outputFeature0.get(34),outputFeature0.get(39),outputFeature0.get(40),outputFeature0.get(45),outputFeature0.get(46))
        val squatAngleRight = calculateAngle(outputFeature0.get(36),outputFeature0.get(37),outputFeature0.get(42),outputFeature0.get(43),outputFeature0.get(48),outputFeature0.get(49))
        val squatLowThreshold = 60f
        val squatHighThreshold = 125f

        val isLeft = (squatAngleLeft > squatLowThreshold && squatAngleLeft < squatHighThreshold)
        val isRight = (squatAngleLeft > squatLowThreshold && squatAngleLeft < squatHighThreshold)
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

    fun countSquat(result: Boolean) {
        if (result == true && temp == false) {
            count += 1
        }
        temp = result
    }
}