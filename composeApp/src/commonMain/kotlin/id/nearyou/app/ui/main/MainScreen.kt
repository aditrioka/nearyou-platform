package id.nearyou.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.theme.Dimensions
import id.nearyou.app.ui.theme.Spacing
import id.nearyou.app.ui.theme.Strings
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Screen - Refactored
 * 
 * Uses consistent koinViewModel injection
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(Dimensions.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = Strings.WELCOME_MESSAGE,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        
        Text(
            text = Strings.AUTHENTICATED_MESSAGE,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.xl)
        )
        
        Button(
            onClick = authViewModel::logout,
            enabled = !authState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.MAIN_BUTTON_HEIGHT)
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.BUTTON_LOADING_INDICATOR_SIZE),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(Strings.LOGOUT)
            }
        }
    }
}
