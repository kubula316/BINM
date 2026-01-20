package com.example.binm

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.binm.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryPage(
    navController: NavController,
    parentId: Int,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val subCategories by categoryViewModel.subCategories.collectAsState()
    val parentCategoryName by categoryViewModel.categoryName.collectAsState()

    LaunchedEffect(parentId) {
        categoryViewModel.loadSubCategories(parentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(parentCategoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        if (subCategories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak podkategorii lub wczytywanie produktów...", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(subCategories) { category ->
                    ListItem(
                        headlineContent = { Text(category.name, style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.clickable {
                            if (category.isLeaf) {
                                navController.navigate("productlist/${category.id}")
                            } else {
                                navController.navigate("subcategory/${category.id}")
                            }
                        },
                        trailingContent = {
                            if (!category.isLeaf) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Przejdź dalej")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
