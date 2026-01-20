package com.example.binm

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.ProductDetail
import com.example.binm.viewmodel.ChatViewModel
import com.example.binm.viewmodel.ChatViewModelFactory
import com.example.binm.viewmodel.FavoriteViewModel
import com.example.binm.viewmodel.ProductDetailUiState
import com.example.binm.viewmodel.ProductViewModel
import com.example.binm.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailPage(
    navController: NavController,
    productId: String,
    productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(MainApplication.sessionManager)),
    favoriteViewModel: FavoriteViewModel,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(MainApplication.sessionManager))
) {
    val uiState by productViewModel.productDetailState.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState(initial = emptySet())
    val isFavorite = favoriteIds.contains(productId)

    LaunchedEffect(productId) {
        productViewModel.fetchProductDetails(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val title = when (val state = uiState) {
                        is ProductDetailUiState.Success -> state.product.title ?: "Szczegóły"
                        is ProductDetailUiState.Error -> "Błąd"
                        else -> "Ładowanie..."
                    }
                    Text(title, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { favoriteViewModel.toggleFavorite(productId) }) {
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
                                imageVector = Icons.Filled.StarBorder,
                                contentDescription = "Ulubione",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is ProductDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.isNotActive) {
                            // Komunikat dla nieaktywnych ogłoszeń
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Ogłoszenie niedostępne",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Wróć")
                            }
                        } else {
                            // Standardowy komunikat błędu
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { productViewModel.fetchProductDetails(productId) }) {
                                Text("Spróbuj ponownie")
                            }
                        }
                    }
                }
                is ProductDetailUiState.Success -> {
                    ProductDetailContent(
                        detail = state.product,
                        navController = navController,
                        chatViewModel = chatViewModel,
                        isFavorite = isFavorite,
                        onFavoriteClick = { favoriteViewModel.toggleFavorite(productId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductDetailContent(
    detail: ProductDetail,
    navController: NavController,
    chatViewModel: ChatViewModel,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var isLoadingPhone by remember { mutableStateOf(false) }
    var showPhoneError by remember { mutableStateOf(false) }
    var isNavigatingToChat by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Galeria zdjęć
        item {
            val images = detail.getImageUrls()
            if (images.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { images.size })
                Box(contentAlignment = Alignment.BottomCenter) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    ) { page ->
                        SubcomposeAsyncImage(
                            model = images[page],
                            contentDescription = "Zdjęcie ${page + 1} z ${images.size}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = { 
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator() 
                                }
                            },
                            error = { 
                                Box(
                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) { 
                                    Text("Brak zdjęcia", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                                }
                            }
                        )
                    }
                    // Wskaźniki strony
                    if (images.size > 1) {
                        Row(
                            Modifier
                                .padding(bottom = 12.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(pagerState.pageCount) { iteration ->
                                val color = if (pagerState.currentPage == iteration) 
                                    Color.White 
                                else 
                                    Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                    // Licznik zdjęć
                    if (images.size > 1) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { 
                    Text("Brak zdjęć", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Cena i tytuł
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Cena
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    detail.priceAmount?.let { 
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f zł", it),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (detail.negotiable == true) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Do negocjacji",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tytuł
                detail.title?.let { 
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Lokalizacja
                if (detail.locationCity != null || detail.locationRegion != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listOfNotNull(detail.locationCity, detail.locationRegion).joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Kategoria
                detail.category?.name?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Przyciski akcji
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Przycisk "Pokaż numer"
                OutlinedButton(
                    onClick = {
                        if (phoneNumber != null) {
                            // Numer już pobrany, można go skopiować
                            Toast.makeText(context, "Numer: $phoneNumber", Toast.LENGTH_LONG).show()
                        } else {
                            // Pobierz numer
                            scope.launch {
                                isLoadingPhone = true
                                showPhoneError = false
                                try {
                                    val token = MainApplication.sessionManager.tokenFlow.first()
                                    if (token.isNullOrBlank()) {
                                        Toast.makeText(context, "Zaloguj się, aby zobaczyć numer", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val response = RetrofitInstance.api.getListingContact("Bearer $token", detail.publicId)
                                        phoneNumber = response.phoneNumber
                                        if (phoneNumber != null) {
                                            Toast.makeText(context, "Numer: $phoneNumber", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Brak numeru telefonu", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    showPhoneError = true
                                    Toast.makeText(context, "Zaloguj się, aby zobaczyć numer", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoadingPhone = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoadingPhone
                ) {
                    if (isLoadingPhone) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (phoneNumber != null) phoneNumber!! else "Pokaż numer")
                    }
                }
                
                // Przycisk "Napisz"
                Button(
                    onClick = {
                        val sellerId = detail.seller?.id ?: ""
                        val sellerName = detail.seller?.name ?: "Sprzedawca"
                        val listingTitle = detail.title ?: "Ogłoszenie"
                        val listingId = detail.publicId
                        
                        if (sellerId.isNotBlank() && !isNavigatingToChat) {
                            isNavigatingToChat = true
                            scope.launch {
                                try {
                                    // Sprawdź czy istnieje już konwersacja
                                    val existingConversationId = chatViewModel.findOrCreateConversation(listingId, sellerId)
                                    
                                    if (existingConversationId != null) {
                                        // Przekieruj do istniejącej konwersacji
                                        navController.navigate("conversation/$existingConversationId")
                                    } else {
                                        // Przekieruj do nowej konwersacji
                                        val encodedTitle = URLEncoder.encode(listingTitle, StandardCharsets.UTF_8.toString())
                                        val encodedSellerName = URLEncoder.encode(sellerName, StandardCharsets.UTF_8.toString())
                                        navController.navigate("new_conversation/$listingId/$encodedTitle/$sellerId/$encodedSellerName")
                                    }
                                } finally {
                                    isNavigatingToChat = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = detail.seller?.id != null && !isNavigatingToChat
                ) {
                    if (isNavigatingToChat) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Napisz")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Informacje o sprzedającym
        item {
            detail.seller?.let { seller ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sprzedający",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = seller.name ?: "Nieznany",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        
        // Opis
        item {
            detail.description?.let { description ->
                if (description.isNotBlank()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Opis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        // Atrybuty (tabela)
        if (!detail.attributes.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Szczegóły",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            items(detail.attributes) { attribute ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = attribute.label ?: attribute.key ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = attribute.getDisplayValue(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // Dodatkowa przestrzeń na dole
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
