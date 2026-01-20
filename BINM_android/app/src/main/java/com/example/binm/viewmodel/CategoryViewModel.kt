package com.example.binm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories.asStateFlow()

    private val _subCategories = MutableStateFlow<List<Category>>(emptyList())
    val subCategories: StateFlow<List<Category>> = _subCategories.asStateFlow()

    private val _categoryName = MutableStateFlow("Kategoria")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                _allCategories.value = RetrofitInstance.api.getCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSubCategories(parentId: Int) {
        val parent = findInCategoryTree(parentId, _allCategories.value)
        _subCategories.value = parent?.children ?: emptyList()
        _categoryName.value = parent?.name ?: "Kategoria"
    }

    fun loadCategoryName(categoryId: Int) {
        val category = findInCategoryTree(categoryId, _allCategories.value)
        _categoryName.value = category?.name ?: "Produkty"
    }

    private fun findInCategoryTree(id: Int, categories: List<Category>): Category? {
        for (category in categories) {
            if (category.id == id) {
                return category
            }
            val foundInChildren = findInCategoryTree(id, category.children)
            if (foundInChildren != null) {
                return foundInChildren
            }
        }
        return null
    }
}
