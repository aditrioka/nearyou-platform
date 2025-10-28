package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.nearyou.app.ui.components.OtpInput
import id.nearyou.app.ui.components.PrimaryButton
import id.nearyou.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OtpVerificationScreen(
    identifier: String,
    identifierType: String,
    username: String? = null, // null for login, non-null for signup
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinInject()
) {
    val authState by viewModel.uiState.collectAsState()

    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var timeRemaining by remember { mutableStateOf(56) } // 56 seconds countdown
    var canResend by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isSignup = username != null

    // Navigate on successful authentication
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onVerificationSuccess()
        }
    }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        canResend = true
    }

    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
            // Logo and Title
            AuthHeader()

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Verification message
            Text(
                text = "Verification code sent to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xxs))

            Text(
                text = identifier,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // OTP Input Boxes
            OtpInput(
                value = otpCode,
                onValueChange = { otpCode = it },
                length = 6,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Timer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "Code expires in 0:${timeRemaining.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Resend text
            Text(
                text = if (canResend) {
                    "Didn't receive the code? "
                } else {
                    "Didn't receive the code? Resend in 0:${timeRemaining.toString().padStart(2, '0')}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (canResend) {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                if (isSignup) {
                                    val result = viewModel.register(
                                        username = username ?: "",
                                        identifier = identifier,
                                        identifierType = identifierType
                                    )
                                    result.fold(
                                        onSuccess = {
                                            successMessage = "Code resent successfully!"
                                            timeRemaining = 56
                                            canResend = false
                                        },
                                        onFailure = { error ->
                                            errorMessage = error.message ?: "Failed to resend code"
                                        }
                                    )
                                } else {
                                    val result = viewModel.login(
                                        identifier = identifier,
                                        identifierType = identifierType
                                    )
                                    result.fold(
                                        onSuccess = {
                                            successMessage = "Code resent successfully!"
                                            timeRemaining = 56
                                            canResend = false
                                        },
                                        onFailure = { error ->
                                            errorMessage = error.message ?: "Failed to resend code"
                                        }
                                    )
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
                    Text("Resend")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Error Message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    textAlign = TextAlign.Center
                )
            }

            // Success Message
            successMessage?.let { success ->
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    textAlign = TextAlign.Center
                )
            }

            // Verify Button
            PrimaryButton(
                onClick = {
                    if (otpCode.length != 6) {
                        errorMessage = "Please enter a 6-digit code"
                        return@PrimaryButton
                    }

                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    scope.launch {
                        val result = viewModel.verifyOtp(
                            identifier = identifier,
                            identifierType = identifierType,
                            otpCode = otpCode
                        )

                        result.fold(
                            onSuccess = {
                                successMessage = "Verification successful!"
                                // State change will trigger navigation via LaunchedEffect
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Verification failed"
                                isLoading = false
                            }
                        )
                    }
                },
                text = "Verify",
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.weight(1f))

            // Back Button
            TextButton(
                onClick = onNavigateBack,
                enabled = !isLoading
            ) {
                Text("Use Different Email or Phone")
            }
        }
    }

