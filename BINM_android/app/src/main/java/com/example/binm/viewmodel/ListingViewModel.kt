package com.example.binm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.Product
import com.example.binm.data.ProductSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definicja stanu UI, której brakowało
sealed interface ListingsUiState {
    object Loading : ListingsUiState
    data class Success(val listings: List<Product>) : ListingsUiState
    data class Error(val message: String) : ListingsUiState
}

class ListingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ListingsUiState>(ListingsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchRandomListings()
    }

    fun fetchRandomListings() {
        viewModelScope.launch {
            _uiState.value = ListingsUiState.Loading
            try {
                val response = RetrofitInstance.api.getRandomListings(size = 10)
                _uiState.value = ListingsUiState.Success(response.content)
            } catch (e: Exception) {
                _uiState.value = ListingsUiState.Error("Nie udało się pobrać ogłoszeń.")
            }
        }
    }

    fun searchListings(query: String) {
        viewModelScope.launch {
            _uiState.value = ListingsUiState.Loading
            try {
                val request = ProductSearchRequest(query = query)
                val response = RetrofitInstance.api.searchListings(request)
                _uiState.value = ListingsUiState.Success(response.content)
            } catch (e: Exception) {
                _uiState.value = ListingsUiState.Error("Błąd wyszukiwania.")
            }
        }
    }
}
