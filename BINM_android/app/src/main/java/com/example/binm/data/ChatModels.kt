package com.example.binm.data

import com.google.gson.annotations.SerializedName

// --- KONWERSACJE ---

data class Conversation(
    val id: Long,
    val listing: ListingCoverDto?,
    val otherParticipantId: String?,
    val otherParticipantName: String?,
    val lastMessageContent: String?,
    val lastMessageTimestamp: String?,
    val unreadCount: Int = 0
) {
    // Pobierz ID drugiej osoby - z otherParticipantId lub z listing.seller.id
    fun getOtherUserId(): String? = otherParticipantId ?: listing?.seller?.id
}

data class ListingCoverDto(
    val publicId: String?,
    val title: String?,
    val seller: SellerDto?,
    val coverImageUrl: String?
)

data class SellerDto(
    val id: String?,
    val name: String?
)

// --- WIADOMOŚCI ---

data class Message(
    val id: Long?,
    val conversationId: Long?,
    val senderId: String?,
    val senderName: String?,
    val content: String?,
    val timestamp: String?,
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("isOwnMessage") val isOwnMessage: Boolean = false
)

data class MessagePage(
    val content: List<Message>,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val last: Boolean = true,
    val first: Boolean = true
)

// --- WYSYŁANIE WIADOMOŚCI (WebSocket) ---

data class SendMessageRequest(
    val listingId: String,
    val recipientId: String,
    val content: String
)

// --- ODPOWIEDŹ Z WEBSOCKET ---

data class ChatMessageResponse(
    val id: Long?,
    val conversationId: Long?,
    val senderId: String?,
    val senderName: String?,
    val content: String?,
    val timestamp: String?
)
