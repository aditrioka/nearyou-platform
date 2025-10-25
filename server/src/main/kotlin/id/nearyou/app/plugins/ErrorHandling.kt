package id.nearyou.app.plugins

import id.nearyou.app.exceptions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

/**
 * Configure centralized error handling using StatusPages plugin
 * 
 * This provides consistent error responses across the entire API
 * and proper logging of exceptions.
 */
fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandling")
    
    install(StatusPages) {
        // Handle custom API exceptions
        exception<ApiException> { call, cause ->
            logger.warn("API Exception: ${cause.errorCode} - ${cause.message}")
            
            call.respond(
                HttpStatusCode.fromValue(cause.statusCode),
                ErrorResponse(
                    error = ErrorDetail(
                        code = cause.errorCode,
                        message = cause.message ?: "An error occurred"
                    )
                )
            )
        }
        
        // Handle validation exceptions specifically
        exception<ValidationException> { call, cause ->
            logger.warn("Validation error: ${cause.message}")
            
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = ErrorDetail(
                        code = cause.errorCode,
                        message = cause.message ?: "Validation failed"
                    )
                )
            )
        }
        
        // Handle authentication exceptions
        exception<AuthenticationException> { call, cause ->
            logger.warn("Authentication error: ${cause.message}")
            
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = ErrorDetail(
                        code = cause.errorCode,
                        message = cause.message ?: "Authentication failed"
                    )
                )
            )
        }
        
        // Handle not found exceptions
        exception<NotFoundException> { call, cause ->
            logger.info("Resource not found: ${cause.message}")
            
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = ErrorDetail(
                        code = cause.errorCode,
                        message = cause.message ?: "Resource not found"
                    )
                )
            )
        }
        
        // Handle serialization errors
        exception<SerializationException> { call, cause ->
            logger.error("Serialization error", cause)
            
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "SERIALIZATION_ERROR",
                        message = "Invalid request format: ${cause.message}"
                    )
                )
            )
        }
        
        // Handle illegal argument exceptions
        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Illegal argument: ${cause.message}")
            
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "INVALID_ARGUMENT",
                        message = cause.message ?: "Invalid argument provided"
                    )
                )
            )
        }
        
        // Handle null pointer exceptions (should be rare with Kotlin)
        exception<NullPointerException> { call, cause ->
            logger.error("Null pointer exception", cause)
            
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "INTERNAL_ERROR",
                        message = "An internal error occurred"
                    )
                )
            )
        }
        
        // Catch-all for unexpected exceptions
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "An unexpected error occurred"
                    )
                )
            )
        }
        
        // Handle 404 Not Found for routes
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "ROUTE_NOT_FOUND",
                        message = "The requested endpoint does not exist"
                    )
                )
            )
        }
        
        // Handle 405 Method Not Allowed
        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "METHOD_NOT_ALLOWED",
                        message = "The HTTP method is not allowed for this endpoint"
                    )
                )
            )
        }
    }
}

