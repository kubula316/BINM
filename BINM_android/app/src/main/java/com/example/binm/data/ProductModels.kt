package com.example.binm.data

import com.google.gson.annotations.SerializedName

// --- ODPOWIEDŹ Z SERWERA ---

data class ProductPageResponse(
    val content: List<Product>,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("totalElements") val totalElements: Long = 0,
    val last: Boolean = true,
    val first: Boolean = true
)

data class Product(
    val id: Int,
    val publicId: String,
    val title: String,
    val seller: Seller?,
    @SerializedName("priceAmount") val priceAmount: Double,
    val negotiable: Boolean,
    @SerializedName("coverImageUrl") val coverImageUrl: String,
    val status: String? = null,
    // Alternatywne nazwy które może zwracać serwer
    val listingStatus: String? = null,
    val state: String? = null
) {
    // Pobierz status z dowolnego dostępnego pola
    private fun getEffectiveStatus(): String? = status ?: listingStatus ?: state
    
    fun getStatusDisplayName(): String {
        val effectiveStatus = getEffectiveStatus()
        return when (effectiveStatus) {
            "DRAFT" -> "Szkic"
            "WAITING" -> "Oczekuje na weryfikację"
            "ACTIVE" -> "Aktywne"
            "REJECTED" -> "Odrzucone"
            "SUSPENDED" -> "Zawieszone"
            "COMPLETED" -> "Zakończone"
            else -> effectiveStatus ?: ""
        }
    }
    
    fun getStatusColor(): StatusColor {
        val effectiveStatus = getEffectiveStatus()
        return when (effectiveStatus) {
            "ACTIVE" -> StatusColor.SUCCESS
            "WAITING" -> StatusColor.WARNING
            "REJECTED" -> StatusColor.ERROR
            "DRAFT" -> StatusColor.NEUTRAL
            "SUSPENDED" -> StatusColor.ERROR
            "COMPLETED" -> StatusColor.NEUTRAL
            else -> StatusColor.NEUTRAL
        }
    }
}

enum class StatusColor {
    SUCCESS, WARNING, ERROR, NEUTRAL
}

data class Seller(
    val id: String?,
    val name: String?
)

data class ProductDetail(
    val publicId: String,
    val title: String?,
    val description: String?,
    val seller: Seller?,
    val category: CategoryInfo?,
    @SerializedName("priceAmount") val priceAmount: Double?,
    val negotiable: Boolean?,
    val media: List<MediaItem>?,
    val attributes: List<ProductAttribute>?,
    val locationCity: String?,
    val locationRegion: String?,
    val status: String?
) {
    // Helper do pobierania URL-i zdjęć
    fun getImageUrls(): List<String> = media?.mapNotNull { it.url } ?: emptyList()
}

data class MediaItem(
    val url: String?,
    val type: String?,
    val position: Int?
)

data class CategoryInfo(
    val id: Int,
    val name: String
)

data class ProductAttribute(
    val key: String?,
    val label: String?,
    val type: String?,
    val stringValue: String?,
    val numberValue: Double?,
    val booleanValue: Boolean?,
    val enumValue: String?,
    val enumLabel: String?
) {
    fun getDisplayValue(): String {
        return when {
            enumLabel != null -> enumLabel
            stringValue != null -> stringValue
            numberValue != null -> numberValue.toString()
            booleanValue != null -> if (booleanValue) "Tak" else "Nie"
            enumValue != null -> enumValue
            else -> "-"
        }
    }
}


// --- ZAPYTANIE DO SERWERA ---

data class ProductSearchRequest(
    val query: String? = null,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("sellerUserId") val sellerUserId: String? = null,
    val attributes: List<AttributeFilter> = emptyList(),
    val sort: List<SortInfo> = emptyList(),
    val page: Int = 0,
    val size: Int = 20
)

data class AttributeFilter(
    val key: String,
    val type: String,
    val op: String,
    val value: String? = null,
    val from: String? = null,
    val to: String? = null,
    val values: List<String>? = null
)

data class SortInfo(
    val field: String,
    val dir: String
)

// Nowe klasy do tworzenia ogłoszeń
data class CreateListingRequest(
    @SerializedName("categoryId") val categoryId: Int,
    val title: String,
    val description: String?,
    @SerializedName("priceAmount") val priceAmount: Double,
    val negotiable: Boolean,
    @SerializedName("locationCity") val locationCity: String?,
    @SerializedName("locationRegion") val locationRegion: String?,
    @SerializedName("mediaUrls") val mediaUrls: List<String>?,
    val attributes: List<CreateAttribute>?
)

data class CreateAttribute(
    val key: String,
    val value: String
)

// --- KONTAKT ---

data class ContactResponse(
    val phoneNumber: String?
)

// --- ULUBIONE ---

data class FavoriteStatusResponse(
    @SerializedName("isFavorite") val isFavorite: Boolean
)

data class FavoriteRequest(
    val entityId: String,
    val entityType: String = "LISTING"
)

// --- RESET HASŁA ---

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

// --- PROFIL UŻYTKOWNIKA ---

data class UserProfileResponse(
    val userId: String,
    val name: String,
    val email: String,
    @SerializedName("isAccountVerified") val isAccountVerified: Boolean,
    val profileImageUrl: String?
)

data class UpdateProfileRequest(
    val name: String? = null,
    val profileImageUrl: String? = null
)
