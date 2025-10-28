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
 * Login Screen - Refactored for best practices
 * 
 * This screen is now a pure UI component that:
 * - Observes state from ViewModel
 * - Sends user actions to ViewModel
 * - Has no local state management (follows single source of truth)
 */
@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
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
                // Navigation will be handled by parent
                viewModel.onEventConsumed()
            }
            is AuthEvent.NavigateToMain -> {
                // This is handled by App.kt checking isAuthenticated
                viewModel.onEventConsumed()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars) // Proper system bars padding
            .imePadding() // Handle keyboard
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

        // Identifier Input
        TextInput(
            value = uiState.identifier,
            onValueChange = viewModel::updateIdentifier,
            label = Strings.EMAIL_OR_PHONE_LABEL,
            placeholder = Strings.EMAIL_OR_PHONE_PLACEHOLDER,
            keyboardType = KeyboardType.Email,
            enabled = !uiState.isLoading,
            error = uiState.error
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // Sign In Button
        PrimaryButton(
            onClick = viewModel::login,
            text = Strings.SIGN_IN,
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

        // Push bottom content to the end
        Spacer(modifier = Modifier.weight(1f))

        // Sign Up Link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = Spacing.md)
        ) {
            Text(
                text = Strings.DONT_HAVE_ACCOUNT,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = {
                    viewModel.resetInputs()
                    onNavigateToSignup()
                },
                enabled = !uiState.isLoading
            ) {
                Text(Strings.SIGN_UP)
            }
        }
    }
}
