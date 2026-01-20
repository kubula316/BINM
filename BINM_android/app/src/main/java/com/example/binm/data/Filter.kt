package com.example.binm.data

// Reprezentuje pojedynczy atrybut/filtr, np. "Stan", "Marka", "Rocznik"
data class FilterAttribute(
    val id: Int,
    val categoryId: Int,
    val key: String,       // np. "condition"
    val label: String,     // np. "Stan"
    val type: String,      // np. "ENUM", "STRING", "NUMBER"
    val required: Boolean,
    val unit: String?,
    val sortOrder: Int,
    val options: List<FilterOption>
)

// Reprezentuje pojedynczą opcję w filtrze typu ENUM, np. "Nowy", "Używany"
data class FilterOption(
    val id: Int,
    val value: String,
    val label: String,
    val sortOrder: Int
)
