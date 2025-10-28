package id.nearyou.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.nearyou.app.ui.components.ErrorScreen
import id.nearyou.app.ui.components.PrimaryButton
import id.nearyou.app.ui.theme.Spacing
import org.koin.compose.koinInject

/**
 * Profile screen showing user's profile information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinInject(),
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.error != null -> {
                    ErrorScreen(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.loadProfile() }
                    )
                }
                
                uiState.user != null -> {
                    ProfileContent(
                        user = uiState.user!!,
                        onLogout = onLogout,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: domain.model.User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Photo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePhotoUrl != null) {
                // TODO: Load image from URL when image loading is implemented
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Display Name
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.xxs))

        // Username
        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.md))
        
        // Bio
        user.bio?.let { bioText ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = bioText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Spacing.md)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }

        // User Info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md)
            ) {
                Text(
                    text = "Account Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                user.email?.let { emailValue ->
                    InfoRow(label = "Email", value = emailValue)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                user.phone?.let { phoneValue ->
                    InfoRow(label = "Phone", value = phoneValue)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                InfoRow(
                    label = "Subscription",
                    value = user.subscriptionTier.name
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                InfoRow(
                    label = "Verified",
                    value = if (user.isVerified) "Yes" else "No"
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

