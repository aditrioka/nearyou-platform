package id.nearyou.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import id.nearyou.app.navigation.AuthGraph
import id.nearyou.app.navigation.MainGraph
import id.nearyou.app.navigation.authNavGraph
import id.nearyou.app.navigation.mainNavGraph
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.theme.NearYouTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    NearYouTheme {
        val authViewModel: AuthViewModel = koinViewModel()
        val authState by authViewModel.uiState.collectAsState()
        val navController = rememberNavController()

        when {
            authState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                val startDestination: Any = if (authState.isAuthenticated) MainGraph else AuthGraph

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    authNavGraph(
                        navController = navController,
                        onAuthSuccess = {
                            authViewModel.checkAuthStatus()
                            navController.navigate(MainGraph) {
                                popUpTo(AuthGraph) { inclusive = true }
                            }
                        },
                    )
                    mainNavGraph(
                        navController = navController,
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(AuthGraph) {
                                popUpTo(MainGraph) { inclusive = true }
                            }
                        },
                    )
                }
            }
        }
    }
}
