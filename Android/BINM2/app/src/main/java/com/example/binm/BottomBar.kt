package com.example.binm

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.home),
                    contentDescription = "Strona główna"
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("favorites") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.favorite),
                    contentDescription = "Obserwowane",
                    modifier = Modifier.size(50.dp)
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("add") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.add),
                    contentDescription = "Dodaj",
                    modifier = Modifier.size(50.dp)
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("cart") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.cart),
                    contentDescription = "Koszyk",
                    modifier = Modifier.size(200.dp)
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("help") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.help),
                    contentDescription = "Pomoc",
                    modifier = Modifier.size(50.dp)
                )
            }
        )
    }
}
