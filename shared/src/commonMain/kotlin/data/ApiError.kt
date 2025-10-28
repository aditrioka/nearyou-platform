package data

import kotlinx.serialization.Serializable

/**
 * Simple error response for parsing API errors
 */
@Serializable
internal data class ApiErrorResponse(
    val error: String,
    val message: String
)

