package id.nearyou.app.post

import domain.model.Location
import id.nearyou.app.config.DatabaseConfig
import id.nearyou.app.repository.UserRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for PostRepository
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostRepositoryTest {

    private lateinit var testUserId: String

    @BeforeAll
    fun setup() {
        // Initialize database connection
        DatabaseConfig.init()

        // Create a test user
        val user = UserRepository.createUser(
            username = "testuser_post",
            displayName = "Test User Post",
            email = "testpost@example.com"
        )
        assertNotNull(user, "Test user should be created")
        testUserId = user!!.id
    }

    @AfterAll
    fun cleanup() {
        // Clean up test data
        transaction {
            exec("DELETE FROM posts WHERE user_id = '$testUserId'")
            exec("DELETE FROM users WHERE id = '$testUserId'")
        }
    }

    @Test
    fun `createPost should create a new post with valid data`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)
        val content = "Test post content"

        val post = PostRepository.createPost(
            userId = testUserId,
            content = content,
            location = location,
            mediaUrls = emptyList()
        )

        assertNotNull(post, "Post should be created")
        assertEquals(testUserId, post!!.userId)
        assertEquals(content, post.content)
        assertEquals(location.latitude, post.location.latitude, 0.0001)
        assertEquals(location.longitude, post.location.longitude, 0.0001)
        assertEquals(0, post.likeCount)
        assertEquals(0, post.commentCount)
        assertFalse(post.isDeleted)
        assertTrue(post.mediaUrls.isEmpty())
    }

    @Test
    fun `createPost should create post with media URLs for premium users`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)
        val content = "Test post with media"
        val mediaUrls = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        )

        val post = PostRepository.createPost(
            userId = testUserId,
            content = content,
            location = location,
            mediaUrls = mediaUrls
        )

        assertNotNull(post, "Post should be created")
        assertEquals(mediaUrls.size, post!!.mediaUrls.size)
        assertEquals(mediaUrls[0], post.mediaUrls[0])
        assertEquals(mediaUrls[1], post.mediaUrls[1])
    }

    @Test
    fun `findNearbyPosts should return posts within radius`() {
        // Create test posts at different locations
        val centerLocation = Location(latitude = -6.2088, longitude = 106.8456)

        // Post 1: Very close (within 100m)
        val post1 = PostRepository.createPost(
            userId = testUserId,
            content = "Post 1 - Very close",
            location = Location(latitude = -6.2089, longitude = 106.8457)
        )

        // Post 2: Within 1km
        val post2 = PostRepository.createPost(
            userId = testUserId,
            content = "Post 2 - Within 1km",
            location = Location(latitude = -6.2100, longitude = 106.8470)
        )

        // Post 3: Beyond 1km (should not be returned)
        val post3 = PostRepository.createPost(
            userId = testUserId,
            content = "Post 3 - Beyond 1km",
            location = Location(latitude = -6.2200, longitude = 106.8600)
        )

        // Find posts within 1km
        val nearbyPosts = PostRepository.findNearbyPosts(
            userLocation = centerLocation,
            radiusMeters = 1000.0,
            limit = 50,
            currentUserId = testUserId
        )

        // Should return post1 and post2, but not post3
        assertTrue(nearbyPosts.size >= 2, "Should find at least 2 posts within 1km")

        val foundPost1 = nearbyPosts.find { it.content == "Post 1 - Very close" }
        val foundPost2 = nearbyPosts.find { it.content == "Post 2 - Within 1km" }
        val foundPost3 = nearbyPosts.find { it.content == "Post 3 - Beyond 1km" }

        assertNotNull(foundPost1, "Post 1 should be found")
        assertNotNull(foundPost2, "Post 2 should be found")
        assertNull(foundPost3, "Post 3 should not be found (beyond radius)")

        // Verify distance is calculated
        assertNotNull(foundPost1!!.distance, "Distance should be calculated")
        assertTrue(foundPost1.distance!! < 1000.0, "Post 1 distance should be less than 1km")

        // Verify posts are sorted by distance
        if (nearbyPosts.size >= 2) {
            for (i in 0 until nearbyPosts.size - 1) {
                val current = nearbyPosts[i].distance ?: 0.0
                val next = nearbyPosts[i + 1].distance ?: 0.0
                assertTrue(current <= next, "Posts should be sorted by distance")
            }
        }
    }

    @Test
    fun `findNearbyPosts should respect different radius levels`() {
        val centerLocation = Location(latitude = -6.2088, longitude = 106.8456)

        // Create posts at different distances
        PostRepository.createPost(
            userId = testUserId,
            content = "Post at 500m",
            location = Location(latitude = -6.2133, longitude = 106.8456)
        )

        PostRepository.createPost(
            userId = testUserId,
            content = "Post at 3km",
            location = Location(latitude = -6.2358, longitude = 106.8456)
        )

        // Test 1km radius
        val posts1km = PostRepository.findNearbyPosts(
            userLocation = centerLocation,
            radiusMeters = 1000.0,
            limit = 50
        )

        // Test 5km radius
        val posts5km = PostRepository.findNearbyPosts(
            userLocation = centerLocation,
            radiusMeters = 5000.0,
            limit = 50
        )

        // 5km radius should return more posts than 1km radius
        assertTrue(posts5km.size >= posts1km.size, "5km radius should return at least as many posts as 1km")
    }

    @Test
    fun `findById should return post when it exists`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)
        val content = "Test post for findById"

        val createdPost = PostRepository.createPost(
            userId = testUserId,
            content = content,
            location = location
        )

        assertNotNull(createdPost, "Post should be created")

        val foundPost = PostRepository.findById(createdPost!!.id)

        assertNotNull(foundPost, "Post should be found")
        assertEquals(createdPost.id, foundPost!!.id)
        assertEquals(content, foundPost.content)
    }

    @Test
    fun `findById should return null when post does not exist`() {
        val nonExistentId = "00000000-0000-0000-0000-000000000000"
        val post = PostRepository.findById(nonExistentId)

        assertNull(post, "Should return null for non-existent post")
    }

    @Test
    fun `updatePost should update content`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)
        val originalContent = "Original content"
        val updatedContent = "Updated content"

        val post = PostRepository.createPost(
            userId = testUserId,
            content = originalContent,
            location = location
        )

        assertNotNull(post, "Post should be created")

        val updatedPost = PostRepository.updatePost(post!!.id, updatedContent)

        assertNotNull(updatedPost, "Post should be updated")
        assertEquals(updatedContent, updatedPost!!.content)
        assertEquals(post.id, updatedPost.id)
    }

    @Test
    fun `deletePost should soft delete post`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)
        val content = "Post to be deleted"

        val post = PostRepository.createPost(
            userId = testUserId,
            content = content,
            location = location
        )

        assertNotNull(post, "Post should be created")

        val deleted = PostRepository.deletePost(post!!.id)

        assertTrue(deleted, "Post should be deleted")

        // Verify post is not returned in queries (soft deleted)
        val foundPost = PostRepository.findById(post.id)
        assertNull(foundPost, "Deleted post should not be found")
    }

    @Test
    fun `findNearbyPosts should not return deleted posts`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)

        // Create and delete a post
        val post = PostRepository.createPost(
            userId = testUserId,
            content = "Post to be deleted",
            location = location
        )

        assertNotNull(post, "Post should be created")
        PostRepository.deletePost(post!!.id)

        // Search for nearby posts
        val nearbyPosts = PostRepository.findNearbyPosts(
            userLocation = location,
            radiusMeters = 1000.0,
            limit = 50
        )

        // Deleted post should not be in results
        val foundDeletedPost = nearbyPosts.find { it.id == post.id }
        assertNull(foundDeletedPost, "Deleted post should not be in nearby results")
    }
}

