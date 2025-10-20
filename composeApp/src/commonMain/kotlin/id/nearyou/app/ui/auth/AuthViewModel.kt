package id.nearyou.app.ui.auth

import androidx.compose.runtime.*
import data.AuthRepository
import data.createTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state
 */
class AuthViewModel {
    private val authRepository = AuthRepository(createTokenStorage())
    private val scope = CoroutineScope(Dispatchers.Main)
    
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
        scope.launch {
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

