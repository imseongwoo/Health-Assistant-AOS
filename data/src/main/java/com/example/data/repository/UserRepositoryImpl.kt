package com.example.data.repository

import com.example.data.datasource.remote.UserRemoteDataSource
import com.example.domain.model.LoginResult
import com.example.domain.model.SignUpResult
import com.example.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun postLogin(
        account: String,
        password: String,
        onResult: (LoginResult) -> Unit,
    ) {
        userRemoteDataSource.postLogin(account,password){ result ->
            if (result.isSuccess) {
                onResult(LoginResult(isSuccess = true))
            } else {
                onResult(LoginResult(errorMessage = "로그인 실패", isSuccess = false))
            }
        }
    }

    override suspend fun signUp(account: String, password: String, onResult: (SignUpResult) -> Unit) {
        userRemoteDataSource.signUp(account, password){result ->
            if (result.isSuccess) {
                onResult(SignUpResult(isSuccess = true))
            } else {
                onResult(SignUpResult(errorMessage = "${result.errorMessage}", isSuccess = false))
            }
        }
    }

}