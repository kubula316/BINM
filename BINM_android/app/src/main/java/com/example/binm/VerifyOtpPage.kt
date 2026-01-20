package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.binm.viewmodel.AuthViewModel
import com.example.binm.viewmodel.AuthState

@Composable
fun VerifyOtpPage(navController: NavController, authViewModel: AuthViewModel) {
    var otp by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    // Nawiguj do strony głównej po udanej weryfikacji
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
            Text("Weryfikacja Konta", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 16.dp))
            Text(
                text = "Wysłaliśmy 6-cyfrowy kod na Twój adres email. Wprowadź go poniżej, aby aktywować konto.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                label = { Text("Kod OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
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
                    authViewModel.otp.value = otp
                    authViewModel.verifyOtp() 
                },
                enabled = authState != AuthState.Loading && otp.length == 6,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Weryfikuj")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nie otrzymałeś kodu? Wyślij ponownie",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { authViewModel.resendOtp() }
            )
        }
    }
}
