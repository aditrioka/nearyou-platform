package id.nearyou.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.validation.UserValidation
import id.nearyou.app.ui.components.PrimaryButton
import id.nearyou.app.ui.components.SecondaryButton
import id.nearyou.app.ui.components.TextInput
import id.nearyou.app.ui.theme.Spacing
import org.koin.compose.koinInject

/**
 * Edit profile screen for updating user information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = koinInject(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var displayName by remember { mutableStateOf(uiState.user?.displayName ?: "") }
    var bio by remember { mutableStateOf(uiState.user?.bio ?: "") }
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }
    
    // Update fields when user data loads
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            displayName = user.displayName
            bio = user.bio ?: ""
        }
    }
    
    // Navigate back on success
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Display Name
                TextInput(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        displayNameError = null
                    },
                    label = "Display Name",
                    placeholder = "Enter your display name",
                    error = displayNameError,
                    enabled = !uiState.isLoading
                )

                // Bio
                TextInput(
                    value = bio,
                    onValueChange = {
                        bio = it
                        bioError = null
                    },
                    label = "Bio",
                    placeholder = "Tell us about yourself",
                    error = bioError,
                    enabled = !uiState.isLoading,
                    singleLine = false
                )
                
                Text(
                    text = "${bio.length}/200 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Profile Photo URL (placeholder until upload is implemented)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.md)
                    ) {
                        Text(
                            text = "Profile Photo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Photo upload will be available soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Error message
                if (uiState.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    )
                    
                    PrimaryButton(
                        text = if (uiState.isLoading) "Saving..." else "Save",
                        onClick = {
                            // Validate inputs
                            var hasError = false
                            
                            val displayNameValidation = UserValidation.validateDisplayName(displayName)
                            if (!displayNameValidation.isValid) {
                                displayNameError = displayNameValidation.error
                                hasError = true
                            }
                            
                            val bioValidation = UserValidation.validateBio(bio)
                            if (!bioValidation.isValid) {
                                bioError = bioValidation.error
                                hasError = true
                            }
                            
                            if (!hasError) {
                                viewModel.updateProfile(
                                    displayName = if (displayName != uiState.user?.displayName) displayName else null,
                                    bio = if (bio != uiState.user?.bio) bio else null
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    )
                }
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

