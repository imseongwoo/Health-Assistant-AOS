package com.example.data.datasource

import com.example.domain.model.LoginResult

interface UserDataSource {
    suspend fun postLogin(account: String, password: String, onResult: (LoginResult) -> Unit)
}