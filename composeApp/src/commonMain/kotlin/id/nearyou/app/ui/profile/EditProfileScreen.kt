package id.nearyou.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
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
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Image picker launcher
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = rememberCoroutineScope(),
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { bytes ->
                selectedImageBytes = bytes
                selectedImage = bytes.toImageBitmap()
                // Upload the photo
                viewModel.uploadProfilePhoto(
                    imageBytes = bytes,
                    fileName = "profile_${System.currentTimeMillis()}.jpg",
                    contentType = "image/jpeg"
                )
            }
        }
    )

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

                // Profile Photo Upload
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Profile Photo",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Photo preview or placeholder
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable(enabled = !uiState.isUploadingPhoto) {
                                    singleImagePicker.launch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                uiState.isUploadingPhoto -> {
                                    CircularProgressIndicator()
                                }
                                selectedImage != null -> {
                                    Image(
                                        bitmap = selectedImage!!,
                                        contentDescription = "Selected profile photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                uiState.uploadedPhotoUrl != null || uiState.user?.profilePhotoUrl != null -> {
                                    AsyncImage(
                                        model = uiState.uploadedPhotoUrl ?: uiState.user?.profilePhotoUrl,
                                        contentDescription = "Current profile photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload photo",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Text(
                            text = if (uiState.isUploadingPhoto) "Uploading..." else "Tap to change photo",
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

