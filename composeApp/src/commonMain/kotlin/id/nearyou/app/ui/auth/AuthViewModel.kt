package id.nearyou.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Authentication
 */
data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for managing authentication state
 * Dependencies are injected via constructor (Dependency Injection)
 *
 * Uses StateFlow for reactive state management following Compose best practices
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(AuthUiState())

    // Public immutable state
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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
            _uiState.update { it.copy(isAuthenticated = authenticated, isLoading = false) }
        }
    }
    
    /**
     * Register a new user
     */
    suspend fun register(
        username: String,
        identifier: String,
        identifierType: String
    ): Result<Unit> {
        return try {
            val request = domain.model.auth.RegisterRequest(
                username = username,
                displayName = username, // Use username as display name for now
                email = if (identifierType == "email") identifier else null,
                phone = if (identifierType == "phone") identifier else null
            )
            authRepository.register(request)
                .map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login existing user (sends OTP)
     */
    suspend fun login(
        identifier: String,
        identifierType: String
    ): Result<Unit> {
        return try {
            authRepository.login(identifier, identifierType)
                .map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify OTP code
     */
    suspend fun verifyOtp(
        identifier: String,
        identifierType: String,
        otpCode: String
    ): Result<Unit> {
        return try {
            val request = domain.model.auth.VerifyOtpRequest(
                identifier = identifier,
                code = otpCode,
                type = identifierType
            )
            authRepository.verifyOtp(request)
                .map {
                    _uiState.update { state -> state.copy(isAuthenticated = true) }
                    Unit
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login with Google
     */
    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            authRepository.loginWithGoogle(idToken)
                .map {
                    _uiState.update { state -> state.copy(isAuthenticated = true) }
                    Unit
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            authRepository.logout()
            _uiState.update { state -> state.copy(isAuthenticated = false) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh authentication token
     */
    suspend fun refreshToken(): Result<Unit> {
        return try {
            authRepository.refreshToken()
                .map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

