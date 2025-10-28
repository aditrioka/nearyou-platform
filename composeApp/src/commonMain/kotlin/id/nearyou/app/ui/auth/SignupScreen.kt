package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import id.nearyou.app.ui.components.PrimaryButton
import id.nearyou.app.ui.components.TextInput
import id.nearyou.app.ui.theme.Spacing
import id.nearyou.app.ui.theme.Strings
import org.koin.compose.viewmodel.koinViewModel

/**
 * Signup Screen - Refactored for best practices
 * 
 * Pure UI component with no local state management
 */
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.events.collectAsState()
    val scrollState = rememberScrollState()
    
    // Handle one-time events
    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is AuthEvent.NavigateToOtpVerification -> {
                // Navigation handled by parent
                viewModel.onEventConsumed()
            }
            else -> {}
        }
    }

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

        // Email or Phone Label
        Text(
            text = Strings.EMAIL_OR_PHONE_LABEL,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xs)
        )

        // Identifier Input - with smart cast fix
        val currentError = uiState.error
        TextInput(
            value = uiState.identifier,
            onValueChange = viewModel::updateIdentifier,
            label = Strings.EMAIL_OR_PHONE_LABEL,
            placeholder = Strings.EMAIL_OR_PHONE_PLACEHOLDER,
            keyboardType = KeyboardType.Email,
            enabled = !uiState.isLoading,
            error = if (currentError != null && 
                       (currentError.contains("email", ignoreCase = true) || 
                        currentError.contains("phone", ignoreCase = true))) currentError else null
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // Username Label
        Text(
            text = Strings.USERNAME_LABEL,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xs)
        )

        // Username Input
        TextInput(
            value = uiState.username,
            onValueChange = viewModel::updateUsername,
            label = Strings.USERNAME_LABEL,
            placeholder = Strings.USERNAME_PLACEHOLDER,
            enabled = !uiState.isLoading,
            error = if (currentError != null && currentError.contains("username", ignoreCase = true)) currentError else null
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // General Error Message (not field-specific)
        if (currentError != null && 
            !currentError.contains("email", ignoreCase = true) &&
            !currentError.contains("phone", ignoreCase = true) &&
            !currentError.contains("username", ignoreCase = true)) {
            Text(
                text = currentError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm)
            )
        }

        // Create Account Button
        PrimaryButton(
            onClick = viewModel::register,
            text = Strings.CREATE_ACCOUNT,
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Helper Text
        Text(
            text = Strings.VERIFICATION_CODE_MESSAGE,
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
                text = Strings.ALREADY_HAVE_ACCOUNT,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = {
                    viewModel.resetInputs()
                    onNavigateToLogin()
                },
                enabled = !uiState.isLoading
            ) {
                Text(Strings.SIGN_IN_LINK)
            }
        }
    }
}
