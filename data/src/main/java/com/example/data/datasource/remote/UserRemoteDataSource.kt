package com.example.data.datasource.remote

import com.example.data.datasource.UserDataSource
import com.example.domain.auth.FirebaseAuthManager
import com.example.domain.model.LoginResult
import com.example.domain.model.SignUpResult

class UserRemoteDataSource: UserDataSource {
    override suspend fun postLogin(account: String, password: String, onResult: (LoginResult) -> Unit){
        FirebaseAuthManager.signinEmail(account, password){ result ->
            if (result.isSuccess) {
                onResult(LoginResult(isSuccess = true))
            } else {
                onResult(LoginResult(errorMessage = "로그인 실패", isSuccess = false))
            }

        }
    }

    override suspend fun signUp(
        account: String,
        password: String,
        onResult: (SignUpResult) -> Unit,
    ) {
        FirebaseAuthManager.signUp(account, password) { result ->
            if (result.isSuccess) {
                onResult(SignUpResult(isSuccess = true))
            } else {
                onResult(SignUpResult(errorMessage = "${result.errorMessage}", isSuccess = false))
            }

        }
    }

}