package domain.validation

/**
 * Post validation logic
 */
object PostValidation {
    private const val CONTENT_MIN_LENGTH = 1
    private const val CONTENT_MAX_LENGTH = 500
    private const val MAX_MEDIA_COUNT = 4
    private const val MAX_MEDIA_SIZE_MB = 10

    /**
     * Validate post content
     */
    fun validateContent(content: String): ValidationResult {
        return when {
            content.isBlank() -> ValidationResult.failure("Post content cannot be empty")
            content.length < CONTENT_MIN_LENGTH -> 
                ValidationResult.failure("Post content must be at least $CONTENT_MIN_LENGTH character")
            content.length > CONTENT_MAX_LENGTH -> 
                ValidationResult.failure("Post content must be at most $CONTENT_MAX_LENGTH characters")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate media URLs
     */
    fun validateMediaUrls(mediaUrls: List<String>, isPremium: Boolean): ValidationResult {
        return when {
            mediaUrls.isNotEmpty() && !isPremium -> 
                ValidationResult.failure("Media uploads are only available for premium users")
            mediaUrls.size > MAX_MEDIA_COUNT -> 
                ValidationResult.failure("Maximum $MAX_MEDIA_COUNT images allowed per post")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate post creation
     */
    fun validateCreatePost(
        content: String,
        mediaUrls: List<String>,
        isPremium: Boolean
    ): ValidationResult {
        validateContent(content).let { if (!it.isValid) return it }
        validateMediaUrls(mediaUrls, isPremium).let { if (!it.isValid) return it }
        
        return ValidationResult.success()
    }
}

/**
 * Comment validation logic
 */
object CommentValidation {
    private const val CONTENT_MIN_LENGTH = 1
    private const val CONTENT_MAX_LENGTH = 500

    /**
     * Validate comment content
     */
    fun validateContent(content: String): ValidationResult {
        return when {
            content.isBlank() -> ValidationResult.failure("Comment cannot be empty")
            content.length < CONTENT_MIN_LENGTH -> 
                ValidationResult.failure("Comment must be at least $CONTENT_MIN_LENGTH character")
            content.length > CONTENT_MAX_LENGTH -> 
                ValidationResult.failure("Comment must be at most $CONTENT_MAX_LENGTH characters")
            else -> ValidationResult.success()
        }
    }
}

