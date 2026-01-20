package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun RegisterPage(navController: NavController, authViewModel: AuthViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val authState by authViewModel.authState.collectAsState()

    // Resetuj stan, gdy użytkownik wejdzie na ekran
    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    // Nawiguj do weryfikacji OTP po udanej rejestracji i zalogowaniu
    LaunchedEffect(authState) {
        if (authState is AuthState.OtpRequired) { // Zmieniono na OtpRequired
            navController.navigate("verify_otp") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Stwórz konto", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nazwa użytkownika") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null || authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null || authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null || authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Powtórz hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            val currentError = when (authState) {
                is AuthState.Error -> (authState as AuthState.Error).message
                else -> validationError
            }
            if (currentError != null && authState !is AuthState.Success) {
                Text(currentError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    validationError = null
                    authViewModel.resetState()
                    when {
                        username.isBlank() || email.isBlank() || password.isBlank() -> validationError = "Wszystkie pola muszą być wypełnione."
                        username.length < 3 -> validationError = "Nazwa użytkownika musi mieć min. 3 znaki."
                        !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> validationError = "Podaj poprawny adres email."
                        password.length < 8 -> validationError = "Hasło musi mieć min. 8 znaków."
                        password != confirmPassword -> validationError = "Hasła nie są identyczne."
                        else -> {
                            authViewModel.name.value = username
                            authViewModel.email.value = email
                            authViewModel.password.value = password
                            authViewModel.register()
                        }
                    }
                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Zarejestruj się")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Masz już konto? Zaloguj się!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { if(authState != AuthState.Loading) navController.popBackStack() }
            )
        }
    }
}
