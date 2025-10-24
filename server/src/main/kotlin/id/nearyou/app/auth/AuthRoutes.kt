package id.nearyou.app.auth

import domain.model.auth.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Configure authentication routes
 * AuthService is injected via Koin
 */
fun Route.authRoutes() {
    val authService: AuthService by inject()
    
    route("/auth") {
        
        /**
         * POST /auth/register
         * Register a new user with email or phone
         * Sends OTP for verification
         */
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                val result = authService.registerUser(request)

                result.fold(
                    onSuccess = { response ->
                        call.respond(HttpStatusCode.OK, response)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                error = "registration_failed",
                                message = error.message ?: "Registration failed"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = e.message ?: "Invalid request"
                    )
                )
            }
        }

        /**
         * POST /auth/login
         * Login existing user with email or phone
         * Sends OTP for verification
         */
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                val result = authService.loginUser(request)

                result.fold(
                    onSuccess = { response ->
                        call.respond(HttpStatusCode.OK, response)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                error = "login_failed",
                                message = error.message ?: "Login failed"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = e.message ?: "Invalid request"
                    )
                )
            }
        }

        /**
         * POST /auth/verify-otp
         * Verify OTP code and complete registration or login
         * Returns access token and refresh token
         */
        post("/verify-otp") {
            try {
                val request = call.receive<VerifyOtpRequest>()
                
                val result = authService.verifyOtp(request)
                
                result.fold(
                    onSuccess = { response ->
                        call.respond(HttpStatusCode.OK, response)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(
                                error = "verification_failed",
                                message = error.message ?: "OTP verification failed"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = e.message ?: "Invalid request"
                    )
                )
            }
        }
        
        /**
         * POST /auth/refresh
         * Refresh access token using refresh token
         * Returns new access token and refresh token
         */
        post("/refresh") {
            try {
                val request = call.receive<RefreshTokenRequest>()
                
                val result = authService.refreshToken(request)
                
                result.fold(
                    onSuccess = { response ->
                        call.respond(HttpStatusCode.OK, response)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(
                                error = "refresh_failed",
                                message = error.message ?: "Token refresh failed"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = e.message ?: "Invalid request"
                    )
                )
            }
        }
        
        /**
         * POST /auth/login/google
         * Login with Google OAuth
         * Placeholder for future implementation
         */
        post("/login/google") {
            try {
                val request = call.receive<GoogleLoginRequest>()
                
                // TODO: Implement Google OAuth verification
                // 1. Verify idToken with Google
                // 2. Extract user info (email, name, etc.)
                // 3. Create or find user in database
                // 4. Generate JWT tokens
                // 5. Return AuthResponse
                
                call.respond(
                    HttpStatusCode.NotImplemented,
                    ErrorResponse(
                        error = "not_implemented",
                        message = "Google OAuth login not yet implemented"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = e.message ?: "Invalid request"
                    )
                )
            }
        }
    }
}

