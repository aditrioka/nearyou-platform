package id.nearyou.app.upload

import id.nearyou.app.exceptions.AuthenticationException
import id.nearyou.app.exceptions.ValidationException
import id.nearyou.app.storage.StorageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get

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

/**
 * Configure upload routes
 * StorageService is injected via Koin
 */
fun Route.uploadRoutes() {
    val storageService = application.get<StorageService>()

    route("/upload") {
        
        /**
         * POST /upload/profile-photo
         * Upload profile photo
         * Requires JWT authentication
         * Accepts multipart/form-data with 'file' field
         */
        authenticate("auth-jwt") {
            post("/profile-photo") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")
                
                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")
                
                // Parse multipart data
                val multipart = call.receiveMultipart()
                var fileUrl: String? = null
                var fileName: String? = null
                var contentType: String? = null
                var fileSize: Long = 0
                
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            // Validate file
                            val originalFileName = part.originalFileName ?: "unknown"
                            val partContentType = part.contentType?.toString() ?: "application/octet-stream"
                            
                            // Validate content type (only images)
                            if (!partContentType.startsWith("image/")) {
                                part.dispose()
                                throw ValidationException(
                                    "Only image files are allowed",
                                    "INVALID_FILE_TYPE"
                                )
                            }
                            
                            // Validate file size (max 5MB)
                            val maxSize = 5 * 1024 * 1024 // 5MB
                            val inputStream = part.streamProvider()
                            val bytes = inputStream.readBytes()
                            fileSize = bytes.size.toLong()
                            
                            if (fileSize > maxSize) {
                                part.dispose()
                                throw ValidationException(
                                    "File size exceeds maximum allowed size of 5MB",
                                    "FILE_TOO_LARGE"
                                )
                            }
                            
                            // Upload file
                            fileUrl = storageService.uploadFile(
                                fileName = originalFileName,
                                contentType = partContentType,
                                inputStream = bytes.inputStream()
                            )
                            
                            fileName = originalFileName
                            contentType = partContentType
                            
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }
                
                // Check if file was uploaded
                if (fileUrl == null) {
                    throw ValidationException(
                        "No file provided",
                        "MISSING_FILE"
                    )
                }
                
                // Return upload response
                call.respond(
                    HttpStatusCode.OK,
                    UploadResponse(
                        url = fileUrl!!,
                        fileName = fileName ?: "unknown",
                        contentType = contentType ?: "application/octet-stream",
                        size = fileSize
                    )
                )
            }
        }
    }
}

