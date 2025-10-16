package domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Post domain model representing a user post
 *
 * @property id Unique identifier for the post
 * @property userId ID of the user who created the post
 * @property user User summary (for display purposes)
 * @property content Text content of the post (1-500 characters)
 * @property location Geographic location where the post was created
 * @property mediaUrls List of media URLs (images, premium only)
 * @property likeCount Number of likes
 * @property commentCount Number of comments
 * @property isLikedByCurrentUser Whether the current user has liked this post
 * @property distance Distance from current user's location (in meters, optional)
 * @property isDeleted Whether the post has been soft-deleted
 * @property createdAt Timestamp when the post was created
 * @property updatedAt Timestamp when the post was last updated
 */
@Serializable
data class Post(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val content: String,
    val location: Location,
    val mediaUrls: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val distance: Double? = null,
    val isDeleted: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * Check if post has media attachments
     */
    val hasMedia: Boolean
        get() = mediaUrls.isNotEmpty()

    /**
     * Get formatted distance string
     */
    val formattedDistance: String?
        get() = distance?.let { formatDistance(it) }
}

/**
 * Post creation request model
 */
@Serializable
data class CreatePostRequest(
    val content: String,
    val location: Location,
    val mediaUrls: List<String> = emptyList()
)

/**
 * Post update request model
 */
@Serializable
data class UpdatePostRequest(
    val content: String
)

/**
 * Post feed type
 */
enum class FeedType {
    NEARBY,
    FOLLOWING
}

/**
 * Post list response with pagination
 */
@Serializable
data class PostListResponse(
    val posts: List<Post>,
    val hasMore: Boolean,
    val nextCursor: String? = null
)

