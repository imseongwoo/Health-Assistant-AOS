package com.example.gymbeacon.ui.login

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityLoginBinding
import com.example.gymbeacon.tts.TextToSpeechActivity
import com.example.gymbeacon.ui.home.HomeActivity
import com.example.gymbeacon.ui.signup.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var auth : FirebaseAuth? = null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        getPermissions()

        with(binding) {
            buttonLogin.setOnClickListener {
                if (binding.editTextId.text.isNullOrEmpty() || binding.editTextPassword.text.isNullOrEmpty()) {
                    Toast.makeText(this@LoginActivity,"정보를 입력해주세요",Toast.LENGTH_SHORT).show()
                } else {
                    signinEmail()
                }
            }
            textViewSignUp.setOnClickListener {
                goSignUpActivity()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),101)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) getPermissions()
    }

    fun goHomeActivity(user: FirebaseUser?) {
        if (user != null) {
            Intent(this,HomeActivity::class.java).also { startActivity(it) }
        }
    }
    fun goSignUpActivity() {
        Intent(this, SignUpActivity::class.java).also { startActivity(it) }
    }
    fun goToTtsActivity() {
        Intent(this, TextToSpeechActivity::class.java).also { startActivity(it) }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(binding.editTextId.text.toString(),binding.editTextPassword.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    // Login, 아이디와 패스워드가 맞았을 때
                    goHomeActivity(task.result?.user)
                } else {
                    // Show the error message, 아이디와 패스워드가 틀렸을 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
}