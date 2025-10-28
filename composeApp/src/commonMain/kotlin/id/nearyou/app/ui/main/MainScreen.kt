package id.nearyou.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.profile.ProfileScreen
import id.nearyou.app.ui.profile.EditProfileScreen
import id.nearyou.app.ui.theme.Dimensions
import id.nearyou.app.ui.theme.Spacing
import id.nearyou.app.ui.theme.Strings
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Screen - Refactored
 *
 * Uses consistent koinViewModel injection
 * Now includes profile navigation
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            modifier = modifier,
            authViewModel = authViewModel,
            onNavigateToProfile = { currentScreen = "profile" }
        )
        "profile" -> ProfileScreen(
            onEditProfile = { currentScreen = "edit_profile" },
            onLogout = {
                authViewModel.logout()
                currentScreen = "home"
            }
        )
        "edit_profile" -> EditProfileScreen(
            onNavigateBack = { currentScreen = "profile" }
        )
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit
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

        // Profile Button
        OutlinedButton(
            onClick = onNavigateToProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.MAIN_BUTTON_HEIGHT)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(Dimensions.BUTTON_LOADING_INDICATOR_SIZE)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("View Profile")
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Logout Button
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
