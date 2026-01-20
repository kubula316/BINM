package com.example.binm

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.binm.data.FilterAttribute
import com.example.binm.viewmodel.EditListingUiState
import com.example.binm.viewmodel.EditOfferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOfferPage(navController: NavController, publicId: String, viewModel: EditOfferViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(publicId) {
        viewModel.loadListingData(publicId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj Ogłoszenie") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is EditListingUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ładowanie...")
                    }
                }
                is EditListingUiState.Uploading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Wysyłanie zdjęcia ${state.current} z ${state.total}...")
                    }
                }
                is EditListingUiState.Success -> {
                    SuccessScreen(message = "Zmiany zostały zapisane!") {
                        navController.popBackStack()
                    }
                }
                is EditListingUiState.Error -> {
                    ErrorScreen(message = state.message) { 
                        navController.popBackStack()
                    }
                }
                is EditListingUiState.Idle -> {
                    EditOfferForm(
                        viewModel = viewModel, 
                        onSubmit = { viewModel.updateListing(publicId, context) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOfferForm(viewModel: EditOfferViewModel, onSubmit: () -> Unit) {
    val attributes by viewModel.attributes.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris -> 
            uris.forEach { uri -> viewModel.addImage(uri.toString()) }
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sekcja zdjęć
        item {
            Text("Zdjęcia", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Dodaj zdjęcia", tint = MaterialTheme.colorScheme.primary)
                            Text("Dodaj", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                items(viewModel.images.size) { index ->
                    val imageItem = viewModel.images[index]
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = imageItem.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Wskaźnik czy zdjęcie jest nowe (lokalne)
                        if (imageItem.isLocal) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Nowe",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.removeImage(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Usuń zdjęcie", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            if (viewModel.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val serverCount = viewModel.images.count { !it.isLocal }
                val localCount = viewModel.images.count { it.isLocal }
                Text(
                    buildString {
                        append("${viewModel.images.size} zdjęć")
                        if (localCount > 0) append(" ($localCount nowych)")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item { 
            OutlinedTextField(
                value = viewModel.title.value, 
                onValueChange = { viewModel.title.value = it }, 
                label = { Text("Tytuł Ogłoszenia") }, 
                modifier = Modifier.fillMaxWidth()
            ) 
        }
        item { 
            OutlinedTextField(
                value = viewModel.description.value, 
                onValueChange = { viewModel.description.value = it }, 
                label = { Text("Opis") }, 
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) 
        }
        item { 
            OutlinedTextField(
                value = viewModel.price.value, 
                onValueChange = { viewModel.price.value = it }, 
                label = { Text("Cena (zł)") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            ) 
        }
        item { 
            OutlinedTextField(
                value = viewModel.locationCity.value, 
                onValueChange = { viewModel.locationCity.value = it }, 
                label = { Text("Lokalizacja (Miasto)") }, 
                modifier = Modifier.fillMaxWidth()
            ) 
        }

        items(attributes.size) { index ->
            val attribute = attributes[index]
            val value = viewModel.attributeValues[attribute.key] ?: ""
            when (attribute.type) {
                "ENUM" -> EnumAttribute(attribute, value) { viewModel.attributeValues[attribute.key] = it }
                "STRING" -> StringAttribute(attribute, value) { viewModel.attributeValues[attribute.key] = it }
                "NUMBER" -> NumberAttribute(attribute, value) { viewModel.attributeValues[attribute.key] = it }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = viewModel.negotiable.value, onCheckedChange = { viewModel.negotiable.value = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Do negocjacji")
            }
        }

        item {
            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                Text("Zapisz zmiany")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumAttribute(attribute: FilterAttribute, value: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = attribute.options.find { it.value == value }?.label ?: "Wybierz..."
    
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(attribute.label) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            attribute.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { 
                        onValueChange(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StringAttribute(attribute: FilterAttribute, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, 
        onValueChange = onValueChange, 
        label = { Text(attribute.label) }, 
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberAttribute(attribute: FilterAttribute, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, 
        onValueChange = onValueChange, 
        label = { Text(attribute.label) }, 
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun SuccessScreen(message: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), 
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onDismiss) { Text("OK") }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), 
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Rozumiem") }
    }
}
