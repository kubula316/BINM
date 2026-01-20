package com.example.binm.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.Category
import com.example.binm.data.CreateAttribute
import com.example.binm.data.CreateListingRequest
import com.example.binm.data.FilterAttribute
import com.example.binm.manager.AuthManager
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream

sealed interface CreateListingUiState {
    object Idle : CreateListingUiState
    object Loading : CreateListingUiState
    data class Uploading(val current: Int, val total: Int) : CreateListingUiState
    object Success : CreateListingUiState
    data class Error(val message: String) : CreateListingUiState
}

class AddOfferViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateListingUiState>(CreateListingUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()
    val selectedCategoryId = mutableStateOf<Int?>(null)

    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val price = mutableStateOf("")
    val negotiable = mutableStateOf(false)
    val locationCity = mutableStateOf("")

    // Zdjęcia - lokalne URI
    val imageUris = mutableStateListOf<String>()

    private val _attributes = MutableStateFlow<List<FilterAttribute>>(emptyList())
    val attributes = _attributes.asStateFlow()
    val attributeValues = mutableStateMapOf<String, String>()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = RetrofitInstance.api.getCategories()
            } catch (_: Exception) { }
        }
    }

    fun selectCategory(categoryId: Int) {
        selectedCategoryId.value = categoryId
        attributeValues.clear()
        viewModelScope.launch {
            try {
                _attributes.value = RetrofitInstance.api.getCategoryAttributes(categoryId)
            } catch (e: Exception) {
                _uiState.value = CreateListingUiState.Error("Błąd pobierania atrybutów dla kategorii.")
            }
        }
    }

    fun createListing(context: Context) {
        viewModelScope.launch {
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                return@launch
            }

            val categoryId = selectedCategoryId.value
            if (categoryId == null) {
                _uiState.value = CreateListingUiState.Error("Musisz wybrać kategorię.")
                return@launch
            }
            if (title.value.isBlank()) {
                _uiState.value = CreateListingUiState.Error("Tytuł jest wymagany.")
                return@launch
            }
            if (title.value.length < 5) {
                _uiState.value = CreateListingUiState.Error("Tytuł musi mieć min. 5 znaków.")
                return@launch
            }
            if (title.value.length > 100) {
                _uiState.value = CreateListingUiState.Error("Tytuł może mieć max. 100 znaków.")
                return@launch
            }
            if (price.value.isBlank()) {
                _uiState.value = CreateListingUiState.Error("Cena jest wymagana.")
                return@launch
            }
            val priceDouble = price.value.replace(",", ".").toDoubleOrNull()
            if (priceDouble == null || priceDouble < 0) {
                _uiState.value = CreateListingUiState.Error("Cena musi być poprawną liczbą dodatnią.")
                return@launch
            }
            if (priceDouble > 10_000_000) {
                _uiState.value = CreateListingUiState.Error("Cena jest zbyt wysoka.")
                return@launch
            }

            // Upload zdjęć
            val uploadedUrls = mutableListOf<String>()
            if (imageUris.isNotEmpty()) {
                val total = imageUris.size
                for ((index, uriString) in imageUris.withIndex()) {
                    _uiState.value = CreateListingUiState.Uploading(index + 1, total)
                    try {
                        val url = uploadImage(context, token, uriString)
                        if (url != null) {
                            uploadedUrls.add(url)
                        }
                    } catch (e: Exception) {
                        _uiState.value = CreateListingUiState.Error("Błąd uploadu zdjęcia ${index + 1}: ${e.message}")
                        return@launch
                    }
                }
            }

            _uiState.value = CreateListingUiState.Loading

            val listingAttributes = attributeValues.map { CreateAttribute(it.key, it.value) }

            val request = CreateListingRequest(
                categoryId = categoryId,
                title = title.value,
                description = description.value,
                priceAmount = priceDouble,
                negotiable = negotiable.value,
                locationCity = locationCity.value.ifBlank { null },
                locationRegion = null,
                mediaUrls = uploadedUrls.ifEmpty { null },
                attributes = listingAttributes.ifEmpty { null }
            )

            try {
                val response = RetrofitInstance.api.createListing("Bearer $token", request)
                if (response.isSuccessful) {
                    _uiState.value = CreateListingUiState.Success
                } else {
                    if (response.code() == 401) {
                        AuthManager.onAuthRequired()
                    } else {
                        _uiState.value = CreateListingUiState.Error("Błąd serwera: ${response.code()}")
                    }
                }
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    AuthManager.onAuthRequired()
                } else {
                    _uiState.value = CreateListingUiState.Error("Wystąpił błąd sieciowy: ${e.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = CreateListingUiState.Error("Wystąpił nieznany błąd: ${e.message}")
            }
        }
    }

    private suspend fun uploadImage(context: Context, token: String, uriString: String): String? {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        try {
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

            val response = RetrofitInstance.api.uploadMediaImage("Bearer $token", part)
            return response.string().trim()
        } finally {
            tempFile.delete()
        }
    }

    fun resetState() {
        _uiState.value = CreateListingUiState.Idle
    }
}

class AddOfferViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddOfferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddOfferViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
