package com.example.binm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.binm.data.Product
import com.example.binm.viewmodel.CategoryViewModel
import com.example.binm.viewmodel.FavoriteViewModel
import com.example.binm.viewmodel.ListingViewModel
import com.example.binm.viewmodel.ListingsUiState
import com.example.binm.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomePage(
    navController: NavController,
    categoryViewModel: CategoryViewModel = viewModel(),
    listingViewModel: ListingViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel,
    profileViewModel: ProfileViewModel? = null
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val allCategories by categoryViewModel.allCategories.collectAsState()
    val topLevelCategories = allCategories.filter { it.depth == 0 }
    val listingsState by listingViewModel.uiState.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState(initial = emptySet())
    val profileImageUrl = profileViewModel?.profileImageUrl?.collectAsState()?.value

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController, categoryViewModel) { 
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Icon(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo") } },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menu") } },
                    actions = { 
                        IconButton(onClick = { navController.navigate("user_panel") }) { 
                            if (profileImageUrl != null) {
                                SubcomposeAsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = "Profil",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    loading = { Icon(painter = painterResource(id = R.drawable.user_icon), contentDescription = "Profil") },
                                    error = { Icon(painter = painterResource(id = R.drawable.user_icon), contentDescription = "Profil") }
                                )
                            } else {
                                Icon(painter = painterResource(id = R.drawable.user_icon), contentDescription = "Profil")
                            }
                        } 
                    }
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(vertical = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    label = { Text("Szukaj przedmiotów...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    trailingIcon = { IconButton(onClick = { if (searchQuery.isNotBlank()) { navController.navigate("productlist/search/$searchQuery") } }) { Icon(Icons.Default.Search, contentDescription = "Szukaj") } }
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text("PRZEGLĄDAJ KATEGORIE BINM", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2), modifier = Modifier.height(220.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(topLevelCategories) { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp).clickable { navController.navigate("productlist/category/${category.id}") }
                        ) {
                            SubcomposeAsyncImage(
                                model = category.imageUrl,
                                contentDescription = category.name,
                                modifier = Modifier.size(70.dp),
                                contentScale = ContentScale.Crop,
                                loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
                                error = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Brak zdjęcia") } }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(category.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("POLECANE OGŁOSZENIA", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                when (val state = listingsState) {
                    is ListingsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    is ListingsUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                    }
                    is ListingsUiState.Success -> {
                        if (state.listings.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { state.listings.size })
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HorizontalPager(
                                    state = pagerState,
                                    contentPadding = PaddingValues(horizontal = 32.dp), // Podgląd sąsiednich elementów
                                    pageSpacing = 16.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) { page ->
                                    ListingItem(
                                        listing = state.listings[page],
                                        navController = navController,
                                        isFavorite = favoriteIds.contains(state.listings[page].publicId),
                                        onFavoriteClick = { favoriteViewModel.toggleFavorite(state.listings[page].publicId) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                // Wskaźniki strony
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(pagerState.pageCount) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        Box(
                                            modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingItem(
    listing: Product,
    navController: NavController,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("product/${listing.publicId}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(contentAlignment = Alignment.TopEnd) {
                SubcomposeAsyncImage(
                    model = listing.coverImageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop,
                    loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
                    error = { Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) { Text("Brak zdjęcia") } }
                )
                FavoriteIcon(isFavorite = isFavorite, onClick = onFavoriteClick)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(listing.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(String.format(Locale.getDefault(), "%.2f zł", listing.priceAmount), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FavoriteIcon(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (isFavorite) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Ulubione",
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp) // Border
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null, // decorative
                    tint = Color.Yellow,
                    modifier = Modifier.size(24.dp) // Fill
                )
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.StarBorder,
                contentDescription = "Ulubione",
                tint = Color.White
            )
        }
    }
}
