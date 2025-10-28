package id.nearyou.app.ui.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable UI State for Authentication
 * 
 * Using @Immutable annotation helps Compose optimize recomposition
 * by treating this data class as stable and immutable.
 */
@Immutable
data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Login/Signup input states
    val identifier: String = "",
    val username: String = "",
    val otpCode: String = "",
    // OTP timer state
    val otpTimeRemaining: Int = 0,
    val canResendOtp: Boolean = false,
    // Success message for user feedback
    val successMessage: String? = null
)

/**
 * Sealed class for one-time events (navigation, etc.)
 * These events are consumed once and don't persist in state
 */
@Immutable
sealed class AuthEvent {
    @Immutable
    data class NavigateToOtpVerification(
        val identifier: String,
        val identifierType: String,
        val username: String? = null
    ) : AuthEvent()
    
    @Immutable
    data object NavigateToMain : AuthEvent()
    
    @Immutable
    data class ShowError(val message: String) : AuthEvent()
}

/**
 * ViewModel for managing authentication state and business logic
 * 
 * All UI state is managed here, following the single source of truth principle.
 * Screens observe this state and send user actions to the ViewModel.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(AuthUiState())
    
    // Public immutable state
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // One-time events channel
    private val _events = MutableStateFlow<AuthEvent?>(null)
    val events: StateFlow<AuthEvent?> = _events.asStateFlow()

    init {
        checkAuthStatus()
    }
    
    /**
     * Check if user is already authenticated
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val authenticated = authRepository.isAuthenticated()
            _uiState.update { 
                it.copy(
                    isAuthenticated = authenticated, 
                    isLoading = false
                ) 
            }
        }
    }
    
    /**
     * Update identifier input
     */
    fun updateIdentifier(value: String) {
        _uiState.update { it.copy(identifier = value, error = null) }
    }
    
    /**
     * Update username input
     */
    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }
    
    /**
     * Update OTP code input
     */
    fun updateOtpCode(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(otpCode = value, error = null) }
        }
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
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Consume event (mark as handled)
     */
    fun onEventConsumed() {
        _events.value = null
    }
    
    /**
     * Determine identifier type (email or phone)
     */
    private fun getIdentifierType(identifier: String): String {
        return if (identifier.contains("@")) "email" else "phone"
    }
    
    /**
     * Check if email is valid using multiplatform-compatible regex
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }
    
    /**
     * Validate identifier input
     */
    private fun validateIdentifier(identifier: String): String? {
        return when {
            identifier.isBlank() -> "Please enter your email or phone"
            identifier.contains("@") && !isValidEmail(identifier) -> 
                "Please enter a valid email address"
            !identifier.contains("@") && identifier.length < 10 -> 
                "Please enter a valid phone number"
            else -> null
        }
    }
    
    /**
     * Register a new user (sends OTP)
     */
    fun register() {
        val currentState = _uiState.value
        
        // Validate inputs
        val identifierError = validateIdentifier(currentState.identifier)
        if (identifierError != null) {
            _uiState.update { it.copy(error = identifierError) }
            return
        }
        
        if (currentState.username.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a username") }
            return
        }
        
        if (currentState.username.length < 3) {
            _uiState.update { it.copy(error = "Username must be at least 3 characters") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val identifierType = getIdentifierType(currentState.identifier)
                val request = domain.model.auth.RegisterRequest(
                    username = currentState.username,
                    displayName = currentState.username,
                    email = if (identifierType == "email") currentState.identifier else null,
                    phone = if (identifierType == "phone") currentState.identifier else null
                )
                
                authRepository.register(request)
                    .onSuccess {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                otpTimeRemaining = 60,
                                canResendOtp = false
                            ) 
                        }
                        _events.value = AuthEvent.NavigateToOtpVerification(
                            identifier = currentState.identifier,
                            identifierType = identifierType,
                            username = currentState.username
                        )
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Registration failed"
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }

    /**
     * Login existing user (sends OTP)
     */
    fun login() {
        val currentState = _uiState.value
        
        // Validate input
        val identifierError = validateIdentifier(currentState.identifier)
        if (identifierError != null) {
            _uiState.update { it.copy(error = identifierError) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val identifierType = getIdentifierType(currentState.identifier)
                
                authRepository.login(currentState.identifier, identifierType)
                    .onSuccess {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                otpTimeRemaining = 60,
                                canResendOtp = false
                            ) 
                        }
                        _events.value = AuthEvent.NavigateToOtpVerification(
                            identifier = currentState.identifier,
                            identifierType = identifierType,
                            username = null
                        )
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Login failed"
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
    
    /**
     * Resend OTP code
     */
    fun resendOtp(identifier: String, identifierType: String, username: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            try {
                if (username != null) {
                    // Resend for signup
                    val request = domain.model.auth.RegisterRequest(
                        username = username,
                        displayName = username,
                        email = if (identifierType == "email") identifier else null,
                        phone = if (identifierType == "phone") identifier else null
                    )
                    authRepository.register(request)
                        .onSuccess {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Verification code resent successfully",
                                    otpTimeRemaining = 60,
                                    canResendOtp = false
                                ) 
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to resend code"
                                ) 
                            }
                        }
                } else {
                    // Resend for login
                    authRepository.login(identifier, identifierType)
                        .onSuccess {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Verification code resent successfully",
                                    otpTimeRemaining = 60,
                                    canResendOtp = false
                                ) 
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to resend code"
                                ) 
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }

    /**
     * Verify OTP code
     */
    fun verifyOtp(identifier: String, identifierType: String) {
        val currentState = _uiState.value
        
        if (currentState.otpCode.length != 6) {
            _uiState.update { it.copy(error = "Please enter a 6-digit code") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            try {
                val request = domain.model.auth.VerifyOtpRequest(
                    identifier = identifier,
                    code = currentState.otpCode,
                    type = identifierType
                )
                
                authRepository.verifyOtp(request)
                    .onSuccess {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                successMessage = "Verification successful!"
                            ) 
                        }
                        _events.value = AuthEvent.NavigateToMain
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Verification failed"
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
    
    /**
     * Update OTP timer (call this every second)
     */
    fun updateOtpTimer() {
        val currentTime = _uiState.value.otpTimeRemaining
        if (currentTime > 0) {
            _uiState.update { 
                it.copy(
                    otpTimeRemaining = currentTime - 1,
                    canResendOtp = currentTime - 1 == 0
                ) 
            }
        }
    }
    
    /**
     * Login with Google
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                authRepository.loginWithGoogle(idToken)
                    .onSuccess {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true
                            ) 
                        }
                        _events.value = AuthEvent.NavigateToMain
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Google login failed"
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                authRepository.logout()
                _uiState.update { 
                    AuthUiState(
                        isAuthenticated = false,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Logout failed"
                    ) 
                }
            }
        }
    }
    
    /**
     * Refresh authentication token
     */
    fun refreshToken() {
        viewModelScope.launch {
            try {
                authRepository.refreshToken()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Token refresh failed") 
                }
            }
        }
    }
    
    /**
     * Reset state when navigating back to login/signup
     */
    fun resetInputs() {
        _uiState.update { 
            it.copy(
                identifier = "",
                username = "",
                otpCode = "",
                error = null,
                successMessage = null,
                otpTimeRemaining = 0,
                canResendOtp = false
            ) 
        }
    }
}
