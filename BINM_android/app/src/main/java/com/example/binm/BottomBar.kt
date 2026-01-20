package com.example.binm

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Główna") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Obserwowane") },
            selected = false,
            onClick = { navController.navigate("favorites") }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Dodaj") },
            selected = false,
            onClick = { navController.navigate("add") }
        )
        NavigationBarItem(
            icon = {
                BadgedBox(badge = { /* Badge powiadomień */ }) {
                    Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Wiadomości")
                }
            },
            selected = false,
            onClick = { navController.navigate("chat") } // Poprawiona nawigacja
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = "Pomoc") },
            selected = false,
            onClick = { navController.navigate("help") }
        )
    }
}
