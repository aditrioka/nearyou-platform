package data

import domain.model.User
import domain.model.UpdateUserRequest
import domain.model.UploadResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import util.AppLogger

/**
 * Repository for user profile operations
 *
 * @param tokenStorage Storage for access and refresh tokens
 * @param httpClient Shared HTTP client instance
 */
class UserRepository(
    private val tokenStorage: TokenStorage,
    private val httpClient: HttpClient
) {
    companion object {
        private const val TAG = "UserRepository"
    }

    /**
     * Parse error response from API
     */
    private suspend fun parseErrorMessage(response: HttpResponse): String {
        return try {
            val errorResponse = response.body<ApiErrorResponse>()
            errorResponse.message
        } catch (e: Exception) {
            response.status.description
        }
    }

    /**
     * Get current user's profile
     * @return User profile or error
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            AppLogger.debug(TAG, "Fetching current user profile")

            val accessToken = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = httpClient.get("/users/me") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }

            if (response.status.isSuccess()) {
                val user = response.body<User>()
                AppLogger.info(TAG, "User profile fetched: ${user.username}")
                Result.success(user)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Failed to fetch user profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Error fetching user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update current user's profile
     * @param request Update request with optional fields
     * @return Updated user profile or error
     */
    suspend fun updateCurrentUser(request: UpdateUserRequest): Result<User> {
        return try {
            AppLogger.info(TAG, "Updating user profile")

            val accessToken = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = httpClient.put("/users/me") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val user = response.body<User>()
                AppLogger.info(TAG, "User profile updated successfully: ${user.username}")
                Result.success(user)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Failed to update user profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Upload profile photo
     * @param imageBytes Image data as ByteArray
     * @param fileName Original file name
     * @param contentType MIME type of the image
     * @return Upload response with URL or error
     */
    suspend fun uploadProfilePhoto(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String = "image/jpeg"
    ): Result<UploadResponse> {
        return try {
            AppLogger.info(TAG, "Uploading profile photo: $fileName")

            val accessToken = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = httpClient.post("/upload/profile-photo") {
                bearerAuth(accessToken)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    )
                )
            }

            if (response.status.isSuccess()) {
                val uploadResponse = response.body<UploadResponse>()
                AppLogger.info(TAG, "Profile photo uploaded successfully: ${uploadResponse.url}")
                Result.success(uploadResponse)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Failed to upload profile photo: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Error uploading profile photo", e)
            Result.failure(e)
        }
    }
}

