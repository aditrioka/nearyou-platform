package id.nearyou.app.post

import domain.model.CreatePostRequest
import domain.model.Location
import domain.model.Post
import domain.model.UpdatePostRequest
import id.nearyou.app.exceptions.AuthorizationException
import id.nearyou.app.exceptions.NotFoundException
import id.nearyou.app.exceptions.ValidationException
import id.nearyou.app.repository.UserRepository

/**
 * Service layer for post-related business logic
 */
class PostService {

    /**
     * Get nearby posts within a specified radius
     *
     * @param userLocation User's current location
     * @param radiusMeters Radius in meters (default: 1000m = 1km)
     * @param limit Maximum number of posts to return
     * @param currentUserId ID of the current user
     * @return List of nearby posts
     */
    fun getNearbyPosts(
        userLocation: Location,
        radiusMeters: Double = 1000.0,
        limit: Int = 50,
        currentUserId: String
    ): List<Post> {
        // Validate radius (must be one of the supported distance levels)
        validateRadius(radiusMeters)

        // Validate limit
        if (limit < 1 || limit > 100) {
            throw ValidationException(
                "Limit must be between 1 and 100",
                "INVALID_LIMIT"
            )
        }

        return PostRepository.findNearbyPosts(
            userLocation = userLocation,
            radiusMeters = radiusMeters,
            limit = limit,
            currentUserId = currentUserId
        )
    }

    /**
     * Create a new post
     *
     * @param userId ID of the user creating the post
     * @param request Post creation request
     * @return Created post
     */
    fun createPost(userId: String, request: CreatePostRequest): Post {
        // Validate content
        if (request.content.isBlank()) {
            throw ValidationException(
                "Post content cannot be blank",
                "INVALID_CONTENT"
            )
        }

        if (request.content.length > 500) {
            throw ValidationException(
                "Post content cannot exceed 500 characters",
                "CONTENT_TOO_LONG"
            )
        }

        // Validate media URLs (premium only)
        if (request.mediaUrls.isNotEmpty()) {
            val user = UserRepository.findById(userId)
                ?: throw NotFoundException("User not found", "USER_NOT_FOUND")

            if (user.subscriptionTier.name != "PREMIUM") {
                throw AuthorizationException(
                    "Media upload is only available for premium users",
                    "PREMIUM_REQUIRED"
                )
            }

            if (request.mediaUrls.size > 4) {
                throw ValidationException(
                    "Maximum 4 media files allowed per post",
                    "TOO_MANY_MEDIA"
                )
            }
        }

        // TODO: Check daily post quota for free users (100 posts/day)
        // This will be implemented in T-401: Subscription Backend

        val post = PostRepository.createPost(
            userId = userId,
            content = request.content,
            location = request.location,
            mediaUrls = request.mediaUrls
        )

        return post ?: throw RuntimeException("Failed to create post")
    }

    /**
     * Get post by ID
     *
     * @param postId Post ID
     * @param currentUserId ID of the current user
     * @return Post or null if not found
     */
    fun getPostById(postId: String, currentUserId: String): Post {
        return PostRepository.findById(postId, currentUserId)
            ?: throw NotFoundException("Post not found", "POST_NOT_FOUND")
    }

    /**
     * Update post content
     *
     * @param postId Post ID
     * @param userId ID of the user requesting the update
     * @param request Update request
     * @return Updated post
     */
    fun updatePost(postId: String, userId: String, request: UpdatePostRequest): Post {
        // Validate content
        if (request.content.isBlank()) {
            throw ValidationException(
                "Post content cannot be blank",
                "INVALID_CONTENT"
            )
        }

        if (request.content.length > 500) {
            throw ValidationException(
                "Post content cannot exceed 500 characters",
                "CONTENT_TOO_LONG"
            )
        }

        // Check if post exists and user is the owner
        val existingPost = PostRepository.findById(postId)
            ?: throw NotFoundException("Post not found", "POST_NOT_FOUND")

        if (existingPost.userId != userId) {
            throw AuthorizationException(
                "You can only update your own posts",
                "NOT_POST_OWNER"
            )
        }

        val updatedPost = PostRepository.updatePost(postId, request.content)
        return updatedPost ?: throw RuntimeException("Failed to update post")
    }

    /**
     * Delete a post (soft delete)
     *
     * @param postId Post ID
     * @param userId ID of the user requesting the deletion
     */
    fun deletePost(postId: String, userId: String) {
        // Check if post exists and user is the owner
        val existingPost = PostRepository.findById(postId)
            ?: throw NotFoundException("Post not found", "POST_NOT_FOUND")

        if (existingPost.userId != userId) {
            throw AuthorizationException(
                "You can only delete your own posts",
                "NOT_POST_OWNER"
            )
        }

        val deleted = PostRepository.deletePost(postId)
        if (!deleted) {
            throw RuntimeException("Failed to delete post")
        }
    }

    /**
     * Validate radius against supported distance levels
     */
    private fun validateRadius(radiusMeters: Double) {
        val supportedRadii = listOf(
            Location.DISTANCE_LEVEL_1,  // 1 km
            Location.DISTANCE_LEVEL_2,  // 5 km
            Location.DISTANCE_LEVEL_3,  // 10 km
            Location.DISTANCE_LEVEL_4   // 20 km
        )

        if (radiusMeters !in supportedRadii) {
            throw ValidationException(
                "Radius must be one of: 1000m (1km), 5000m (5km), 10000m (10km), 20000m (20km)",
                "INVALID_RADIUS"
            )
        }
    }
}

