package com.example.binm.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {

    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id") // Nowy klucz dla UUID
    }

    suspend fun saveTokenAndUserId(token: String, userId: String) {
        context.dataStore.edit {
            preferences ->
            preferences[JWT_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.clear()
        }
    }

    val tokenFlow: Flow<String?>
        get() = context.dataStore.data.map { preferences -> preferences[JWT_TOKEN_KEY] }

    val userIdFlow: Flow<String?>
        get() = context.dataStore.data.map { preferences -> preferences[USER_ID_KEY] }

    fun isUserLoggedIn(): Boolean {
        return runBlocking {
            val token = tokenFlow.firstOrNull()
            !token.isNullOrEmpty()
        }
    }
}