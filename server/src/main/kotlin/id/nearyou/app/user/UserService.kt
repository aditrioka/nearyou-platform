package id.nearyou.app.user

import domain.model.User
import domain.model.UpdateUserRequest
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
        // Validate input
        request.displayName?.let { name ->
            if (name.isBlank() || name.length > 50) {
                throw ValidationException(
                    "Display name must be between 1 and 50 characters",
                    "INVALID_DISPLAY_NAME"
                )
            }
        }
        
        request.bio?.let { bio ->
            if (bio.length > 200) {
                throw ValidationException(
                    "Bio must not exceed 200 characters",
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

