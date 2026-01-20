package com.example.binm.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.LoginRequest
import com.example.binm.data.RegisterRequest
import com.example.binm.data.VerifyOtpRequest
import com.example.binm.manager.AuthManager
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Success : AuthState
    data class Error(val message: String) : AuthState
    object OtpRequired : AuthState
}

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val name = mutableStateOf("")
    val otp = mutableStateOf("")

    fun login() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val request = LoginRequest(email.value, password.value)
            try {
                val response = RetrofitInstance.api.login(request)
                // Po zalogowaniu, od razu pobierz profil i zapisz UUID
                fetchAndSaveUserProfile(response.token)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Błędne dane logowania lub błąd serwera.")
            }
        }
    }

    private suspend fun fetchAndSaveUserProfile(token: String) {
        try {
            val userProfile = RetrofitInstance.api.getUserProfile("Bearer $token")
            sessionManager.saveTokenAndUserId(token, userProfile.userId)
        } catch (e: Exception) {
            // W przypadku błędu zapisz sam token, aby aplikacja mogła działać
            sessionManager.saveTokenAndUserId(token, "") 
        }
    }

    fun register() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val request = RegisterRequest(name.value, email.value, password.value)
            try {
                val response = RetrofitInstance.api.register(request)
                if (response.isSuccessful) {
                    login() // Po rejestracji logujemy, co pobierze też profil i zapisze UUID
                    _authState.value = AuthState.OtpRequired
                } else {
                    _authState.value = AuthState.Error("Rejestracja nieudana: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Wystąpił błąd: ${e.message}")
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                return@launch
            }
            try {
                val response = RetrofitInstance.api.verifyOtp("Bearer $token", VerifyOtpRequest(otp.value))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Błędny kod OTP.")
                }
            } catch (e: HttpException) {
                if (e.code() == 401) AuthManager.onAuthRequired()
                _authState.value = AuthState.Error("Błąd weryfikacji: ${e.message}")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Błąd weryfikacji: ${e.message}")
            }
        }
    }
    
    fun resendOtp() {
        viewModelScope.launch {
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                return@launch
            }
            try {
                RetrofitInstance.api.resendOtp("Bearer $token")
            } catch (_: Exception) { /* Można dodać obsługę błędu */ }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
