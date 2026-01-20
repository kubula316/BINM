package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPanelPage(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Użytkownika") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Profil") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate("profile") }
            )
            ListItem(
                headlineContent = { Text("Ustawienia") },
                leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate("settings") }
            )
            ListItem(
                headlineContent = { Text("Moje Ogłoszenia") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate("my_listings") }
            )
        }
    }
}
