package com.example.data.datasource.remote

import com.example.data.datasource.UserDataSource
import com.example.domain.auth.FirebaseAuthManager
import com.example.domain.model.LoginResult

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
}