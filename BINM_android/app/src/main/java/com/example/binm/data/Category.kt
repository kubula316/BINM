package com.example.binm.data

data class Category(
    val id: Int,
    val parentId: Int?,
    val name: String,
    val imageUrl: String?,
    val sortOrder: Int,
    val depth: Int,
    val isLeaf: Boolean,
    val children: List<Category> // Rekurencyjna struktura dla zagnieżdżonych kategorii
)
