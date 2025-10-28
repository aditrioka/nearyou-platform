package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import id.nearyou.app.ui.components.PrimaryButton
import id.nearyou.app.ui.components.TextInput
import id.nearyou.app.ui.theme.Spacing
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOtpVerification: (String, String, String) -> Unit, // identifier, type, username
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()
    val authState by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
            // Logo and Title
            AuthHeader()

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Email or Phone Label
            Text(
                text = "Email or Phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xs)
            )

            // Identifier Input
            TextInput(
                value = identifier,
                onValueChange = { identifier = it },
                label = "",
                placeholder = "Enter your email or phone",
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
                error = null
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Username Label
            Text(
                text = "Username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xs)
            )

            // Username Input
            TextInput(
                value = username,
                onValueChange = { username = it },
                label = "",
                placeholder = "Choose a username",
                enabled = !isLoading,
                error = null
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Error Message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm)
                )
            }

            // Create Account Button
            PrimaryButton(
                onClick = {
                    when {
                        identifier.isBlank() -> {
                            errorMessage = "Please enter your email or phone"
                            return@PrimaryButton
                        }
                        username.isBlank() -> {
                            errorMessage = "Please enter a username"
                            return@PrimaryButton
                        }
                    }

                    isLoading = true
                    errorMessage = null

                    // Determine identifier type
                    val identifierType = if (identifier.contains("@")) "email" else "phone"

                    // Call register API
                    scope.launch {
                        val result = viewModel.register(
                            username = username,
                            identifier = identifier,
                            identifierType = identifierType
                        )

                        result.fold(
                            onSuccess = {
                                // Navigate to OTP verification on success
                                onNavigateToOtpVerification(identifier, identifierType, username)
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Registration failed"
                            }
                        )

                        isLoading = false
                    }
                },
                text = "Create Account",
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Helper Text
            Text(
                text = "We'll send you a verification code",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !isLoading
                ) {
                    Text("Sign In")
                }
            }
        }
    }

