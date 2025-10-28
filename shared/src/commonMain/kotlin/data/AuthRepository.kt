package data

import domain.model.auth.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import util.AppConfig
import util.AppLogger

/**
 * Repository for authentication operations
 *
 * @param tokenStorage Storage for access and refresh tokens
 * @param httpClient Shared HTTP client instance
 */
class AuthRepository(
    private val tokenStorage: TokenStorage,
    private val httpClient: HttpClient
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Parse error response from API
     */
    private suspend fun parseErrorMessage(response: HttpResponse): String {
        return try {
            val errorResponse = response.body<ApiErrorResponse>()
            errorResponse.message
        } catch (e: Exception) {
            response.status.description
        }
    }

    /**
     * Register a new user
     * @return OtpSentResponse with OTP details
     */
    suspend fun register(request: RegisterRequest): Result<OtpSentResponse> {
        return try {
            AppLogger.info(TAG, "Registering user: ${request.username}")

            val response = httpClient.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val otpResponse = response.body<OtpSentResponse>()
                AppLogger.info(TAG, "Registration successful, OTP sent to: ${otpResponse.identifier}")
                Result.success(otpResponse)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Registration failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Registration error", e)
            Result.failure(e)
        }
    }

    /**
     * Login existing user (sends OTP)
     * @return OtpSentResponse with OTP details
     */
    suspend fun login(identifier: String, identifierType: String): Result<OtpSentResponse> {
        return try {
            AppLogger.info(TAG, "Login attempt for: $identifierType")

            val request = LoginRequest(
                email = if (identifierType == "email") identifier else null,
                phone = if (identifierType == "phone") identifier else null
            )
            val response = httpClient.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val otpResponse = response.body<OtpSentResponse>()
                AppLogger.info(TAG, "Login OTP sent to: ${otpResponse.identifier}")
                Result.success(otpResponse)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Login failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Login error", e)
            Result.failure(e)
        }
    }

    /**
     * Verify OTP code
     * @return AuthResponse with tokens and user info
     */
    suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> {
        return try {
            val response = httpClient.post("/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()

                // Log authentication success
                AppLogger.info(TAG, "OTP verification successful for user: ${authResponse.user.username}")

                // Log tokens only in development mode
                AppLogger.debugSensitive(TAG, "Access Token: ${authResponse.accessToken}")
                AppLogger.debugSensitive(TAG, "Refresh Token: ${authResponse.refreshToken}")

                // Save tokens
                tokenStorage.saveAccessToken(authResponse.accessToken)
                tokenStorage.saveRefreshToken(authResponse.refreshToken)

                AppLogger.debug(TAG, "Tokens saved to storage")
                Result.success(authResponse)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with Google
     * @return AuthResponse with tokens and user info
     */
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            val response = httpClient.post("/auth/login/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleLoginRequest(idToken))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                // Save tokens
                tokenStorage.saveAccessToken(authResponse.accessToken)
                tokenStorage.saveRefreshToken(authResponse.refreshToken)
                Result.success(authResponse)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh access token
     * @return TokenResponse with new tokens
     */
    suspend fun refreshToken(): Result<TokenResponse> {
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))

            val response = httpClient.post("/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }

            if (response.status.isSuccess()) {
                val tokenResponse = response.body<TokenResponse>()
                // Save new tokens
                tokenStorage.saveAccessToken(tokenResponse.accessToken)
                tokenStorage.saveRefreshToken(tokenResponse.refreshToken)
                Result.success(tokenResponse)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user (revoke tokens on server and clear locally)
     */
    suspend fun logout() {
        try {
            // Get access token before clearing
            val accessToken = tokenStorage.getAccessToken()

            if (accessToken != null) {
                // Call server logout endpoint to revoke tokens
                httpClient.post("/auth/logout") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue with local logout
            AppLogger.error(TAG, "Server logout failed: ${e.message}")
        } finally {
            // Always clear local tokens
            tokenStorage.clearTokens()
        }
    }

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return tokenStorage.isAuthenticated()
    }

    /**
     * Get current access token
     */
    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }
}

