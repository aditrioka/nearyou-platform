package id.nearyou.app.ui.navigation

import androidx.compose.runtime.*
import id.nearyou.app.ui.auth.LoginScreen
import id.nearyou.app.ui.auth.SignupScreen
import id.nearyou.app.ui.auth.OtpVerificationScreen

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
 * Auth navigation composable
 */
@Composable
fun AuthNavigation(
    onAuthSuccess: () -> Unit
) {
    var currentRoute by remember { mutableStateOf<AuthRoute>(AuthRoute.Login) }
    
    when (val route = currentRoute) {
        is AuthRoute.Login -> {
            LoginScreen(
                onNavigateToSignup = {
                    currentRoute = AuthRoute.Signup
                },
                onNavigateToOtpVerification = { identifier, identifierType ->
                    currentRoute = AuthRoute.OtpVerification(
                        identifier = identifier,
                        identifierType = identifierType,
                        username = null
                    )
                },
                onLoginSuccess = onAuthSuccess
            )
        }
        
        is AuthRoute.Signup -> {
            SignupScreen(
                onNavigateToLogin = {
                    currentRoute = AuthRoute.Login
                },
                onNavigateToOtpVerification = { identifier, identifierType, username ->
                    currentRoute = AuthRoute.OtpVerification(
                        identifier = identifier,
                        identifierType = identifierType,
                        username = username
                    )
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

