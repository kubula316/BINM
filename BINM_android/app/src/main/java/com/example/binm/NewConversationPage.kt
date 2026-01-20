package com.example.binm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.binm.ui.components.MessageBubble
import com.example.binm.ui.components.MessageInput
import com.example.binm.viewmodel.ChatViewModel
import com.example.binm.viewmodel.ConversationsUiState
import com.example.binm.viewmodel.MessagesUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationPage(
    navController: NavController,
    chatViewModel: ChatViewModel,
    listingId: String,
    listingTitle: String,
    listingImageUrl: String?,
    sellerId: String,
    sellerName: String
) {
    var messageText by remember { mutableStateOf("") }
    var messageSentOnce by remember { mutableStateOf(false) }
    var waitingForConversation by remember { mutableStateOf(false) }
    val messagesState by chatViewModel.messagesState.collectAsState()
    val conversationsState by chatViewModel.conversationsState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val messages = (messagesState as? MessagesUiState.Success)?.messages ?: emptyList()

    // Przewiń do ostatniej wiadomości
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Po wysłaniu wiadomości periodycznie sprawdzaj czy konwersacja została utworzona
    LaunchedEffect(messageSentOnce, waitingForConversation) {
        if (messageSentOnce && waitingForConversation) {
            // Daj serwerowi chwilę na utworzenie konwersacji
            kotlinx.coroutines.delay(1000)
            chatViewModel.fetchConversations()
        }
    }

    // Nawiguj gdy konwersacja zostanie znaleziona
    LaunchedEffect(conversationsState, messageSentOnce) {
        if (messageSentOnce && conversationsState is ConversationsUiState.Success) {
            val conversations = (conversationsState as ConversationsUiState.Success).conversations
            val newConversation = conversations.find { 
                it.listing?.publicId == listingId && it.otherParticipantId == sellerId 
            }
            if (newConversation != null) {
                waitingForConversation = false
                navController.navigate("conversation/${newConversation.id}") {
                    popUpTo("chat") { inclusive = false }
                    launchSingleTop = true
                }
            } else if (waitingForConversation) {
                // Spróbuj ponownie po chwili
                kotlinx.coroutines.delay(1500)
                chatViewModel.fetchConversations()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.clearCurrentConversation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listingImageUrl?.let {
                            SubcomposeAsyncImage(
                                model = it, contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(text = sellerName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = listingTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    InitialNewConversationView(listingImageUrl, listingTitle, sellerName) { messageText = it }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(message = message)
                        }
                    }
                }
            }
            MessageInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        chatViewModel.sendMessage(listingId, sellerId, messageText)
                        messageSentOnce = true
                        waitingForConversation = true
                        messageText = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun InitialNewConversationView(
    listingImageUrl: String?,
    listingTitle: String,
    sellerName: String,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        listingImageUrl?.let {
            SubcomposeAsyncImage(
                model = it, contentDescription = null,
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(text = listingTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Napisz do: $sellerName", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Możesz napisać np.:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))
        SuggestionChip(onClick = { onSuggestionClick("Cześć! Czy ten przedmiot jest jeszcze dostępny?") }, label = { Text("Czy przedmiot jest dostępny?") })
        Spacer(modifier = Modifier.height(8.dp))
        SuggestionChip(onClick = { onSuggestionClick("Dzień dobry, interesuje mnie ta oferta. Czy możliwa jest negocjacja ceny?") }, label = { Text("Pytanie o cenę") })
    }
}
