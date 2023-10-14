package com.example.data.repository

import com.example.data.datasource.remote.UserRemoteDataSource
import com.example.domain.model.LoginResult
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

}