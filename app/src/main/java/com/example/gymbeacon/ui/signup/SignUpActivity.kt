package com.example.gymbeacon.ui.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivitySignUpBinding
import com.example.gymbeacon.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)
        binding.vm = viewModel
        subscribe()

        with(binding) {
            buttonSignUp.setOnClickListener {
                viewModel.signUp()
            }
        }
    }

    fun goHomeActivity() {
        Intent(this, HomeActivity::class.java).also { startActivity(it) }
    }

    fun subscribe() {
        with(viewModel) {
            signUpState.observe(this@SignUpActivity) {
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
}