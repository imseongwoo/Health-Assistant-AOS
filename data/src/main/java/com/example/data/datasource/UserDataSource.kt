package com.example.data.datasource

import com.example.domain.model.LoginResult
import com.example.domain.model.NaviHomeEntity
import com.example.domain.model.SignUpResult

interface UserDataSource {
    suspend fun postLogin(account: String, password: String, onResult: (LoginResult) -> Unit)
    suspend fun signUp(account: String, password: String, onResult: (SignUpResult) -> Unit)
    suspend fun getTrainingData(onResult: (NaviHomeEntity) -> Unit)
}