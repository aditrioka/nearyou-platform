package id.nearyou.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.navigation.AuthNavigation
import id.nearyou.app.ui.main.MainScreen
import id.nearyou.app.ui.theme.NearYouTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main App Composable - Refactored
 * 
 * Now uses koinViewModel() consistently for proper lifecycle management
 */
@Composable
@Preview
fun App() {
    NearYouTheme {
        // Get AuthViewModel from Koin DI container with proper lifecycle
        val authViewModel: AuthViewModel = koinViewModel()
        val authState by authViewModel.uiState.collectAsState()

        when {
            authState.isLoading -> {
                // Show loading indicator while checking auth status
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            authState.isAuthenticated -> {
                // Show main app if authenticated
                MainScreen()
            }
            else -> {
                // Show auth flow if not authenticated
                AuthNavigation(
                    onAuthSuccess = {
                        // AuthViewModel handles state update
                        // This just ensures we refresh if needed
                        authViewModel.checkAuthStatus()
                    }
                )
            }
        }
    }
}
