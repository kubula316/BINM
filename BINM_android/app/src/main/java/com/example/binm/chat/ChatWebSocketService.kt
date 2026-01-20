package com.example.binm.chat

import android.util.Log
import com.example.binm.data.ChatMessageResponse
import com.example.binm.data.SendMessageRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import com.example.binm.BuildConfig

class ChatWebSocketService {
    
    companion object {
        private const val TAG = "ChatWebSocketService"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    
    private var webSocket: WebSocket? = null
    private var token: String? = null
    private var isConnected = false
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _incomingMessages = MutableSharedFlow<ChatMessageResponse>()
    val incomingMessages: SharedFlow<ChatMessageResponse> = _incomingMessages.asSharedFlow()
    
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()
    
    fun connect(jwtToken: String) {
        if (isConnected) {
            Log.d(TAG, "Already connected")
            return
        }
        
        val wsUrl = BuildConfig.WS_BASE_URL
        Log.d(TAG, "Connecting to WebSocket: $wsUrl")
        token = jwtToken
        _connectionState.value = ConnectionState.CONNECTING
        
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $jwtToken")
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true
                _connectionState.value = ConnectionState.CONNECTED
                
                // Wyślij STOMP CONNECT frame
                val connectFrame = buildStompConnectFrame(jwtToken)
                webSocket.send(connectFrame)
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: $text")
                handleStompMessage(text)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                isConnected = false
                _connectionState.value = ConnectionState.DISCONNECTED
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                Log.e(TAG, "Response: ${response?.code} - ${response?.message}")
                isConnected = false
                _connectionState.value = ConnectionState.ERROR
            }
        })
    }
    
    fun disconnect() {
        webSocket?.let {
            val disconnectFrame = "DISCONNECT\n\n\u0000"
            it.send(disconnectFrame)
            it.close(1000, "User disconnect")
        }
        webSocket = null
        isConnected = false
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    fun sendMessage(listingId: String, recipientId: String, content: String) {
        if (!isConnected) {
            Log.e(TAG, "Cannot send message - not connected")
            return
        }
        
        val messageRequest = SendMessageRequest(
            listingId = listingId,
            recipientId = recipientId,
            content = content
        )
        
        val messageJson = gson.toJson(messageRequest)
        val sendFrame = buildStompSendFrame("/app/chat.sendMessage", messageJson)
        
        Log.d(TAG, "Sending message: $sendFrame")
        webSocket?.send(sendFrame)
    }
    
    private fun buildStompConnectFrame(token: String): String {
        return buildString {
            append("CONNECT\n")
            append("accept-version:1.1,1.2\n")
            append("heart-beat:10000,10000\n")
            append("Authorization:Bearer $token\n")
            append("\n")
            append("\u0000")
        }
    }
    
    private fun buildStompSendFrame(destination: String, body: String): String {
        return buildString {
            append("SEND\n")
            append("destination:$destination\n")
            append("content-type:application/json\n")
            append("\n")
            append(body)
            append("\u0000")
        }
    }
    
    private fun buildStompSubscribeFrame(destination: String, id: String): String {
        return buildString {
            append("SUBSCRIBE\n")
            append("id:$id\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }
    }
    
    private fun handleStompMessage(rawMessage: String) {
        val lines = rawMessage.split("\n")
        if (lines.isEmpty()) return
        
        when {
            rawMessage.startsWith("CONNECTED") -> {
                Log.d(TAG, "STOMP connected, subscribing to messages...")
                // Subskrybuj kanał wiadomości
                val subscribeFrame = buildStompSubscribeFrame("/user/queue/messages", "sub-0")
                webSocket?.send(subscribeFrame)
            }
            rawMessage.startsWith("MESSAGE") -> {
                // Parsuj wiadomość STOMP
                val bodyStart = rawMessage.indexOf("\n\n")
                if (bodyStart != -1) {
                    val body = rawMessage.substring(bodyStart + 2).trimEnd('\u0000')
                    try {
                        val message = gson.fromJson(body, ChatMessageResponse::class.java)
                        scope.launch {
                            _incomingMessages.emit(message)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse message: ${e.message}")
                    }
                }
            }
            rawMessage.startsWith("ERROR") -> {
                Log.e(TAG, "STOMP error: $rawMessage")
            }
        }
    }
    
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }
}
