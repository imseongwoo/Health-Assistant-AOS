package com.example.gymbeacon.ui.home.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityCameraBinding
import com.example.gymbeacon.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
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

// Creates inputs for reference.
                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                inputFeature0.loadBuffer(tensorImage.buffer)

// Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                var canvas = Canvas(mutable)
                var h = bitmap.height
                var w = bitmap.width
                var x = 0

                while (x <= 49) {
                    if (outputFeature0.get(x + 2) > 0.45) {
                        canvas.drawCircle(outputFeature0.get(x + 1) * w, outputFeature0.get(x) * h, 10f, paint)

                    }
                    x += 3
                }
                val result = detectTest(outputFeature0)
                Log.e("result","${result}")
                binding.imageView.setImageBitmap(mutable)

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

    fun detectSquat(outputFeature0: FloatArray) : Boolean {
        // 허리(Hip)와 무릎(Knee) 사이의 거리를 구합니다.
        val hipToKneeDistance = calculateDistance(
            outputFeature0.get(27), outputFeature0.get(28),
            outputFeature0.get(15), outputFeature0.get(16)
        )

// 무릎(Knee)와 발목(Ankle) 사이의 거리를 구합니다.
        val kneeToAnkleDistance = calculateDistance(
            outputFeature0.get(15), outputFeature0.get(16),
            outputFeature0.get(9), outputFeature0.get(10)
        )

// 무릎(Knee)와 발목(Ankle) 사이의 각도를 계산합니다.
        val kneeAngle = calculateAngle(
            outputFeature0.get(15), outputFeature0.get(16),
            outputFeature0.get(9), outputFeature0.get(10),
            outputFeature0.get(11), outputFeature0.get(12)
        )

// 허리(Hip)와 무릎(Knee) 사이의 각도를 계산합니다.
        val hipAngle = calculateAngle(
            outputFeature0.get(27), outputFeature0.get(28),
            outputFeature0.get(15), outputFeature0.get(16),
            outputFeature0.get(11), outputFeature0.get(12)
        )

// 스쿼트 자세를 판단하는 기준 값입니다.
        val kneeAngleThreshold = 140f
        val hipToKneeDistanceThreshold = hipToKneeDistance * 0.6f
        val kneeToAnkleDistanceThreshold = kneeToAnkleDistance * 1.5f
        val hipAngleThreshold = 60f

// 스쿼트 자세를 판단합니다.
        val isSquatting = kneeAngle > kneeAngleThreshold &&
                hipToKneeDistance < hipToKneeDistanceThreshold &&
                kneeToAnkleDistance > kneeToAnkleDistanceThreshold &&
                hipAngle > hipAngleThreshold
        return isSquatting
    }

    fun detectTest(outputFeature0: FloatArray): Boolean {
        val leftShoulderToRightShoulderToRightElbow = calculateAngle(outputFeature0.get(15),outputFeature0.get(16),outputFeature0.get(18),outputFeature0.get(19),outputFeature0.get(24),outputFeature0.get(25))

        val testThreshold = 110f

        val isTest = leftShoulderToRightShoulderToRightElbow < testThreshold
        return isTest
        Log.e("testangle","$leftShoulderToRightShoulderToRightElbow")
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