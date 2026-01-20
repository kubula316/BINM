package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.binm.api.RetrofitInstance
import com.example.binm.data.ResetPasswordRequest
import kotlinx.coroutines.launch

enum class ResetPasswordStep {
    ENTER_EMAIL,
    ENTER_OTP,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordPage(navController: NavController) {
    var currentStep by remember { mutableStateOf(ResetPasswordStep.ENTER_EMAIL) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (currentStep) {
                            ResetPasswordStep.ENTER_EMAIL -> "Resetowanie hasła"
                            ResetPasswordStep.ENTER_OTP -> "Wprowadź kod"
                            ResetPasswordStep.SUCCESS -> "Sukces"
                        }
                    ) 
                },
                navigationIcon = {
                    if (currentStep != ResetPasswordStep.SUCCESS) {
                        IconButton(onClick = { 
                            if (currentStep == ResetPasswordStep.ENTER_OTP) {
                                currentStep = ResetPasswordStep.ENTER_EMAIL
                                errorMessage = null
                            } else {
                                navController.popBackStack() 
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentStep) {
                ResetPasswordStep.ENTER_EMAIL -> {
                    EnterEmailStep(
                        email = email,
                        onEmailChange = { email = it },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onSendOtp = {
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Wprowadź poprawny adres email"
                                return@EnterEmailStep
                            }
                            
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val response = RetrofitInstance.api.sendResetOtp(email)
                                    if (response.isSuccessful) {
                                        currentStep = ResetPasswordStep.ENTER_OTP
                                    } else {
                                        errorMessage = "Nie udało się wysłać kodu. Sprawdź adres email."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Błąd połączenia: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
                
                ResetPasswordStep.ENTER_OTP -> {
                    EnterOtpStep(
                        email = email,
                        otp = otp,
                        onOtpChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                        newPassword = newPassword,
                        onNewPasswordChange = { newPassword = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onResetPassword = {
                            when {
                                otp.length != 6 -> {
                                    errorMessage = "Kod OTP musi mieć 6 cyfr"
                                }
                                newPassword.length < 6 -> {
                                    errorMessage = "Hasło musi mieć minimum 6 znaków"
                                }
                                newPassword != confirmPassword -> {
                                    errorMessage = "Hasła nie są identyczne"
                                }
                                else -> {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        try {
                                            val request = ResetPasswordRequest(
                                                email = email,
                                                otp = otp,
                                                newPassword = newPassword
                                            )
                                            val response = RetrofitInstance.api.resetPassword(request)
                                            if (response.isSuccessful) {
                                                currentStep = ResetPasswordStep.SUCCESS
                                            } else {
                                                errorMessage = "Nieprawidłowy kod OTP lub błąd serwera"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Błąd połączenia: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        onResendOtp = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val response = RetrofitInstance.api.sendResetOtp(email)
                                    if (response.isSuccessful) {
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Nie udało się wysłać kodu ponownie"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Błąd połączenia"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    )
                }
                
                ResetPasswordStep.SUCCESS -> {
                    SuccessStep(
                        onBackToLogin = {
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnterEmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSendOtp: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Zapomniałeś hasła?",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Wprowadź adres email powiązany z Twoim kontem. Wyślemy Ci kod do zresetowania hasła.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Adres email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = errorMessage != null,
            enabled = !isLoading
        )
        
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSendOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && email.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Wyślij kod")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Wróć do logowania",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(enabled = !isLoading) { onBackToLogin() }
        )
    }
}

@Composable
private fun EnterOtpStep(
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onResetPassword: () -> Unit,
    onResendOtp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Password,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Wprowadź kod",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Wysłaliśmy 6-cyfrowy kod na adres:\n$email",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("Kod OTP (6 cyfr)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = errorMessage != null,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("Nowe hasło") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = errorMessage != null,
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Potwierdź nowe hasło") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = errorMessage != null,
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )
        
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onResetPassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && otp.length == 6 && newPassword.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Zmień hasło")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nie otrzymałeś kodu? Wyślij ponownie",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(enabled = !isLoading) { onResendOtp() }
        )
    }
}

@Composable
private fun SuccessStep(
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Hasło zostało zmienione!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Możesz teraz zalogować się używając nowego hasła.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Przejdź do logowania")
        }
    }
}
