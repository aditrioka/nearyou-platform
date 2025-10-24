package id.nearyou.app.auth

import domain.model.auth.*
import id.nearyou.app.exceptions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

/**
 * Configure authentication routes
 * AuthService is injected via Koin
 */
fun Route.authRoutes() {
    val authService = application.get<AuthService>()
    
    route("/auth") {
        
        /**
         * POST /auth/register
         * Register a new user with email or phone
         * Sends OTP for verification
         */
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = authService.registerUser(request)

            result.fold(
                onSuccess = { response ->
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { error ->
                    throw ValidationException(error.message ?: "Registration failed", "REGISTRATION_FAILED")
                }
            )
        }

        /**
         * POST /auth/login
         * Login existing user with email or phone
         * Sends OTP for verification
         */
        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.loginUser(request)

            result.fold(
                onSuccess = { response ->
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { error ->
                    // Re-throw the original exception if it's already an ApiException
                    // This preserves the specific error code (e.g., VERIFICATION_PENDING)
                    when (error) {
                        is ApiException -> throw error
                        else -> throw AuthenticationException(error.message ?: "Login failed", "LOGIN_FAILED")
                    }
                }
            )
        }

        /**
         * POST /auth/verify-otp
         * Verify OTP code and complete registration or login
         * Returns access token and refresh token
         */
        post("/verify-otp") {
            val request = call.receive<VerifyOtpRequest>()
            val result = authService.verifyOtp(request)

            result.fold(
                onSuccess = { response ->
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { error ->
                    throw AuthenticationException(error.message ?: "OTP verification failed", "VERIFICATION_FAILED")
                }
            )
        }
        
        /**
         * POST /auth/refresh
         * Refresh access token using refresh token
         * Returns new access token and refresh token
         */
        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val result = authService.refreshToken(request)

            result.fold(
                onSuccess = { response ->
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { error ->
                    throw AuthenticationException(error.message ?: "Token refresh failed", "REFRESH_FAILED")
                }
            )
        }
        
        /**
         * POST /auth/login/google
         * Login with Google OAuth
         * Placeholder for future implementation
         */
        post("/login/google") {
            val request = call.receive<GoogleLoginRequest>()

            // TODO: Implement Google OAuth verification
            // 1. Verify idToken with Google
            // 2. Extract user info (email, name, etc.)
            // 3. Create or find user in database
            // 4. Generate JWT tokens
            // 5. Return AuthResponse

            throw ServiceUnavailableException("Google OAuth login not yet implemented", "NOT_IMPLEMENTED")
        }
    }
}

