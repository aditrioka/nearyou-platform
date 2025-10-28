package id.nearyou.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.UserRepository
import domain.model.UpdateUserRequest
import domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val updateSuccess: Boolean = false
)

/**
 * ViewModel for user profile management
 */
class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
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
                                error = null
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load profile"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
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
        profilePhotoUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, updateSuccess = false) }
            
            try {
                val request = UpdateUserRequest(
                    displayName = displayName,
                    bio = bio,
                    profilePhotoUrl = profilePhotoUrl
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
                                isEditing = false
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to update profile",
                                updateSuccess = false
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile",
                        updateSuccess = false
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
}

