package com.example.domain.auth

import com.example.domain.model.LoginResult
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
}