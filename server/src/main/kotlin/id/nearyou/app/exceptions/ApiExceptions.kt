package id.nearyou.app.exceptions

import kotlinx.serialization.Serializable

/**
 * Base exception for all API errors
 */
sealed class ApiException(
    message: String,
    val statusCode: Int,
    val errorCode: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Validation errors (400 Bad Request)
 */
class ValidationException(
    message: String,
    errorCode: String = "VALIDATION_ERROR"
) : ApiException(message, 400, errorCode)

/**
 * Authentication errors (401 Unauthorized)
 */
class AuthenticationException(
    message: String,
    errorCode: String = "AUTHENTICATION_ERROR"
) : ApiException(message, 401, errorCode)

/**
 * Authorization errors (403 Forbidden)
 */
class AuthorizationException(
    message: String,
    errorCode: String = "AUTHORIZATION_ERROR"
) : ApiException(message, 403, errorCode)

/**
 * Resource not found errors (404 Not Found)
 */
class NotFoundException(
    message: String,
    errorCode: String = "NOT_FOUND"
) : ApiException(message, 404, errorCode)

/**
 * Conflict errors (409 Conflict)
 */
class ConflictException(
    message: String,
    errorCode: String = "CONFLICT"
) : ApiException(message, 409, errorCode)

/**
 * Rate limiting errors (429 Too Many Requests)
 */
class RateLimitException(
    message: String,
    errorCode: String = "RATE_LIMIT_EXCEEDED"
) : ApiException(message, 429, errorCode)

/**
 * Internal server errors (500 Internal Server Error)
 */
class InternalServerException(
    message: String,
    errorCode: String = "INTERNAL_SERVER_ERROR",
    cause: Throwable? = null
) : ApiException(message, 500, errorCode, cause)

/**
 * Service unavailable errors (503 Service Unavailable)
 */
class ServiceUnavailableException(
    message: String,
    errorCode: String = "SERVICE_UNAVAILABLE"
) : ApiException(message, 503, errorCode)

/**
 * Standardized error response
 */
@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, String>? = null
)

