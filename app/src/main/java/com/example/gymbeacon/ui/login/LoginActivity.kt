package com.example.gymbeacon.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityLoginBinding
import com.example.gymbeacon.ui.home.HomeActivity
import com.example.gymbeacon.ui.signup.SignUpActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        with(binding) {
            buttonLogin.setOnClickListener {
                goHomeActivity()
            }
            textViewSignUp.setOnClickListener {
                goSignUpActivity()
            }
        }
    }

    fun goHomeActivity() {
        Intent(this,HomeActivity::class.java).also { startActivity(it) }
    }
    fun goSignUpActivity() {
        Intent(this, SignUpActivity::class.java).also { startActivity(it) }
    }
}