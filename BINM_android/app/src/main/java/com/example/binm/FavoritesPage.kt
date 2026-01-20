package com.example.binm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.binm.viewmodel.FavoriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesPage(navController: NavController, favoriteViewModel: FavoriteViewModel) {
    val favorites by favoriteViewModel.favoriteProducts.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()
    val isLoading by favoriteViewModel.isLoading.collectAsState()
    val error by favoriteViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        favoriteViewModel.fetchFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Obserwowane") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error ?: "Wystąpił nieznany błąd",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (favorites.isEmpty()) {
                Text(
                    text = "Nie masz jeszcze żadnych obserwowanych ogłoszeń.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites) { product ->
                        ProductCard(
                            product = product,
                            isFavorite = favoriteIds.contains(product.publicId),
                            onFavoriteClick = { favoriteViewModel.toggleFavorite(product.publicId) },
                            onClick = { navController.navigate("product/${product.publicId}") }
                        )
                    }
                }
            }
        }
    }
}
