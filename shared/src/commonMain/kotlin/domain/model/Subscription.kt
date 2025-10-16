package domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Subscription domain model
 *
 * @property id Unique identifier for the subscription
 * @property userId ID of the user
 * @property tier Subscription tier
 * @property startedAt When the subscription started
 * @property expiresAt When the subscription expires (null for lifetime)
 * @property isActive Whether the subscription is currently active
 * @property createdAt Timestamp when the subscription was created
 * @property updatedAt Timestamp when the subscription was last updated
 */
@Serializable
data class Subscription(
    val id: String,
    val userId: String,
    val tier: SubscriptionTier,
    val startedAt: Instant,
    val expiresAt: Instant? = null,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * Check if subscription is expired
     */
    fun isExpired(now: Instant): Boolean {
        return expiresAt?.let { it < now } ?: false
    }

    /**
     * Check if subscription is premium
     */
    val isPremium: Boolean
        get() = tier == SubscriptionTier.PREMIUM && isActive
}

/**
 * Usage quota for different subscription tiers
 */
object SubscriptionQuota {
    // Free tier quotas
    const val FREE_POSTS_PER_DAY = 100
    const val FREE_CHATS_PER_DAY = 500
    const val FREE_MEDIA_UPLOADS = false
    const val FREE_SEARCH_ACCESS = false

    // Premium tier quotas
    const val PREMIUM_POSTS_PER_DAY = Int.MAX_VALUE // Unlimited
    const val PREMIUM_CHATS_PER_DAY = Int.MAX_VALUE // Unlimited
    const val PREMIUM_MEDIA_UPLOADS = true
    const val PREMIUM_SEARCH_ACCESS = true

    /**
     * Get posts quota for a subscription tier
     */
    fun getPostsQuota(tier: SubscriptionTier): Int {
        return when (tier) {
            SubscriptionTier.FREE -> FREE_POSTS_PER_DAY
            SubscriptionTier.PREMIUM -> PREMIUM_POSTS_PER_DAY
        }
    }

    /**
     * Get chats quota for a subscription tier
     */
    fun getChatsQuota(tier: SubscriptionTier): Int {
        return when (tier) {
            SubscriptionTier.FREE -> FREE_CHATS_PER_DAY
            SubscriptionTier.PREMIUM -> PREMIUM_CHATS_PER_DAY
        }
    }

    /**
     * Check if media uploads are allowed for a subscription tier
     */
    fun canUploadMedia(tier: SubscriptionTier): Boolean {
        return when (tier) {
            SubscriptionTier.FREE -> FREE_MEDIA_UPLOADS
            SubscriptionTier.PREMIUM -> PREMIUM_MEDIA_UPLOADS
        }
    }

    /**
     * Check if search is allowed for a subscription tier
     */
    fun canSearch(tier: SubscriptionTier): Boolean {
        return when (tier) {
            SubscriptionTier.FREE -> FREE_SEARCH_ACCESS
            SubscriptionTier.PREMIUM -> PREMIUM_SEARCH_ACCESS
        }
    }
}

/**
 * Usage log for tracking daily quotas
 */
@Serializable
data class UsageLog(
    val id: String,
    val userId: String,
    val actionType: UsageActionType,
    val actionDate: String, // ISO date string (YYYY-MM-DD)
    val count: Int,
    val createdAt: Instant
)

/**
 * Usage action types
 */
@Serializable
enum class UsageActionType {
    POST,
    CHAT,
    SEARCH
}

/**
 * Subscription upgrade request
 */
@Serializable
data class SubscriptionUpgradeRequest(
    val tier: SubscriptionTier,
    val paymentToken: String? = null // For future payment integration
)

/**
 * Subscription status response
 */
@Serializable
data class SubscriptionStatusResponse(
    val subscription: Subscription,
    val usage: Map<UsageActionType, Int>, // Current day usage
    val quotas: Map<UsageActionType, Int> // Daily quotas
)

