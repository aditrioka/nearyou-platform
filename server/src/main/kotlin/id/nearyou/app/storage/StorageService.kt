package id.nearyou.app.storage

import java.io.File
import java.io.InputStream
import java.util.UUID

/**
 * Storage service interface for file uploads
 * Can be implemented for local storage, S3, GCS, etc.
 */
interface StorageService {
    /**
     * Upload a file
     * @param fileName Original file name
     * @param contentType MIME type of the file
     * @param inputStream File content stream
     * @return URL of the uploaded file
     */
    suspend fun uploadFile(
        fileName: String,
        contentType: String,
        inputStream: InputStream
    ): String

    /**
     * Delete a file
     * @param fileUrl URL of the file to delete
     */
    suspend fun deleteFile(fileUrl: String)

    /**
     * Check if a file exists
     * @param fileUrl URL of the file
     * @return true if file exists
     */
    suspend fun fileExists(fileUrl: String): Boolean
}

/**
 * Local file storage implementation for development/MVP
 * Stores files in a local directory and serves them via HTTP
 */
class LocalStorageService(
    private val uploadDir: String = "uploads",
    private val baseUrl: String = "http://localhost:8080"
) : StorageService {

    init {
        // Create upload directory if it doesn't exist
        val dir = File(uploadDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    override suspend fun uploadFile(
        fileName: String,
        contentType: String,
        inputStream: InputStream
    ): String {
        // Generate unique file name to avoid conflicts
        val extension = fileName.substringAfterLast(".", "")
        val uniqueFileName = "${UUID.randomUUID()}${if (extension.isNotEmpty()) ".$extension" else ""}"
        
        // Create subdirectory based on file type
        val subDir = when {
            contentType.startsWith("image/") -> "images"
            else -> "files"
        }
        
        val targetDir = File(uploadDir, subDir)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        
        // Save file
        val targetFile = File(targetDir, uniqueFileName)
        inputStream.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        // Return URL
        return "$baseUrl/uploads/$subDir/$uniqueFileName"
    }

    override suspend fun deleteFile(fileUrl: String) {
        try {
            // Extract file path from URL
            val path = fileUrl.substringAfter("/uploads/")
            val file = File(uploadDir, path)
            
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log error but don't throw - file might already be deleted
            println("Error deleting file: ${e.message}")
        }
    }

    override suspend fun fileExists(fileUrl: String): Boolean {
        return try {
            val path = fileUrl.substringAfter("/uploads/")
            val file = File(uploadDir, path)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
}

