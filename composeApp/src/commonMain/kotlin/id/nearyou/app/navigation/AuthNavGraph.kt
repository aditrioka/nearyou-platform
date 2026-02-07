package id.nearyou.app.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import id.nearyou.app.ui.auth.AuthEvent
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.auth.LoginScreen
import id.nearyou.app.ui.auth.OtpVerificationScreen
import id.nearyou.app.ui.auth.SignupScreen
import id.nearyou.app.ui.util.ObserveEvents
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onAuthSuccess: () -> Unit,
) {
    navigation<AuthGraph>(startDestination = Login) {
        composable<Login> {
            val authViewModel: AuthViewModel = koinViewModel()

            ObserveEvents(authViewModel.events) { event ->
                when (event) {
                    is AuthEvent.NavigateToOtpVerification -> {
                        navController.navigate(
                            OtpVerification(
                                identifier = event.identifier,
                                identifierType = event.identifierType,
                                username = event.username,
                            ),
                        )
                    }
                    is AuthEvent.NavigateToMain -> onAuthSuccess()
                    is AuthEvent.ShowError -> {}
                }
            }

            LoginScreen(
                onNavigateToSignup = {
                    authViewModel.resetInputs()
                    navController.navigate(Signup)
                },
                viewModel = authViewModel,
            )
        }

        composable<Signup> {
            val authViewModel: AuthViewModel = koinViewModel()

            ObserveEvents(authViewModel.events) { event ->
                when (event) {
                    is AuthEvent.NavigateToOtpVerification -> {
                        navController.navigate(
                            OtpVerification(
                                identifier = event.identifier,
                                identifierType = event.identifierType,
                                username = event.username,
                            ),
                        )
                    }
                    is AuthEvent.NavigateToMain -> onAuthSuccess()
                    is AuthEvent.ShowError -> {}
                }
            }

            SignupScreen(
                onNavigateToLogin = {
                    authViewModel.resetInputs()
                    navController.popBackStack()
                },
                viewModel = authViewModel,
            )
        }

        composable<OtpVerification> { backStackEntry ->
            val route = backStackEntry.toRoute<OtpVerification>()
            val authViewModel: AuthViewModel = koinViewModel()

            ObserveEvents(authViewModel.events) { event ->
                when (event) {
                    is AuthEvent.NavigateToMain -> onAuthSuccess()
                    is AuthEvent.NavigateToOtpVerification -> {}
                    is AuthEvent.ShowError -> {}
                }
            }

            OtpVerificationScreen(
                identifier = route.identifier,
                identifierType = route.identifierType,
                username = route.username,
                onVerificationSuccess = onAuthSuccess,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = authViewModel,
            )
        }
    }
}
