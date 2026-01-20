package com.example.binm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.binm.api.RetrofitInstance
import com.example.binm.chat.ChatWebSocketService
import com.example.binm.data.Conversation
import com.example.binm.data.Message
import com.example.binm.manager.AuthManager
import com.example.binm.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface ConversationsUiState {
    object Loading : ConversationsUiState
    data class Success(val conversations: List<Conversation>) : ConversationsUiState
    data class Error(val message: String) : ConversationsUiState
    object Empty : ConversationsUiState
}

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val messages: List<Message>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}

class ChatViewModel(private val sessionManager: SessionManager) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val webSocketService = ChatWebSocketService()

    private val _conversationsState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val conversationsState: StateFlow<ConversationsUiState> = _conversationsState.asStateFlow()

    private val _messagesState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val messagesState: StateFlow<MessagesUiState> = _messagesState.asStateFlow()

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    val connectionState = webSocketService.connectionState

    init {
        viewModelScope.launch {
            sessionManager.tokenFlow.collect { token ->
                if (!token.isNullOrBlank()) {
                    webSocketService.connect(token)
                } else {
                    webSocketService.disconnect()
                }
            }
        }

        viewModelScope.launch {
            webSocketService.incomingMessages.collect { chatMessage ->
                if (chatMessage.conversationId != _currentConversation.value?.id) return@collect

                val myUserId = sessionManager.userIdFlow.firstOrNull()
                val isOwn = chatMessage.senderId == myUserId
                val currentState = _messagesState.value

                if (currentState is MessagesUiState.Success) {
                    val currentMessages = currentState.messages.toMutableList()
                    val tempMessageIndex = currentMessages.indexOfFirst { it.id == null && it.isOwnMessage && it.content == chatMessage.content }

                    if (isOwn && tempMessageIndex != -1) {
                        val confirmedMessage = currentMessages[tempMessageIndex].copy(id = chatMessage.id, timestamp = chatMessage.timestamp)
                        currentMessages[tempMessageIndex] = confirmedMessage
                        _messagesState.value = MessagesUiState.Success(currentMessages)
                    } else if (!currentMessages.any { it.id == chatMessage.id }) {
                        val newMessage = Message(
                            id = chatMessage.id, conversationId = chatMessage.conversationId,
                            senderId = chatMessage.senderId, senderName = chatMessage.senderName,
                            content = chatMessage.content, timestamp = chatMessage.timestamp,
                            isRead = false, isOwnMessage = isOwn
                        )
                        _messagesState.value = MessagesUiState.Success(currentMessages + newMessage)
                    }
                }
                fetchConversations()
            }
        }
    }

    fun fetchConversations() {
        viewModelScope.launch {
            val token = sessionManager.tokenFlow.firstOrNull() ?: return@launch
            try {
                val conversations = RetrofitInstance.api.getConversations("Bearer $token")
                _conversationsState.value = if (conversations.isEmpty()) ConversationsUiState.Empty else ConversationsUiState.Success(conversations.distinctBy { it.id })
            } catch (e: HttpException) {
                if (e.code() == 401) AuthManager.onAuthRequired()
                _conversationsState.value = ConversationsUiState.Error("Błąd serwera: ${e.code()}")
            } catch (e: Exception) {
                _conversationsState.value = ConversationsUiState.Error("Błąd pobierania konwersacji")
            }
        }
    }

    fun loadConversation(conversationId: Long) {
        viewModelScope.launch {
            _messagesState.value = MessagesUiState.Loading
            val token = sessionManager.tokenFlow.firstOrNull()
            val myUserId = sessionManager.userIdFlow.firstOrNull()

            if (token == null || myUserId == null) {
                _messagesState.value = MessagesUiState.Error("Brak autoryzacji")
                return@launch
            }

            try {
                val conversation = RetrofitInstance.api.getConversations("Bearer $token").find { it.id == conversationId }
                _currentConversation.value = conversation

                if (conversation == null) {
                    _messagesState.value = MessagesUiState.Error("Nie znaleziono konwersacji")
                    return@launch
                }

                val messagesPage = RetrofitInstance.api.getMessages("Bearer $token", conversationId)
                val messagesWithOwnership = messagesPage.content.map { it.copy(isOwnMessage = it.senderId == myUserId) }.reversed()

                _messagesState.value = MessagesUiState.Success(messagesWithOwnership)
                RetrofitInstance.api.markConversationAsRead("Bearer $token", conversationId)

            } catch (e: HttpException) {
                if (e.code() == 401) AuthManager.onAuthRequired()
                _messagesState.value = MessagesUiState.Error("Błąd serwera: ${e.code()}")
            } catch (e: Exception) {
                _messagesState.value = MessagesUiState.Error("Błąd wczytywania wiadomości")
            }
        }
    }

    fun sendMessage(listingId: String, recipientId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val myUserId = sessionManager.userIdFlow.firstOrNull() ?: return@launch
            val currentState = _messagesState.value
            
            // Obsługa zarówno Success jak i Loading (dla nowych konwersacji)
            val currentMessages = when (currentState) {
                is MessagesUiState.Success -> currentState.messages
                else -> emptyList()
            }
            
            val tempMessage = Message(
                id = null, conversationId = _currentConversation.value?.id,
                senderId = myUserId, senderName = "Ty", content = content,
                timestamp = java.time.Instant.now().toString(),
                isRead = true, isOwnMessage = true
            )
            _messagesState.value = MessagesUiState.Success(currentMessages + tempMessage)
            webSocketService.sendMessage(listingId, recipientId, content)
        }
    }
    
    // Znajdź lub utwórz konwersację dla danego ogłoszenia i sprzedawcy
    suspend fun findOrCreateConversation(listingId: String, sellerId: String): Long? {
        val token = sessionManager.tokenFlow.firstOrNull() ?: return null
        return try {
            val conversations = RetrofitInstance.api.getConversations("Bearer $token")
            val existing = conversations.find { 
                it.listing?.publicId == listingId && it.otherParticipantId == sellerId 
            }
            existing?.id
        } catch (e: Exception) {
            Log.e(TAG, "Error finding conversation: ${e.message}")
            null
        }
    }

    fun clearCurrentConversation() {
        _currentConversation.value = null
        _messagesState.value = MessagesUiState.Loading
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}

class ChatViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
