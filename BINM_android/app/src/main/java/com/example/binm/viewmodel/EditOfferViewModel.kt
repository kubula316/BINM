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

sealed interface EditListingUiState {
    object Idle : EditListingUiState
    object Loading : EditListingUiState
    data class Uploading(val current: Int, val total: Int) : EditListingUiState
    object Success : EditListingUiState
    data class Error(val message: String) : EditListingUiState
}

// Reprezentacja zdjęcia - może być URL z serwera lub lokalne URI
data class ImageItem(
    val uri: String,
    val isLocal: Boolean // true = lokalne URI do uploadu, false = już na serwerze
)

class EditOfferViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow<EditListingUiState>(EditListingUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val price = mutableStateOf("")
    val negotiable = mutableStateOf(false)
    val locationCity = mutableStateOf("")

    // Zdjęcia - zarówno istniejące (z serwera) jak i nowe (lokalne)
    val images = mutableStateListOf<ImageItem>()

    private val _attributes = MutableStateFlow<List<FilterAttribute>>(emptyList())
    val attributes = _attributes.asStateFlow()
    val attributeValues = mutableStateMapOf<String, String>()
    
    private var categoryId: Int? = null

    fun loadListingData(publicId: String) {
        viewModelScope.launch {
            _uiState.value = EditListingUiState.Loading
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                return@launch
            }
            try {
                val listing = RetrofitInstance.api.getListingEditData("Bearer $token", publicId)
                title.value = listing.title ?: ""
                description.value = listing.description ?: ""
                price.value = listing.priceAmount?.toString() ?: ""
                negotiable.value = listing.negotiable ?: false
                locationCity.value = listing.locationCity ?: ""
                categoryId = listing.category?.id

                // Załaduj istniejące zdjęcia
                images.clear()
                listing.getImageUrls().forEach { url ->
                    images.add(ImageItem(uri = url, isLocal = false))
                }

                // Załaduj atrybuty
                listing.attributes?.forEach { attr ->
                    attr.key?.let { key ->
                        val displayValue = attr.getDisplayValue()
                        if (displayValue != "-") {
                            attributeValues[key] = displayValue
                        }
                    }
                }

                // Pobierz definicje atrybutów dla kategorii
                categoryId?.let { catId ->
                    try {
                        _attributes.value = RetrofitInstance.api.getCategoryAttributes(catId)
                    } catch (_: Exception) { }
                }

                _uiState.value = EditListingUiState.Idle
            } catch (e: HttpException) {
                if(e.code() == 401) AuthManager.onAuthRequired()
                _uiState.value = EditListingUiState.Error("Nie udało się załadować danych ogłoszenia.")
            } catch (e: Exception) {
                _uiState.value = EditListingUiState.Error("Nie udało się załadować danych ogłoszenia.")
            }
        }
    }

    fun addImage(uri: String) {
        images.add(ImageItem(uri = uri, isLocal = true))
    }

    fun removeImage(index: Int) {
        if (index in images.indices) {
            images.removeAt(index)
        }
    }

    fun updateListing(publicId: String, context: Context) {
        viewModelScope.launch {
            val token = sessionManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                AuthManager.onAuthRequired()
                return@launch
            }

            val priceDouble = price.value.toDoubleOrNull()
            if (priceDouble == null) {
                _uiState.value = EditListingUiState.Error("Cena musi być poprawną liczbą.")
                return@launch
            }

            // Upload nowych zdjęć
            val finalUrls = mutableListOf<String>()
            val localImages = images.filter { it.isLocal }
            val serverImages = images.filter { !it.isLocal }

            // Dodaj istniejące URL-e
            serverImages.forEach { finalUrls.add(it.uri) }

            // Upload nowych
            if (localImages.isNotEmpty()) {
                val total = localImages.size
                for ((index, imageItem) in localImages.withIndex()) {
                    _uiState.value = EditListingUiState.Uploading(index + 1, total)
                    try {
                        val url = uploadImage(context, token, imageItem.uri)
                        if (url != null) {
                            finalUrls.add(url)
                        }
                    } catch (e: Exception) {
                        _uiState.value = EditListingUiState.Error("Błąd uploadu zdjęcia ${index + 1}: ${e.message}")
                        return@launch
                    }
                }
            }

            _uiState.value = EditListingUiState.Loading

            val listingAttributes = attributeValues.map { CreateAttribute(it.key, it.value) }

            val request = CreateListingRequest(
                categoryId = categoryId ?: 72,
                title = title.value,
                description = description.value,
                priceAmount = priceDouble,
                negotiable = negotiable.value,
                locationCity = locationCity.value.ifBlank { null },
                locationRegion = null,
                mediaUrls = finalUrls.ifEmpty { null },
                attributes = listingAttributes.ifEmpty { null }
            )

            try {
                val response = RetrofitInstance.api.updateListing("Bearer $token", publicId, request)
                if (response.isSuccessful) {
                    _uiState.value = EditListingUiState.Success
                } else {
                    if (response.code() == 401) {
                        AuthManager.onAuthRequired()
                    } else {
                        _uiState.value = EditListingUiState.Error("Błąd serwera: ${response.code()}")
                    }
                }
            } catch (e: HttpException) {
                 if (e.code() == 401) {
                    AuthManager.onAuthRequired()
                } else {
                    _uiState.value = EditListingUiState.Error("Wystąpił błąd sieciowy: ${e.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = EditListingUiState.Error("Wystąpił nieznany błąd: ${e.message}")
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
}

class EditOfferViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditOfferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditOfferViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
