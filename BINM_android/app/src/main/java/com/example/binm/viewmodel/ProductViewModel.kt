package com.example.binm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.AttributeFilter
import com.example.binm.data.FilterAttribute
import com.example.binm.data.Product
import com.example.binm.data.ProductDetail
import com.example.binm.data.ProductSearchRequest
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface ProductDetailUiState {
    object Loading : ProductDetailUiState
    data class Success(val product: ProductDetail) : ProductDetailUiState
    data class Error(val message: String, val isNotActive: Boolean = false) : ProductDetailUiState
}

class ProductViewModel(private val sessionManager: SessionManager? = null) : ViewModel() {

    private val _filters = MutableStateFlow<List<FilterAttribute>>(emptyList())
    val filters = _filters.asStateFlow()

    private val _selectedFilters = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedFilters = _selectedFilters.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _productDetailState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val productDetailState = _productDetailState.asStateFlow()

    fun fetchProductDetails(id: String) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailUiState.Loading
            try {
                // Najpierw spróbuj pobrać publicznie
                val product = RetrofitInstance.api.getListingDetails(id)
                _productDetailState.value = ProductDetailUiState.Success(product)
                Log.d("ProductViewModel", "Pobrano szczegóły produktu: $product")
            } catch (e: HttpException) {
                Log.e("ProductViewModel", "HTTP error: ${e.code()}", e)
                when (e.code()) {
                    404, 403 -> {
                        // Ogłoszenie nie jest aktywne lub nie istnieje - spróbuj jako właściciel
                        tryFetchAsOwner(id)
                    }
                    else -> {
                        _productDetailState.value = ProductDetailUiState.Error("Błąd serwera: ${e.code()}")
                    }
                }
            } catch (t: Throwable) {
                Log.e("ProductViewModel", "Krytyczny błąd podczas pobierania szczegółów produktu", t)
                _productDetailState.value = ProductDetailUiState.Error("Wystąpił błąd połączenia z serwerem.")
            }
        }
    }
    
    private suspend fun tryFetchAsOwner(id: String) {
        val token = sessionManager?.tokenFlow?.firstOrNull()
        if (token.isNullOrBlank()) {
            _productDetailState.value = ProductDetailUiState.Error(
                "To ogłoszenie nie jest jeszcze aktywne lub nie istnieje.",
                isNotActive = true
            )
            return
        }
        
        try {
            val product = RetrofitInstance.api.getListingEditData("Bearer $token", id)
            
            // Sprawdź status ogłoszenia
            when (product.status) {
                "ACTIVE" -> {
                    _productDetailState.value = ProductDetailUiState.Success(product)
                }
                "WAITING" -> {
                    _productDetailState.value = ProductDetailUiState.Error(
                        "To ogłoszenie oczekuje na weryfikację przez administrację. Prosimy o cierpliwość.",
                        isNotActive = true
                    )
                }
                "DRAFT" -> {
                    _productDetailState.value = ProductDetailUiState.Error(
                        "To ogłoszenie jest szkicem. Aby je opublikować, wyślij je do weryfikacji.",
                        isNotActive = true
                    )
                }
                "REJECTED" -> {
                    _productDetailState.value = ProductDetailUiState.Error(
                        "To ogłoszenie zostało odrzucone przez administrację. Sprawdź powód w edycji ogłoszenia.",
                        isNotActive = true
                    )
                }
                "SUSPENDED" -> {
                    _productDetailState.value = ProductDetailUiState.Error(
                        "To ogłoszenie zostało zawieszone.",
                        isNotActive = true
                    )
                }
                "COMPLETED" -> {
                    _productDetailState.value = ProductDetailUiState.Error(
                        "To ogłoszenie zostało zakończone.",
                        isNotActive = true
                    )
                }
                else -> {
                    _productDetailState.value = ProductDetailUiState.Success(product)
                }
            }
        } catch (e: HttpException) {
            Log.e("ProductViewModel", "Nie można pobrać jako właściciel: ${e.code()}", e)
            _productDetailState.value = ProductDetailUiState.Error(
                "To ogłoszenie nie jest dostępne lub nie masz do niego dostępu.",
                isNotActive = true
            )
        } catch (t: Throwable) {
            Log.e("ProductViewModel", "Błąd podczas pobierania jako właściciel", t)
            _productDetailState.value = ProductDetailUiState.Error("Wystąpił błąd połączenia z serwerem.")
        }
    }

    fun clearProductDetails() {
        _productDetailState.value = ProductDetailUiState.Loading
    }

    fun searchProducts(categoryId: Int? = null, query: String? = null) {
        viewModelScope.launch {
            try {
                val attributeFilters = buildAttributeFilters()
                val request = ProductSearchRequest(
                    categoryId = categoryId,
                    query = query,
                    sellerUserId = null,
                    attributes = attributeFilters
                )

                Log.d("ProductViewModel", "Wysyłanie zapytania: $request")
                val response = RetrofitInstance.api.searchListings(request)
                _products.value = response.content
                Log.d("ProductViewModel", "Otrzymano odpowiedź: ${response.content.size} produktów")

            } catch (e: Exception) {
                Log.e("ProductViewModel", "Błąd podczas wyszukiwania produktów", e)
                _products.value = emptyList()
            }
        }
    }

    fun fetchFilters(categoryId: Int) {
        viewModelScope.launch {
            try {
                _filters.value = RetrofitInstance.api.getCategoryAttributes(categoryId)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Błąd podczas pobierania filtrów", e)
                _filters.value = emptyList()
            }
        }
    }

    fun updateSelectedFilter(key: String, value: String) {
        _selectedFilters.value = _selectedFilters.value.toMutableMap().apply {
            if (value.isBlank()) {
                remove(key)
            } else {
                this[key] = value
            }
        }
    }

    private fun buildAttributeFilters(): List<AttributeFilter> {
        val builtFilters = mutableListOf<AttributeFilter>()
        val processedKeys = mutableSetOf<String>()

        _selectedFilters.value.keys.forEach { key ->
            if (key in processedKeys) return@forEach

            val baseKey = key.removeSuffix("_from").removeSuffix("_to")
            val attribute = _filters.value.find { it.key == baseKey }

            if (attribute != null) {
                when (attribute.type) {
                    "ENUM", "STRING" -> {
                        _selectedFilters.value[key]?.let {
                            builtFilters.add(AttributeFilter(key = baseKey, type = attribute.type, op = "eq", value = it))
                            processedKeys.add(key)
                        }
                    }
                    "NUMBER" -> {
                        val from = _selectedFilters.value[baseKey + "_from"]
                        val to = _selectedFilters.value[baseKey + "_to"]
                        if (from != null || to != null) {
                            builtFilters.add(AttributeFilter(key = baseKey, type = attribute.type, op = "between", from = from, to = to))
                            processedKeys.add(baseKey + "_from")
                            processedKeys.add(baseKey + "_to")
                        }
                    }
                }
            }
        }
        return builtFilters
    }
}

class ProductViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
