package domain.model

import kotlinx.serialization.Serializable

/**
 * Response for file upload
 */
@Serializable
data class UploadResponse(
    val url: String,
    val fileName: String,
    val contentType: String,
    val size: Long
)

