package com.example.binm.data

/**
 * Reprezentuje odpowiedź z paginacją dla listy ogłoszeń.
 */
data class PagedListingResponse(
    val content: List<Product>,
    val totalPages: Int,
    val totalElements: Long
)
