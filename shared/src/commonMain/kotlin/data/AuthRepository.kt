package data

import domain.model.auth.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import util.AppLogger
import util.LoggerConfig

/**
 * Repository for authentication operations
 *
 * @param tokenStorage Storage for access and refresh tokens
 * @param baseUrl Base URL for the API
 * @param httpClient Optional HTTP client for testing (if null, creates default client)
 */
class AuthRepository(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String = "http://localhost:8080",
    httpClient: HttpClient? = null
) {
    companion object {
        private const val TAG = "AuthRepository"
    }
    private val client = httpClient ?: HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            // Use BODY in development, INFO in production
            level = if (LoggerConfig.logSensitiveData) LogLevel.BODY else LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }
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

            val response = client.post("$baseUrl/auth/register") {
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
            val response = client.post("$baseUrl/auth/login") {
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
            val response = client.post("$baseUrl/auth/verify-otp") {
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
            val response = client.post("$baseUrl/auth/login/google") {
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

            val response = client.post("$baseUrl/auth/refresh") {
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
     * Logout user (clear tokens)
     */
    suspend fun logout() {
        tokenStorage.clearTokens()
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

    /**
     * Close HTTP client
     */
    fun close() {
        client.close()
    }
}

