package com.example.binm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.FavoriteRequest
import com.example.binm.data.Product
import com.example.binm.manager.AuthManager
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Jeden, centralny ViewModel do zarządzania ulubionymi w całej aplikacji
class FavoriteViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _favoriteProducts = MutableStateFlow<List<Product>>(emptyList())
    val favoriteProducts = _favoriteProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Lokalne ID do dodania (optymistyczne dodawanie)
    private val _pendingAddIds = MutableStateFlow<Set<String>>(emptySet())
    // Lokalne ID do usunięcia (optymistyczne usuwanie)
    private val _pendingRemoveIds = MutableStateFlow<Set<String>>(emptySet())

    // Kombinacja ID z serwera z lokalnymi modyfikacjami
    val favoriteIds: StateFlow<Set<String>> = combine(
        favoriteProducts.map { products -> products.map { it.publicId }.toSet() },
        _pendingAddIds,
        _pendingRemoveIds
    ) { serverIds, pendingAdd, pendingRemove ->
        (serverIds + pendingAdd) - pendingRemove
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        // Reaguj na zmiany w stanie logowania - pobierz ulubione, gdy użytkownik się zaloguje
        viewModelScope.launch {
            sessionManager.tokenFlow.collect { token ->
                if (!token.isNullOrBlank()) {
                    fetchFavorites()
                } else {
                    _favoriteProducts.value = emptyList() // Wyczyść ulubione po wylogowaniu
                }
            }
        }
    }

    fun fetchFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val token = sessionManager.tokenFlow.firstOrNull()
            Log.d("FavoriteViewModel", "fetchFavorites called, token: ${if (token.isNullOrBlank()) "null/blank" else "present"}")
            if (token.isNullOrBlank()) {
                _isLoading.value = false
                return@launch
            }
            try {
                Log.d("FavoriteViewModel", "Calling API getFavorites...")
                val response = RetrofitInstance.api.getFavorites("Bearer $token")
                Log.d("FavoriteViewModel", "Got ${response.content.size} favorites")
                _favoriteProducts.value = response.content
            } catch (e: HttpException) {
                Log.e("FavoriteViewModel", "HttpException: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    AuthManager.onAuthRequired()
                } else {
                    _error.value = "Błąd serwera: ${e.code()}"
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Exception: ${e.message}", e)
                _error.value = "Wystąpił nieznany błąd: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Uniwersalna funkcja do przełączania statusu ulubionego
    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            val token = sessionManager.tokenFlow.firstOrNull()
            Log.d("FavoriteViewModel", "toggleFavorite called for: $listingId")
            if (token.isNullOrBlank()) {
                Log.w("FavoriteViewModel", "No token, requiring auth")
                AuthManager.onAuthRequired()
                return@launch
            }

            val isCurrentlyFavorite = favoriteIds.value.contains(listingId)
            val authHeader = "Bearer $token"
            Log.d("FavoriteViewModel", "isCurrentlyFavorite: $isCurrentlyFavorite")

            // Optymistyczna aktualizacja UI - natychmiast zmieniamy lokalny stan
            if (isCurrentlyFavorite) {
                // Oznacz jako "do usunięcia" - gwiazdka natychmiast zgaśnie
                _pendingRemoveIds.value = _pendingRemoveIds.value + listingId
                _pendingAddIds.value = _pendingAddIds.value - listingId
            } else {
                // Oznacz jako "do dodania" - gwiazdka natychmiast się zaświeci
                _pendingAddIds.value = _pendingAddIds.value + listingId
                _pendingRemoveIds.value = _pendingRemoveIds.value - listingId
            }

            try {
                val request = FavoriteRequest(entityId = listingId, entityType = "LISTING")
                
                if (isCurrentlyFavorite) {
                    Log.d("FavoriteViewModel", "Removing from favorites...")
                    val response = RetrofitInstance.api.removeFromFavorites(authHeader, request)
                    Log.d("FavoriteViewModel", "Remove response: ${response.code()}")
                    if (response.isSuccessful) {
                        // Usuń z listy produktów i wyczyść pending
                        _favoriteProducts.value = _favoriteProducts.value.filter { it.publicId != listingId }
                        _pendingRemoveIds.value = _pendingRemoveIds.value - listingId
                    } else {
                        Log.e("FavoriteViewModel", "Remove failed, reverting...")
                        _pendingRemoveIds.value = _pendingRemoveIds.value - listingId // Przywróć
                    }
                } else {
                    Log.d("FavoriteViewModel", "Adding to favorites: $listingId")
                    val response = RetrofitInstance.api.addToFavorites(authHeader, request)
                    Log.d("FavoriteViewModel", "Add response: ${response.code()}")
                    if (response.isSuccessful) {
                        // Pobierz zaktualizowaną listę aby mieć pełne dane produktu
                        fetchFavorites()
                        _pendingAddIds.value = _pendingAddIds.value - listingId
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("FavoriteViewModel", "Add failed with code: ${response.code()}, body: $errorBody")
                        _pendingAddIds.value = _pendingAddIds.value - listingId // Przywróć
                        
                        // Sprawdź czy to błąd 500 - może konto nie jest zweryfikowane
                        if (response.code() == 500) {
                            try {
                                val profile = RetrofitInstance.api.getUserProfile(authHeader)
                                if (!profile.isAccountVerified) {
                                    _error.value = "Musisz zweryfikować swoje konto, aby dodawać do ulubionych"
                                    Log.e("FavoriteViewModel", "Account not verified!")
                                } else {
                                    _error.value = "Błąd serwera. Spróbuj ponownie później."
                                }
                            } catch (e: Exception) {
                                _error.value = "Błąd serwera (500). Sprawdź czy ogłoszenie jest aktywne."
                            }
                        } else {
                            _error.value = "Nie udało się dodać do ulubionych (kod: ${response.code()})"
                        }
                    }
                }
            } catch (e: HttpException) {
                Log.e("FavoriteViewModel", "toggleFavorite HttpException: ${e.code()}")
                // Przywróć stan przy błędzie
                if (isCurrentlyFavorite) {
                    _pendingRemoveIds.value = _pendingRemoveIds.value - listingId
                } else {
                    _pendingAddIds.value = _pendingAddIds.value - listingId
                }
                if (e.code() == 401) {
                    AuthManager.onAuthRequired()
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "toggleFavorite Exception: ${e.message}", e)
                // Przywróć stan przy błędzie
                if (isCurrentlyFavorite) {
                    _pendingRemoveIds.value = _pendingRemoveIds.value - listingId
                } else {
                    _pendingAddIds.value = _pendingAddIds.value - listingId
                }
            }
        }
    }
}

// Fabryka do tworzenia instancji FavoriteViewModel z wymaganym SessionManagerem
class FavoriteViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
