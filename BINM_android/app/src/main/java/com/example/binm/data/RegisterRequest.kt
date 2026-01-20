package com.example.binm.data

/**
 * Obiekt transferu danych (DTO) dla żądania rejestracji.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
