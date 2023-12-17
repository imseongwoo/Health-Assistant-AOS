package com.example.domain.auth

import android.widget.Toast
import com.example.domain.model.LoginResult
import com.example.domain.model.SignUpResult
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {
    private var auth: FirebaseAuth? = FirebaseAuth.getInstance()

    // signinEmail 함수 이동
    fun signinEmail(email: String, password: String, onComplete: (LoginResult) -> Unit) {
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login, 아이디와 패스워드가 맞았을 때
                    onComplete(LoginResult(isSuccess = true))
                } else {
                    // Show the error message, 아이디와 패스워드가 틀렸을 때
                    onComplete(LoginResult(errorMessage = "로그인 실패", isSuccess = false))
                }
            }
    }

    fun signUp(email: String, password: String, onComplete: (SignUpResult) -> Unit) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
//                    goHomeActivity(task.result?.user)
                    onComplete(SignUpResult(isSuccess = true))
                } else if(task.exception?.message.isNullOrEmpty()) {
//                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    onComplete(SignUpResult(errorMessage = "${task.exception?.message}"))
                } else {
//                    Toast.makeText(this,"이미 회원가입 된 계정입니다.", Toast.LENGTH_SHORT).show()
                    onComplete(SignUpResult(errorMessage = "이미 회원가입 된 계정입니다."))
                }
            }
    }
}