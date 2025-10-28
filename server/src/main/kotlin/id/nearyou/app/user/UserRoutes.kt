package id.nearyou.app.user

import domain.model.UpdateUserRequest
import id.nearyou.app.exceptions.AuthenticationException
import id.nearyou.app.exceptions.NotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

/**
 * Configure user routes
 * UserService is injected via Koin
 */
fun Route.userRoutes() {
    val userService = application.get<UserService>()
    
    route("/users") {
        
        /**
         * GET /users/me
         * Get authenticated user's profile
         * Requires JWT authentication
         */
        authenticate("auth-jwt") {
            get("/me") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")
                
                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")
                
                // Get user profile
                val user = userService.getUserById(userId)
                    ?: throw NotFoundException("User not found", "USER_NOT_FOUND")
                
                call.respond(HttpStatusCode.OK, user)
            }
            
            /**
             * PUT /users/me
             * Update authenticated user's profile
             * Requires JWT authentication
             */
            put("/me") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")
                
                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")
                
                // Parse request body
                val request = call.receive<UpdateUserRequest>()
                
                // Update user profile
                val updatedUser = userService.updateUserProfile(userId, request)
                
                call.respond(HttpStatusCode.OK, updatedUser)
            }
        }
    }
}

