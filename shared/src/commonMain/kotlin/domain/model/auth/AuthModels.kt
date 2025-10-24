package domain.model.auth

import domain.model.User
import kotlinx.serialization.Serializable

/**
 * Request to register a new user
 */
@Serializable
data class RegisterRequest(
    val username: String,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null
)

/**
 * Request to login existing user (sends OTP)
 */
@Serializable
data class LoginRequest(
    val email: String? = null,
    val phone: String? = null
)

/**
 * Request to verify OTP code
 */
@Serializable
data class VerifyOtpRequest(
    val identifier: String, // email or phone
    val code: String,
    val type: String // "email" or "phone"
)

/**
 * Request to refresh access token
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * Request for Google OAuth login
 */
@Serializable
data class GoogleLoginRequest(
    val idToken: String
)

/**
 * Response when OTP is sent
 */
@Serializable
data class OtpSentResponse(
    val message: String,
    val identifier: String,
    val type: String,
    val expiresInSeconds: Int
)

/**
 * Authentication response with tokens and user info
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

/**
 * Token refresh response
 */
@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

