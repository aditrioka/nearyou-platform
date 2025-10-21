package id.nearyou.app.ui.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state
 * Dependencies are injected via constructor (Dependency Injection)
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    var isAuthenticated by mutableStateOf(false)
        private set
    
    var isLoading by mutableStateOf(true)
        private set
    
    init {
        checkAuthStatus()
    }
    
    /**
     * Check if user is already authenticated
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            isLoading = true
            isAuthenticated = authRepository.isAuthenticated()
            isLoading = false
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
                    isAuthenticated = true
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
                    isAuthenticated = true
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
            isAuthenticated = false
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

