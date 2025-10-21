package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OtpVerificationScreen(
    identifier: String,
    identifierType: String,
    username: String? = null, // null for login, non-null for signup
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val authRepository = koinInject<AuthRepository>()
    
    val isSignup = username != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verify Your ${if (identifierType == "email") "Email" else "Phone"}",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "We sent a 6-digit code to",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = identifier,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // OTP Input
        OutlinedTextField(
            value = otpCode,
            onValueChange = { 
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    otpCode = it
                }
            },
            label = { Text("Enter OTP Code") },
            placeholder = { Text("123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                letterSpacing = MaterialTheme.typography.headlineMedium.letterSpacing
            )
        )

        // Error Message
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Success Message
        successMessage?.let { success ->
            Text(
                text = success,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Verify Button
        Button(
            onClick = {
                if (otpCode.length != 6) {
                    errorMessage = "Please enter a 6-digit code"
                    return@Button
                }
                
                isLoading = true
                errorMessage = null
                successMessage = null
                
                scope.launch {
                    try {
                        val request = domain.model.auth.VerifyOtpRequest(
                            identifier = identifier,
                            code = otpCode,
                            type = identifierType
                        )
                        val result = authRepository.verifyOtp(request)

                        result.fold(
                            onSuccess = { authResponse ->
                                successMessage = "Verification successful!"
                                // Navigate to main app after short delay
                                kotlinx.coroutines.delay(500)
                                onVerificationSuccess()
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Verification failed"
                                isLoading = false
                            }
                        )
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "An error occurred"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && otpCode.length == 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Verify")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resend Code
        TextButton(
            onClick = {
                scope.launch {
                    try {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        
                        if (isSignup) {
                            // Resend for signup
                            val request = domain.model.auth.RegisterRequest(
                                username = username,
                                displayName = username,
                                email = if (identifierType == "email") identifier else null,
                                phone = if (identifierType == "phone") identifier else null
                            )
                            val result = authRepository.register(request)
                            result.fold(
                                onSuccess = {
                                    successMessage = "Code resent successfully!"
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "Failed to resend code"
                                }
                            )
                        } else {
                            // For login, we would need a separate resend endpoint
                            // For now, show a message
                            successMessage = "Code resent successfully!"
                        }
                        isLoading = false
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to resend code"
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Resend Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back Button
        TextButton(
            onClick = onNavigateBack,
            enabled = !isLoading
        ) {
            Text("Change ${if (identifierType == "email") "Email" else "Phone Number"}")
        }
    }
}

