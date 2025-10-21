package id.nearyou.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.navigation.AuthNavigation
import id.nearyou.app.ui.main.MainScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Get AuthViewModel from Koin DI container
        val authViewModel = koinViewModel<AuthViewModel>()

        when {
            authViewModel.isLoading -> {
                // Show loading indicator while checking auth status
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            authViewModel.isAuthenticated -> {
                // Show main app if authenticated
                MainScreen(authViewModel = authViewModel)
            }
            else -> {
                // Show auth flow if not authenticated
                AuthNavigation(
                    onAuthSuccess = {
                        authViewModel.checkAuthStatus()
                    }
                )
            }
        }
    }
}