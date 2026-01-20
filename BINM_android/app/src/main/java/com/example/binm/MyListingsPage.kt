package com.example.binm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.binm.data.Product
import com.example.binm.viewmodel.ListingStatusFilter
import com.example.binm.viewmodel.MyListingsViewModel
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsPage(navController: NavController, viewModel: MyListingsViewModel) {
    val myListings by viewModel.myListings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var showDialog by remember { mutableStateOf<String?>(null) }

    // Pobierz dane przy pierwszym załadowaniu ekranu
    LaunchedEffect(Unit) {
        viewModel.fetchMyListings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje Ogłoszenia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Filtry statusów
            StatusFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else if (myListings.isEmpty()) {
                    Text(
                        text = getEmptyMessage(selectedFilter),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myListings) { product ->
                            MyListingItem(
                                product = product,
                                selectedFilter = selectedFilter,
                                onEdit = { navController.navigate("edit_offer/${product.publicId}") },
                                onDelete = { showDialog = product.publicId },
                                onClick = { navController.navigate("product/${product.publicId}") },
                                onSubmitForApproval = { viewModel.submitForApproval(product.publicId) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog potwierdzający usunięcie
    showDialog?.let {
        publicId ->
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text("Potwierdź usunięcie") },
            text = { Text("Czy na pewno chcesz usunąć to ogłoszenie? Tej operacji nie można cofnąć.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteListing(publicId)
                        showDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = null }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
private fun StatusFilterChips(
    selectedFilter: ListingStatusFilter,
    onFilterSelected: (ListingStatusFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ListingStatusFilter.entries.toList()) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

private fun getEmptyMessage(filter: ListingStatusFilter): String {
    return when (filter) {
        ListingStatusFilter.ALL -> "Nie masz jeszcze żadnych ogłoszeń."
        ListingStatusFilter.ACTIVE -> "Brak aktywnych ogłoszeń."
        ListingStatusFilter.WAITING -> "Brak ogłoszeń oczekujących na weryfikację."
        ListingStatusFilter.DRAFT -> "Brak szkiców ogłoszeń."
        ListingStatusFilter.REJECTED -> "Brak odrzuconych ogłoszeń."
        ListingStatusFilter.COMPLETED -> "Brak zakończonych ogłoszeń."
    }
}

@Composable
private fun MyListingItem(
    product: Product, 
    selectedFilter: ListingStatusFilter,
    onEdit: () -> Unit, 
    onDelete: () -> Unit, 
    onClick: () -> Unit,
    onSubmitForApproval: () -> Unit
) {
    // Gdy filtr jest DRAFT, wszystkie ogłoszenia w liście są szkicami
    val isDraft = selectedFilter == ListingStatusFilter.DRAFT
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                SubcomposeAsyncImage(
                    model = product.coverImageUrl,
                    contentDescription = product.title,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) } },
                    error = { Box(modifier = Modifier.fillMaxSize().background(Color.LightGray, RoundedCornerShape(8.dp))) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f zł", product.priceAmount), 
                        style = MaterialTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            // Przycisk "Wyślij do weryfikacji" dla szkiców
            if (isDraft) {
                Button(
                    onClick = onSubmitForApproval,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, 
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wyślij do weryfikacji")
                }
            }
        }
    }
}
