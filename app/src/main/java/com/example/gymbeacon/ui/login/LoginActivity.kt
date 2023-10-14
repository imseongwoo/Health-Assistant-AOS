package com.example.gymbeacon.ui.login

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityLoginBinding
import com.example.gymbeacon.ui.home.HomeActivity
import com.example.gymbeacon.ui.signup.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    val viewModel: LoginViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.vm = viewModel
        auth = FirebaseAuth.getInstance()

        getPermissions()
        subscribe()

        with(binding) {
            buttonLogin.setOnClickListener {
                viewModel.login()
            }
            textViewSignUp.setOnClickListener {
                goSignUpActivity()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermissions() {
        val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(permissions, 101)
            }
        }


        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) getPermissions()
    }

    fun goHomeActivity() {
        Intent(this, HomeActivity::class.java).also { startActivity(it) }
    }

    fun goSignUpActivity() {
        Intent(this, SignUpActivity::class.java).also { startActivity(it) }
    }

    fun subscribe() {
        with(viewModel) {

            loginState.observe(this@LoginActivity) {
                if (it != null) {
                    if (it.isSuccess) {
                        goHomeActivity()
                    } else {
                        if (it.errorMessage.isNotEmpty()) {
                            showToast(it.errorMessage)
                        }
                    }
                }
            }
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

/*
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(binding.editTextId.text.toString(),
            binding.editTextPassword.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login, 아이디와 패스워드가 맞았을 때
                    goHomeActivity(task.result?.user)
                } else {
                    // Show the error message, 아이디와 패스워드가 틀렸을 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }*/
}