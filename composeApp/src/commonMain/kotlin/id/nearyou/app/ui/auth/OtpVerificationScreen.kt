package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.nearyou.app.ui.components.OtpInput
import id.nearyou.app.ui.components.PrimaryButton
import id.nearyou.app.ui.theme.Dimensions
import id.nearyou.app.ui.theme.Spacing
import id.nearyou.app.ui.theme.Strings
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * OTP Verification Screen - Refactored for best practices
 * 
 * Timer management moved to ViewModel to prevent unnecessary recomposition
 */
@Composable
fun OtpVerificationScreen(
    identifier: String,
    identifierType: String,
    username: String? = null,
    onVerificationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.events.collectAsState()
    val isSignup = username != null
    val scrollState = rememberScrollState()

    // Handle navigation events
    LaunchedEffect(event) {
        when (event) {
            is AuthEvent.NavigateToMain -> {
                onVerificationSuccess()
                viewModel.onEventConsumed()
            }
            else -> {}
        }
    }

    // Countdown timer - using LaunchedEffect with key
    LaunchedEffect(uiState.otpTimeRemaining) {
        if (uiState.otpTimeRemaining > 0) {
            delay(1000)
            viewModel.updateOtpTimer()
        }
    }

    // Extract error and success message to local variables to avoid smart cast issues
    val currentError = uiState.error
    val currentSuccessMessage = uiState.successMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Push content down with spacer
        Spacer(modifier = Modifier.weight(0.3f))
        // Logo and Title
        AuthHeader()

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Verification message
        Text(
            text = Strings.VERIFICATION_CODE_SENT,
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
            value = uiState.otpCode,
            onValueChange = viewModel::updateOtpCode,
            length = 6,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // Timer - wrapped in derivedStateOf for optimization
        val timerText by remember {
            derivedStateOf {
                "${Strings.CODE_EXPIRES_IN} 0:${uiState.otpTimeRemaining.toString().padStart(2, '0')}"
            }
        }
        
        if (uiState.otpTimeRemaining > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(Dimensions.TIMER_DOT_SIZE),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(Dimensions.TIMER_DOT_SIZE),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = timerText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Resend section
        if (uiState.canResendOtp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Strings.DIDNT_RECEIVE_CODE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        viewModel.resendOtp(identifier, identifierType, username)
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text(Strings.RESEND)
                }
            }
        } else {
            Text(
                text = "${Strings.RESEND_IN} 0:${uiState.otpTimeRemaining.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Error Message
        if (currentError != null) {
            Text(
                text = currentError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                textAlign = TextAlign.Center
            )
        }

        // Success Message
        if (currentSuccessMessage != null) {
            Text(
                text = currentSuccessMessage,
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
                viewModel.verifyOtp(identifier, identifierType)
            },
            text = Strings.VERIFY,
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading && uiState.otpCode.length == 6
        )

        Spacer(modifier = Modifier.weight(1f))

        // Back Button
        TextButton(
            onClick = {
                viewModel.resetInputs()
                onNavigateBack()
            },
            enabled = !uiState.isLoading
        ) {
            Text(Strings.USE_DIFFERENT_CONTACT)
        }
    }
}
