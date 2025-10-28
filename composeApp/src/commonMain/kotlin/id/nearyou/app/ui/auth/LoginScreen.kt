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
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToOtpVerification: (String, String) -> Unit, // identifier, type
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()
    val authState by viewModel.uiState.collectAsState()

    var identifier by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Navigate to main screen if already authenticated
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }

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

            // Sign In Button
            PrimaryButton(
                onClick = {
                    if (identifier.isBlank()) {
                        errorMessage = "Please enter your email or phone"
                        return@PrimaryButton
                    }

                    isLoading = true
                    errorMessage = null

                    // Determine identifier type
                    val identifierType = if (identifier.contains("@")) "email" else "phone"

                    // Call login API
                    scope.launch {
                        val result = viewModel.login(
                            identifier = identifier,
                            identifierType = identifierType
                        )

                        result.fold(
                            onSuccess = {
                                // Navigate to OTP verification on success
                                onNavigateToOtpVerification(identifier, identifierType)
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Login failed"
                            }
                        )

                        isLoading = false
                    }
                },
                text = "Sign In",
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

            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToSignup,
                    enabled = !isLoading
                ) {
                    Text("Sign Up")
                }
            }
        }
    }

