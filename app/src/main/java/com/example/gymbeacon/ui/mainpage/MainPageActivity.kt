package com.example.gymbeacon.ui.mainpage

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gymbeacon.R
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainPageActivity : AppCompatActivity() {

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    val TAG = "BEACON"
 // 장치 검색하기
    private var mScanning: Boolean = false
    private var arrayDevices = ArrayList<BluetoothDevice>()
    private val handler = HandlerThread("scanHandlerThread")

    private val scanCallback = object: ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                if (!arrayDevices.contains(it.device)){
                    arrayDevices.add(it.device)
                    Log.e(TAG,"arrayDevices : $arrayDevices")
                    Log.e(TAG,"device rssi : ${it.rssi} , device address : ${it.device.address}, device : ${it.device},${it.device.name}")
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let {
                for (result in it) {
                    if (!arrayDevices.contains(result.device)) arrayDevices.add(result.device)
                    Log.e(TAG,"arrayDevices : $arrayDevices")
                }
            }
        }
    }

    private val SCAN_PERIOD = 10000

    private fun scanLeDevice(enable: Boolean) {
        Log.e(TAG,"SCAN 시작")
        when (enable) {
            true -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    mScanning = false
                    Log.e(TAG, "SCAN Post delayed 실행")
                    bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)

                }, SCAN_PERIOD.toLong())

                mScanning = true
                arrayDevices.clear()
                bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
            }
            else -> {
                mScanning = false
                bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
            }
        }
    }

    private fun selectDevice(position: Int) {
        val device = arrayDevices.get(position)
        //val intent = Intent(this,DeviceControlActivity::class.java)
        //intent.putExtra("address",device.address)

        if (mScanning) scanLeDevice(false)
        Log.e(TAG, arrayDevices.toString())
        //startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        Log.e(TAG, "onCreate 실행")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume 실행")
        scanLeDevice(true)
    }


    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart 실행")
        requestPermission()
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            TedPermission.create()
                .setPermissionListener(object : PermissionListener {

                    //권한이 허용됐을 때
                    override fun onPermissionGranted() {
                        Log.e(TAG, "권한허용됨")
                        scanLeDevice(true)
                    }

                    //권한이 거부됐을 때
                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        Toast.makeText(this@MainPageActivity, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
                .setDeniedMessage("권한을 허용해주세요.")// 권한이 없을 때 띄워주는 Dialog Message
                .setPermissions(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )// 얻으려는 권한(여러개 가능)
                .check()
        } else {
            TedPermission.create()
                .setPermissionListener(object : PermissionListener {

                    //권한이 허용됐을 때
                    override fun onPermissionGranted() {
                        Log.e(TAG, "권한허용됨")
                        scanLeDevice(true)
                    }

                    //권한이 거부됐을 때
                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        Toast.makeText(this@MainPageActivity, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
                .setDeniedMessage("권한을 허용해주세요.")// 권한이 없을 때 띄워주는 Dialog Message
                .setPermissions(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )// 얻으려는 권한(여러개 가능)
                .check()
        }


    }


    private fun checkBluetoothPermission() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        // check android12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.e(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.S")
            if (!bluetoothAdapter.isEnabled) {
                showToast("블루투스가 꺼져있습니다.")
                //requestBluetooth.launch(enableBtIntent)
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            } else {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            }

        } else {
            requestBluetooth.launch(enableBtIntent)
        }


    }

    //    private var isBluetootDialogAlreadyShown = false
    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
                Log.e(TAG, "GRANTED")

            } else {
                Log.e(TAG, "DENIED")
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
        }
}