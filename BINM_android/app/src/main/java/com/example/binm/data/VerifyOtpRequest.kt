package com.example.binm.data

/**
 * Obiekt transferu danych (DTO) dla żądania weryfikacji kodu OTP.
 */
data class VerifyOtpRequest(
    val otp: String
)
