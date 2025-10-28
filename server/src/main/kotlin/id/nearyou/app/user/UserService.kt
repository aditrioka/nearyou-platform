package id.nearyou.app.user

import domain.model.User
import domain.model.UpdateUserRequest
import domain.validation.UserValidation
import id.nearyou.app.exceptions.NotFoundException
import id.nearyou.app.exceptions.ValidationException
import id.nearyou.app.repository.UserRepository

/**
 * User service handling user profile operations
 */
class UserService {

    /**
     * Get user by ID
     * @param userId User ID
     * @return User or null if not found
     */
    fun getUserById(userId: String): User? {
        return UserRepository.findById(userId)
    }

    /**
     * Update user profile
     * @param userId User ID
     * @param request Update request with optional fields
     * @return Updated user
     * @throws NotFoundException if user not found
     * @throws ValidationException if validation fails
     */
    fun updateUserProfile(userId: String, request: UpdateUserRequest): User {
        // Validate input using shared validation
        request.displayName?.let { name ->
            val result = UserValidation.validateDisplayName(name)
            if (!result.isValid) {
                throw ValidationException(
                    result.error ?: "Invalid display name",
                    "INVALID_DISPLAY_NAME"
                )
            }
        }

        request.bio?.let { bio ->
            val result = UserValidation.validateBio(bio)
            if (!result.isValid) {
                throw ValidationException(
                    result.error ?: "Invalid bio",
                    "INVALID_BIO"
                )
            }
        }

        request.profilePhotoUrl?.let { url ->
            if (url.isBlank()) {
                throw ValidationException(
                    "Profile photo URL cannot be blank",
                    "INVALID_PHOTO_URL"
                )
            }
        }
        
        // Update user
        val updatedUser = UserRepository.updateUser(
            userId = userId,
            displayName = request.displayName,
            bio = request.bio,
            profilePhotoUrl = request.profilePhotoUrl
        )
        
        return updatedUser ?: throw NotFoundException(
            "User not found",
            "USER_NOT_FOUND"
        )
    }
}

