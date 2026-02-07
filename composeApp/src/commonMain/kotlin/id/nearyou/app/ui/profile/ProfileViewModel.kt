package id.nearyou.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.UserRepository
import domain.model.UpdateUserRequest
import domain.model.User
import id.nearyou.app.ui.util.createEventChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for profile screen
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val updateSuccess: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val uploadedPhotoUrl: String? = null,
)

sealed class ProfileEvent {
    data class ShowMessage(
        val message: String,
    ) : ProfileEvent()

    data object NavigateBack : ProfileEvent()
}

/**
 * ViewModel for user profile management
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = createEventChannel<ProfileEvent>()
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    /**
     * Load current user's profile
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = userRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                user = user,
                                isLoading = false,
                                error = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load profile",
                            )
                        }
                    },
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile",
                    )
                }
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        profilePhotoUrl: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, updateSuccess = false) }

            try {
                val request =
                    UpdateUserRequest(
                        displayName = displayName,
                        bio = bio,
                        profilePhotoUrl = profilePhotoUrl,
                    )

                val result = userRepository.updateCurrentUser(request)
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                user = user,
                                isLoading = false,
                                error = null,
                                updateSuccess = true,
                                isEditing = false,
                            )
                        }
                        _events.send(ProfileEvent.ShowMessage("Profile updated successfully"))
                        _events.send(ProfileEvent.NavigateBack)
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to update profile",
                                updateSuccess = false,
                            )
                        }
                    },
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile",
                        updateSuccess = false,
                    )
                }
            }
        }
    }

    /**
     * Start editing profile
     */
    fun startEditing() {
        _uiState.update { it.copy(isEditing = true, error = null, updateSuccess = false) }
    }

    /**
     * Cancel editing
     */
    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, error = null, updateSuccess = false) }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _uiState.update { it.copy(updateSuccess = false) }
    }

    /**
     * Upload profile photo
     * @param imageBytes Image data as ByteArray
     * @param fileName Original file name
     * @param contentType MIME type of the image
     */
    fun uploadProfilePhoto(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String = "image/jpeg",
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, error = null) }

            try {
                val result = userRepository.uploadProfilePhoto(imageBytes, fileName, contentType)
                result.fold(
                    onSuccess = { uploadResponse ->
                        _uiState.update {
                            it.copy(
                                isUploadingPhoto = false,
                                uploadedPhotoUrl = uploadResponse.url,
                                error = null,
                            )
                        }

                        // Automatically update profile with new photo URL
                        updateProfile(profilePhotoUrl = uploadResponse.url)
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isUploadingPhoto = false,
                                error = error.message ?: "Failed to upload photo",
                            )
                        }
                    },
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        error = e.message ?: "Failed to upload photo",
                    )
                }
            }
        }
    }
}
