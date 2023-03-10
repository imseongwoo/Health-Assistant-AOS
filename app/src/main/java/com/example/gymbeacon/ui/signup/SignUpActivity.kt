package com.example.gymbeacon.ui.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivitySignUpBinding
import com.example.gymbeacon.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()
        with(binding) {
            buttonSignUp.setOnClickListener {
                signUp()
            }
        }
    }

    fun signUp() {
        auth?.createUserWithEmailAndPassword(binding.editTextId.text.toString(),binding.editTextPassword.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    goHomeActivity(task.result?.user)
                } else if(task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this,"이미 회원가입 된 계정입니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun goHomeActivity(user: FirebaseUser?) {
        if (user != null) {
            Intent(this, HomeActivity::class.java).also { startActivity(it) }
        }
    }
}