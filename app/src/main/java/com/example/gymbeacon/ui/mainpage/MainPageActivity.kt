package com.example.gymbeacon.ui.mainpage

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gymbeacon.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainPageActivity : AppCompatActivity() {

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    val TAG = "BEACON"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        Log.e(TAG, "onCreate 실행")
    }

    override fun onStart() {
        super.onStart()
        checkBluetoothPermission()
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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