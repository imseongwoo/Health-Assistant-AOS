package com.example.domain.base

data class ErrorType(
    val errorMessage: String,
    val code: Int = 0
)
