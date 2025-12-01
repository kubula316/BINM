package com.example.binm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.binm.ui.theme.BINMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            BINMTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") { HomePage(navController) }
                    composable("login") { LoginPage(navController) }
                    composable("register") { RegisterPage(navController) }
                    composable("favorites") { FavoritesPage(navController) }
                    composable("add") { AddOfferPage(navController) }
                    composable("cart") { CartPage(navController) }
                    composable("help") { HelpPage(navController) }
                }
            }
        }
    }
}