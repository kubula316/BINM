package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.binm.data.FilterAttribute
import com.example.binm.data.Product
import com.example.binm.viewmodel.CategoryViewModel
import com.example.binm.viewmodel.FavoriteViewModel
import com.example.binm.viewmodel.ProductViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListPage(
    navController: NavController,
    categoryId: Int? = null,
    query: String? = null,
    categoryViewModel: CategoryViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel
) {
    val categoryName by categoryViewModel.categoryName.collectAsState()
    val pageTitle = when {
        query != null -> "Wyniki dla: \"$query\""
        categoryId != null -> categoryName
        else -> "Produkty"
    }

    var searchQuery by remember { mutableStateOf(query ?: "") }
    var showBottomSheet by remember { mutableStateOf(false) }
    val products by productViewModel.products.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState(initial = emptySet())

    LaunchedEffect(key1 = categoryId, key2 = query) {
        if (categoryId != null) {
            categoryViewModel.loadCategoryName(categoryId)
            productViewModel.fetchFilters(categoryId)
            productViewModel.searchProducts(categoryId = categoryId)
        } else if (!query.isNullOrBlank()) {
            productViewModel.searchProducts(query = query)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Szukaj...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = { IconButton(onClick = { productViewModel.searchProducts(query = searchQuery) }) { Icon(Icons.Default.Search, contentDescription = "Szukaj") } }
                )
                if (categoryId != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtruj")
                        Text("Filtry")
                    }
                }
            }

            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak wyników.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
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

    if (showBottomSheet && categoryId != null) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            FilterSheetContent(
                productViewModel = productViewModel,
                onShowResults = {
                    productViewModel.searchProducts(categoryId = categoryId)
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun ProductCard(product: Product, isFavorite: Boolean, onFavoriteClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(contentAlignment = Alignment.TopEnd) {
                SubcomposeAsyncImage(
                    model = product.coverImageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop,
                    loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
                    error = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.BrokenImage, contentDescription = "Błąd ładowania obrazu") } }
                )
                FavoriteIcon(isFavorite = isFavorite, onClick = onFavoriteClick)
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(String.format(Locale.getDefault(), "%.2f zł", product.priceAmount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (product.negotiable) {
                    Text("do negocjacji", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun FilterSheetContent(
    productViewModel: ProductViewModel,
    onShowResults: () -> Unit
) {
    val filters by productViewModel.filters.collectAsState()
    val selectedFilters by productViewModel.selectedFilters.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Filtry", style = MaterialTheme.typography.headlineSmall) }
        items(filters) { filter ->
            Column {
                Text(filter.label, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                when (filter.type) {
                    "ENUM" -> EnumFilter(filter, selectedFilters[filter.key]) { productViewModel.updateSelectedFilter(filter.key, it) }
                    "STRING" -> StringFilter(filter, selectedFilters[filter.key]) { productViewModel.updateSelectedFilter(filter.key, it) }
                    "NUMBER" -> NumberRangeFilter(filter, selectedFilters) { key, value -> productViewModel.updateSelectedFilter(key, value) }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onShowResults, modifier = Modifier.fillMaxWidth()) { Text("Pokaż wyniki") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumFilter(filter: FilterAttribute, selectedValue: String?, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = filter.options.find { it.value == selectedValue }?.label ?: "Wybierz..."

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            filter.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onValueChange(option.value); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun StringFilter(filter: FilterAttribute, value: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value ?: "", onValueChange = onValueChange, label = { Text(filter.label) }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun NumberRangeFilter(filter: FilterAttribute, selectedValues: Map<String, String>, onValueChange: (String, String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val fromKey = "${filter.key}_from"
        val toKey = "${filter.key}_to"

        OutlinedTextField(
            value = selectedValues[fromKey] ?: "",
            onValueChange = { onValueChange(fromKey, it) },
            label = { Text("Od" + (filter.unit?.let { " ($it)" } ?: "")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = selectedValues[toKey] ?: "",
            onValueChange = { onValueChange(toKey, it) },
            label = { Text("Do" + (filter.unit?.let { " ($it)" } ?: "")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
}
