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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.binm.ui.components.MessageBubble
import com.example.binm.ui.components.MessageInput
import com.example.binm.viewmodel.ChatViewModel
import com.example.binm.viewmodel.MessagesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationPage(
    navController: NavController,
    conversationId: Long,
    chatViewModel: ChatViewModel
) {
    val messagesState by chatViewModel.messagesState.collectAsState()
    val currentConversation by chatViewModel.currentConversation.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val effectiveListingId = currentConversation?.listing?.publicId ?: ""
    val effectiveRecipientId = currentConversation?.getOtherUserId() ?: ""

    LaunchedEffect(conversationId) {
        if (conversationId > 0) {
            chatViewModel.loadConversation(conversationId)
        }
    }

    // Przewiń do ostatniej wiadomości
    LaunchedEffect(messagesState) {
        if (messagesState is MessagesUiState.Success) {
            val messages = (messagesState as MessagesUiState.Success).messages
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
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
                        currentConversation?.listing?.coverImageUrl?.let {
                            SubcomposeAsyncImage(
                                model = it,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(
                                text = currentConversation?.otherParticipantName ?: "Rozmowa",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            currentConversation?.listing?.title?.let {
                                Text(
                                    text = it, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
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
                when (val state = messagesState) {
                    is MessagesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is MessagesUiState.Error -> {
                        Text(
                            text = state.message, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    is MessagesUiState.Success -> {
                        if (state.messages.isEmpty()) {
                            Text(
                                text = "Rozpocznij rozmowę",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.messages) { message ->
                                    MessageBubble(message = message)
                                }
                            }
                        }
                    }
                }
            }
            MessageInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank() && effectiveListingId.isNotBlank() && effectiveRecipientId.isNotBlank()) {
                        chatViewModel.sendMessage(effectiveListingId, effectiveRecipientId, messageText)
                        messageText = ""
                    }
                }
            )
        }
    }
}
