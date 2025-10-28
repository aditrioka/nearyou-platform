package id.nearyou.app.ui.navigation

import androidx.compose.runtime.*
import id.nearyou.app.ui.auth.AuthEvent
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.auth.LoginScreen
import id.nearyou.app.ui.auth.SignupScreen
import id.nearyou.app.ui.auth.OtpVerificationScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Navigation state for auth flow
 */
sealed class AuthRoute {
    data object Login : AuthRoute()
    data object Signup : AuthRoute()
    data class OtpVerification(
        val identifier: String,
        val identifierType: String,
        val username: String? = null
    ) : AuthRoute()
}

/**
 * Auth navigation composable - Refactored
 * 
 * Now uses events from ViewModel to trigger navigation
 * instead of callbacks from screens
 */
@Composable
fun AuthNavigation(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var currentRoute by remember { mutableStateOf<AuthRoute>(AuthRoute.Login) }
    val event by viewModel.events.collectAsState()
    
    // Handle navigation events from ViewModel
    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is AuthEvent.NavigateToOtpVerification -> {
                currentRoute = AuthRoute.OtpVerification(
                    identifier = currentEvent.identifier,
                    identifierType = currentEvent.identifierType,
                    username = currentEvent.username
                )
                viewModel.onEventConsumed()
            }
            is AuthEvent.NavigateToMain -> {
                onAuthSuccess()
                viewModel.onEventConsumed()
            }
            else -> {}
        }
    }
    
    when (val route = currentRoute) {
        is AuthRoute.Login -> {
            LoginScreen(
                onNavigateToSignup = {
                    currentRoute = AuthRoute.Signup
                }
            )
        }
        
        is AuthRoute.Signup -> {
            SignupScreen(
                onNavigateToLogin = {
                    currentRoute = AuthRoute.Login
                }
            )
        }
        
        is AuthRoute.OtpVerification -> {
            OtpVerificationScreen(
                identifier = route.identifier,
                identifierType = route.identifierType,
                username = route.username,
                onVerificationSuccess = onAuthSuccess,
                onNavigateBack = {
                    currentRoute = if (route.username != null) {
                        AuthRoute.Signup
                    } else {
                        AuthRoute.Login
                    }
                }
            )
        }
    }
}
