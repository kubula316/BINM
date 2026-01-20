package com.example.binm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.Product
import com.example.binm.manager.AuthManager
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class ListingStatusFilter(val apiValue: String?, val displayName: String) {
    ALL(null, "Wszystkie"),
    ACTIVE("ACTIVE", "Aktywne"),
    WAITING("WAITING", "Oczekujące"),
    DRAFT("DRAFT", "Szkice"),
    REJECTED("REJECTED", "Odrzucone"),
    COMPLETED("COMPLETED", "Zakończone")
}

class MyListingsViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _myListings = MutableStateFlow<List<Product>>(emptyList())
    val myListings = _myListings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow(ListingStatusFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    fun fetchMyListings(statusFilter: ListingStatusFilter = _selectedFilter.value) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedFilter.value = statusFilter
            
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                _isLoading.value = false
                return@launch
            }
            try {
                val response = RetrofitInstance.api.getMyListings(
                    token = "Bearer $token",
                    status = statusFilter.apiValue
                )
                Log.d("MyListingsViewModel", "Filtr: ${statusFilter.displayName}, otrzymano ${response.content.size} ogłoszeń")
                _myListings.value = response.content
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    AuthManager.onAuthRequired()
                } else {
                    _error.value = "Wystąpił błąd podczas pobierania ogłoszeń: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Wystąpił błąd podczas pobierania ogłoszeń: ${e.message}"
            }
            _isLoading.value = false
        }
    }
    
    fun setFilter(filter: ListingStatusFilter) {
        if (filter != _selectedFilter.value) {
            fetchMyListings(filter)
        }
    }

    fun deleteListing(publicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                _isLoading.value = false
                return@launch
            }
            try {
                val response = RetrofitInstance.api.deleteListing("Bearer $token", publicId)
                if (response.isSuccessful) {
                    fetchMyListings()
                } else {
                    if (response.code() == 401) {
                        AuthManager.onAuthRequired()
                    } else {
                        _error.value = "Nie udało się usunąć ogłoszenia."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Błąd podczas usuwania: ${e.message}"
            }
            _isLoading.value = false
        }
    }
    
    fun submitForApproval(publicId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                _isLoading.value = false
                return@launch
            }
            try {
                val response = RetrofitInstance.api.submitForApproval("Bearer $token", publicId)
                if (response.isSuccessful) {
                    Log.d("MyListingsViewModel", "Ogłoszenie $publicId wysłane do weryfikacji")
                    onSuccess()
                    fetchMyListings()
                } else {
                    if (response.code() == 401) {
                        AuthManager.onAuthRequired()
                    } else {
                        _error.value = "Nie udało się wysłać do weryfikacji."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
            _isLoading.value = false
        }
    }
}

class MyListingsViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyListingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyListingsViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
