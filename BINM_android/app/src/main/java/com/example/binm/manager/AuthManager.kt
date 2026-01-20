package com.example.binm.manager

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthManager {
    private val _authRequired = MutableSharedFlow<Boolean>(replay = 1)
    val authRequired = _authRequired.asSharedFlow()

    suspend fun onAuthRequired() {
        _authRequired.emit(true)
    }
}