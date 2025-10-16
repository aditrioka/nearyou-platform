package domain.validation

/**
 * Message validation logic
 */
object MessageValidation {
    private const val CONTENT_MIN_LENGTH = 1
    private const val CONTENT_MAX_LENGTH = 2000

    /**
     * Validate message content
     */
    fun validateContent(content: String): ValidationResult {
        return when {
            content.isBlank() -> ValidationResult.failure("Message cannot be empty")
            content.length < CONTENT_MIN_LENGTH -> 
                ValidationResult.failure("Message must be at least $CONTENT_MIN_LENGTH character")
            content.length > CONTENT_MAX_LENGTH -> 
                ValidationResult.failure("Message must be at most $CONTENT_MAX_LENGTH characters")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate send message request
     */
    fun validateSendMessage(
        conversationId: String?,
        recipientId: String?,
        content: String
    ): ValidationResult {
        if (conversationId == null && recipientId == null) {
            return ValidationResult.failure("Either conversationId or recipientId must be provided")
        }
        
        return validateContent(content)
    }
}

