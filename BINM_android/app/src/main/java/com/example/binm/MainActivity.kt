package com.example.binm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.binm.manager.AuthManager
import com.example.binm.ui.theme.BINMTheme
import com.example.binm.viewmodel.AddOfferViewModel
import com.example.binm.viewmodel.AddOfferViewModelFactory
import com.example.binm.viewmodel.AuthViewModel
import com.example.binm.viewmodel.AuthViewModelFactory
import com.example.binm.viewmodel.CategoryViewModel
import com.example.binm.viewmodel.EditOfferViewModel
import com.example.binm.viewmodel.EditOfferViewModelFactory
import com.example.binm.viewmodel.FavoriteViewModel
import com.example.binm.viewmodel.FavoriteViewModelFactory
import com.example.binm.viewmodel.MyListingsViewModel
import com.example.binm.viewmodel.MyListingsViewModelFactory
import com.example.binm.viewmodel.ProductViewModelFactory
import com.example.binm.viewmodel.ChatViewModel
import com.example.binm.viewmodel.ChatViewModelFactory
import com.example.binm.viewmodel.ProfileViewModel
import com.example.binm.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BINMTheme {
                val navController = rememberNavController()
                val categoryViewModel: CategoryViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(MainApplication.sessionManager))
                val myListingsViewModel: MyListingsViewModel = viewModel(factory = MyListingsViewModelFactory(MainApplication.sessionManager))
                val addOfferViewModel: AddOfferViewModel = viewModel(factory = AddOfferViewModelFactory(MainApplication.sessionManager))
                val editOfferViewModel: EditOfferViewModel = viewModel(factory = EditOfferViewModelFactory(MainApplication.sessionManager))
                val favoriteViewModel: FavoriteViewModel = viewModel(factory = FavoriteViewModelFactory(MainApplication.sessionManager))
                val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(MainApplication.sessionManager))
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(MainApplication.sessionManager))

                // Globalna obsługa błędów autoryzacji
                LaunchedEffect(Unit) {
                    AuthManager.authRequired.onEach {
                        if (it) {
                            lifecycleScope.launch {
                                MainApplication.sessionManager.clearSession()
                            }
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }.launchIn(lifecycleScope)
                }

                val startDestination = if (MainApplication.sessionManager.isUserLoggedIn()) {
                    "home"
                } else {
                    "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("home") { HomePage(navController, categoryViewModel, listingViewModel = viewModel(), favoriteViewModel = favoriteViewModel, profileViewModel = profileViewModel) }
                    composable("login") { LoginPage(navController, authViewModel) }
                    composable("register") { RegisterPage(navController, authViewModel) }
                    composable("verify_otp") { VerifyOtpPage(navController, authViewModel) }
                    composable("forgot_password") { ForgotPasswordPage(navController) }
                    composable("favorites") { FavoritesPage(navController, favoriteViewModel) }
                    composable("add") { AddOfferPage(navController, addOfferViewModel) }
                    composable("chat") { ChatPage(navController, chatViewModel) }
                    composable("help") { HelpPage(navController) }
                    
                    // Konwersacje
                    composable(
                        route = "conversation/{conversationId}",
                        arguments = listOf(navArgument("conversationId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
                        ConversationPage(
                            navController = navController,
                            conversationId = conversationId,
                            chatViewModel = chatViewModel
                        )
                    }
                    
                    // Nowa konwersacja (z ProductDetailPage)
                    composable(
                        route = "new_conversation/{listingId}/{listingTitle}/{sellerId}/{sellerName}",
                        arguments = listOf(
                            navArgument("listingId") { type = NavType.StringType },
                            navArgument("listingTitle") { type = NavType.StringType },
                            navArgument("sellerId") { type = NavType.StringType },
                            navArgument("sellerName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                        val listingTitle = URLDecoder.decode(backStackEntry.arguments?.getString("listingTitle") ?: "", StandardCharsets.UTF_8.toString())
                        val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
                        val sellerName = URLDecoder.decode(backStackEntry.arguments?.getString("sellerName") ?: "", StandardCharsets.UTF_8.toString())
                        
                        NewConversationPage(
                            navController = navController,
                            chatViewModel = chatViewModel,
                            listingId = listingId,
                            listingTitle = listingTitle,
                            listingImageUrl = null,
                            sellerId = sellerId,
                            sellerName = sellerName
                        )
                    }
                    composable("user_panel") { UserPanelPage(navController) }
                    composable("profile") { ProfilePage(navController, profileViewModel) }
                    composable("settings") { SettingsPage(navController) }
                    composable("my_listings") { MyListingsPage(navController, myListingsViewModel) }

                    composable(
                        route = "edit_offer/{publicId}",
                        arguments = listOf(navArgument("publicId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val publicId = backStackEntry.arguments?.getString("publicId")
                        if (publicId != null) {
                            EditOfferPage(navController, publicId = publicId, viewModel = editOfferViewModel)
                        }
                    }

                    composable(
                        route = "subcategory/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val parentId = backStackEntry.arguments?.getInt("id") ?: -1
                        SubCategoryPage(navController, parentId, categoryViewModel)
                    }

                    composable(
                        route = "productlist/category/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getInt("categoryId")
                        ProductListPage(navController, categoryId = categoryId, categoryViewModel = categoryViewModel, productViewModel = viewModel(), favoriteViewModel = favoriteViewModel)
                    }

                    composable(
                        route = "productlist/search/{query}",
                        arguments = listOf(navArgument("query") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val query = backStackEntry.arguments?.getString("query")
                        ProductListPage(navController, query = query, categoryViewModel = categoryViewModel, productViewModel = viewModel(), favoriteViewModel = favoriteViewModel)
                    }

                    composable(
                        route = "product/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")
                        if (productId != null) {
                            ProductDetailPage(
                                navController = navController, 
                                productId = productId, 
                                productViewModel = viewModel(factory = ProductViewModelFactory(MainApplication.sessionManager)), 
                                favoriteViewModel = favoriteViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
