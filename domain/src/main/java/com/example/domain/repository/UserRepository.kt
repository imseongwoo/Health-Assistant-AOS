package com.example.domain.repository

import com.example.domain.model.LoginResult
import com.example.domain.model.SignUpResult

interface UserRepository {
    suspend fun postLogin(
        account: String,
        password: String,
        onResult: (LoginResult) -> Unit,
    )

    suspend fun signUp(account: String, password: String, onResult: (SignUpResult) -> Unit)
}