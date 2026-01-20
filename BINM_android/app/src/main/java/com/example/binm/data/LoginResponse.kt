package com.example.binm.data

/**
 * Obiekt transferu danych (DTO) dla odpowiedzi po pomyślnym logowaniu.
 */
data class LoginResponse(
    val userId: String,
    val name: String,
    val email: String,
    val token: String // Dodane pole na prawdziwy token JWT
)
