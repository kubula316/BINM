package com.example.binm.data

/**
 * Obiekt transferu danych (DTO) dla żądania logowania.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
