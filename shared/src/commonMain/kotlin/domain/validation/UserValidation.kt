package domain.validation

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val error: String? = null
) {
    companion object {
        fun success() = ValidationResult(true)
        fun failure(error: String) = ValidationResult(false, error)
    }
}

/**
 * User validation logic
 */
object UserValidation {
    private const val USERNAME_MIN_LENGTH = 3
    private const val USERNAME_MAX_LENGTH = 20
    private const val DISPLAY_NAME_MIN_LENGTH = 1
    private const val DISPLAY_NAME_MAX_LENGTH = 50
    private const val BIO_MAX_LENGTH = 200
    private const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    private const val PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$" // E.164 format
    private const val USERNAME_REGEX = "^[a-zA-Z0-9_]+$"

    /**
     * Validate username
     */
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.failure("Username cannot be empty")
            username.length < USERNAME_MIN_LENGTH -> 
                ValidationResult.failure("Username must be at least $USERNAME_MIN_LENGTH characters")
            username.length > USERNAME_MAX_LENGTH -> 
                ValidationResult.failure("Username must be at most $USERNAME_MAX_LENGTH characters")
            !username.matches(Regex(USERNAME_REGEX)) -> 
                ValidationResult.failure("Username can only contain alphanumeric characters and underscore")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate display name
     */
    fun validateDisplayName(displayName: String): ValidationResult {
        return when {
            displayName.isBlank() -> ValidationResult.failure("Display name cannot be empty")
            displayName.length < DISPLAY_NAME_MIN_LENGTH -> 
                ValidationResult.failure("Display name must be at least $DISPLAY_NAME_MIN_LENGTH character")
            displayName.length > DISPLAY_NAME_MAX_LENGTH -> 
                ValidationResult.failure("Display name must be at most $DISPLAY_NAME_MAX_LENGTH characters")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate email
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.failure("Email cannot be empty")
            !email.matches(Regex(EMAIL_REGEX)) -> 
                ValidationResult.failure("Invalid email format")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate phone number
     */
    fun validatePhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult.failure("Phone number cannot be empty")
            !phone.matches(Regex(PHONE_REGEX)) -> 
                ValidationResult.failure("Invalid phone number format (use E.164 format)")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate bio
     */
    fun validateBio(bio: String): ValidationResult {
        return when {
            bio.length > BIO_MAX_LENGTH -> 
                ValidationResult.failure("Bio must be at most $BIO_MAX_LENGTH characters")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate user creation request
     */
    fun validateCreateUser(
        username: String,
        displayName: String,
        email: String?,
        phone: String?,
        bio: String?
    ): ValidationResult {
        validateUsername(username).let { if (!it.isValid) return it }
        validateDisplayName(displayName).let { if (!it.isValid) return it }
        
        if (email == null && phone == null) {
            return ValidationResult.failure("Either email or phone must be provided")
        }
        
        email?.let { validateEmail(it).let { result -> if (!result.isValid) return result } }
        phone?.let { validatePhone(it).let { result -> if (!result.isValid) return result } }
        bio?.let { validateBio(it).let { result -> if (!result.isValid) return result } }
        
        return ValidationResult.success()
    }
}

