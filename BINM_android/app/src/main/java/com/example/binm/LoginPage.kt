package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.binm.viewmodel.AuthViewModel
import com.example.binm.viewmodel.AuthState

@Composable
fun LoginPage(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    // Resetuj stan, gdy użytkownik wejdzie na ekran
    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    // Nawiguj po udanym logowaniu
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Zaloguj się", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (authState is AuthState.Error) {
                Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    authViewModel.email.value = email
                    authViewModel.password.value = password
                    authViewModel.login() 
                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Zaloguj się")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Nie masz konta? Zarejestruj się!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate("register") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Nie pamiętasz hasła?",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable { navController.navigate("forgot_password") }
            )
        }
    }
}
