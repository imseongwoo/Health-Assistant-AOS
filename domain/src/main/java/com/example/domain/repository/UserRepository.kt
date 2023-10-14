package com.example.domain.repository

import com.example.domain.model.LoginResult

interface UserRepository {
    suspend fun postLogin(
        account: String,
        password: String,
        onResult: (LoginResult) -> Unit
    )
}