package com.example.binm.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.UpdateProfileRequest
import com.example.binm.data.UserProfileResponse
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfileResponse) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val token = sessionManager.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _uiState.value = ProfileUiState.Error("Brak autoryzacji")
                    return@launch
                }
                
                val profile = RetrofitInstance.api.getUserProfile("Bearer $token")
                _uiState.value = ProfileUiState.Success(profile)
                _profileImageUrl.value = profile.profileImageUrl
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Błąd ładowania profilu: ${e.message}")
            }
        }
    }
    
    fun updateName(newName: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val token = sessionManager.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("Brak autoryzacji")
                    return@launch
                }
                
                val response = RetrofitInstance.api.updateProfile(
                    "Bearer $token",
                    UpdateProfileRequest(name = newName)
                )
                
                if (response.isSuccessful) {
                    _updateState.value = UpdateState.Success
                    loadProfile()
                } else {
                    _updateState.value = UpdateState.Error("Błąd aktualizacji")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Błąd: ${e.message}")
            }
        }
    }
    
    fun uploadProfileImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val token = sessionManager.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("Brak autoryzacji")
                    return@launch
                }
                
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Nie można odczytać pliku")
                inputStream.close()
                
                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "profile_image.jpg", requestBody)
                
                val uploadResponse = RetrofitInstance.api.uploadProfileImage("Bearer $token", part)
                val imageUrl = uploadResponse.string()
                
                val updateResponse = RetrofitInstance.api.updateProfile(
                    "Bearer $token",
                    UpdateProfileRequest(profileImageUrl = imageUrl)
                )
                
                if (updateResponse.isSuccessful) {
                    _profileImageUrl.value = imageUrl
                    _updateState.value = UpdateState.Success
                    loadProfile()
                } else {
                    _updateState.value = UpdateState.Error("Błąd zapisywania zdjęcia")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Błąd uploadu: ${e.message}")
            }
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}

class ProfileViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
