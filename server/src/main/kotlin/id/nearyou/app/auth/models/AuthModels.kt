package id.nearyou.app.auth.models

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
    val password: String? = null // Optional, for email registration
) {
    init {
        require(email != null || phone != null) {
            "Either email or phone must be provided"
        }
        require(username.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))) {
            "Username must be 3-20 characters and contain only letters, numbers, and underscores"
        }
        require(displayName.length in 1..50) {
            "Display name must be 1-50 characters"
        }
        if (email != null) {
            require(email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                "Invalid email format"
            }
        }
        if (phone != null) {
            require(phone.matches(Regex("^\\+?[1-9]\\d{1,14}$"))) {
                "Invalid phone format (use E.164 format)"
            }
        }
    }
}

/**
 * Request to verify OTP code
 */
@Serializable
data class VerifyOtpRequest(
    val identifier: String, // email or phone
    val code: String,
    val type: String // "email" or "phone"
) {
    init {
        require(code.matches(Regex("^\\d{6}$"))) {
            "OTP code must be 6 digits"
        }
        require(type in listOf("email", "phone")) {
            "Type must be 'email' or 'phone'"
        }
    }
}

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
 * Response containing authentication tokens
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

/**
 * Response containing only tokens (for refresh)
 */
@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

/**
 * User data transfer object
 */
@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val bio: String? = null,
    val profilePhotoUrl: String? = null,
    val isVerified: Boolean,
    val subscriptionTier: String
)

/**
 * Generic error response
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

/**
 * Success response for OTP sent
 */
@Serializable
data class OtpSentResponse(
    val message: String,
    val identifier: String,
    val type: String,
    val expiresInSeconds: Int
)

