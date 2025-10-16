package domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * User domain model representing a user account in the system.
 *
 * @property id Unique identifier for the user
 * @property username Unique username (3-20 characters, alphanumeric + underscore)
 * @property displayName Display name shown to other users (1-50 characters)
 * @property email Email address (optional, required if phone is null)
 * @property phone Phone number (optional, required if email is null)
 * @property bio User biography (0-200 characters)
 * @property profilePhotoUrl URL to user's profile photo
 * @property isVerified Whether the user has verified their account
 * @property subscriptionTier Current subscription tier (free or premium)
 * @property createdAt Timestamp when the user account was created
 * @property updatedAt Timestamp when the user account was last updated
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val bio: String? = null,
    val profilePhotoUrl: String? = null,
    val isVerified: Boolean = false,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(email != null || phone != null) {
            "User must have either email or phone"
        }
    }

    /**
     * Check if user is a premium subscriber
     */
    val isPremium: Boolean
        get() = subscriptionTier == SubscriptionTier.PREMIUM

    /**
     * Check if user is a free tier user
     */
    val isFree: Boolean
        get() = subscriptionTier == SubscriptionTier.FREE
}

/**
 * Subscription tier enumeration
 */
@Serializable
enum class SubscriptionTier {
    FREE,
    PREMIUM
}

/**
 * User creation request model
 */
@Serializable
data class CreateUserRequest(
    val username: String,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val bio: String? = null
)

/**
 * User update request model
 */
@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val profilePhotoUrl: String? = null
)

/**
 * User profile summary (for displaying in lists)
 */
@Serializable
data class UserSummary(
    val id: String,
    val username: String,
    val displayName: String,
    val profilePhotoUrl: String? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE
)

