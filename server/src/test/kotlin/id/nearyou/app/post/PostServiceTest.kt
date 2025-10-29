package id.nearyou.app.post

import domain.model.CreatePostRequest
import domain.model.Location
import domain.model.UpdatePostRequest
import id.nearyou.app.config.DatabaseConfig
import id.nearyou.app.exceptions.AuthorizationException
import id.nearyou.app.exceptions.NotFoundException
import id.nearyou.app.exceptions.ValidationException
import id.nearyou.app.repository.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for PostService
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostServiceTest {

    private val postService = PostService()
    private lateinit var testUserId: String
    private lateinit var otherUserId: String

    @BeforeAll
    fun setup() {
        // Initialize database connection
        DatabaseConfig.init()

        // Create test users
        val user1 = UserRepository.createUser(
            username = "testuser_service",
            displayName = "Test User Service",
            email = "testservice@example.com"
        )
        assertNotNull(user1, "Test user 1 should be created")
        testUserId = user1!!.id

        val user2 = UserRepository.createUser(
            username = "otheruser_service",
            displayName = "Other User Service",
            email = "otherservice@example.com"
        )
        assertNotNull(user2, "Test user 2 should be created")
        otherUserId = user2!!.id
    }

    @AfterAll
    fun cleanup() {
        // Clean up test data
        transaction {
            exec("DELETE FROM posts WHERE user_id IN ('$testUserId', '$otherUserId')")
            exec("DELETE FROM users WHERE id IN ('$testUserId', '$otherUserId')")
        }
    }

    @Test
    fun `createPost should create post with valid data`() {
        val request = CreatePostRequest(
            content = "Test post from service",
            location = Location(latitude = -6.2088, longitude = 106.8456),
            mediaUrls = emptyList()
        )

        val post = postService.createPost(testUserId, request)

        assertNotNull(post)
        assertEquals(testUserId, post.userId)
        assertEquals(request.content, post.content)
        assertEquals(request.location.latitude, post.location.latitude, 0.0001)
        assertEquals(request.location.longitude, post.location.longitude, 0.0001)
    }

    @Test
    fun `createPost should throw ValidationException for blank content`() {
        val request = CreatePostRequest(
            content = "   ",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )

        val exception = assertThrows<ValidationException> {
            postService.createPost(testUserId, request)
        }

        assertEquals("INVALID_CONTENT", exception.errorCode)
    }

    @Test
    fun `createPost should throw ValidationException for content exceeding 500 characters`() {
        val longContent = "a".repeat(501)
        val request = CreatePostRequest(
            content = longContent,
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )

        val exception = assertThrows<ValidationException> {
            postService.createPost(testUserId, request)
        }

        assertEquals("CONTENT_TOO_LONG", exception.errorCode)
    }

    @Test
    fun `createPost should throw AuthorizationException for media upload by free user`() {
        val request = CreatePostRequest(
            content = "Post with media",
            location = Location(latitude = -6.2088, longitude = 106.8456),
            mediaUrls = listOf("https://example.com/image.jpg")
        )

        val exception = assertThrows<AuthorizationException> {
            postService.createPost(testUserId, request)
        }

        assertEquals("PREMIUM_REQUIRED", exception.errorCode)
    }

    @Test
    fun `getNearbyPosts should return posts within radius`() {
        // Create test posts
        val location1 = Location(latitude = -6.2088, longitude = 106.8456)
        val request1 = CreatePostRequest(
            content = "Nearby post 1",
            location = location1
        )
        postService.createPost(testUserId, request1)

        val location2 = Location(latitude = -6.2090, longitude = 106.8458)
        val request2 = CreatePostRequest(
            content = "Nearby post 2",
            location = location2
        )
        postService.createPost(testUserId, request2)

        // Get nearby posts
        val userLocation = Location(latitude = -6.2088, longitude = 106.8456)
        val posts = postService.getNearbyPosts(
            userLocation = userLocation,
            radiusMeters = 1000.0,
            limit = 50,
            currentUserId = testUserId
        )

        assertTrue(posts.isNotEmpty(), "Should find nearby posts")
        assertTrue(posts.any { it.content == "Nearby post 1" })
        assertTrue(posts.any { it.content == "Nearby post 2" })
    }

    @Test
    fun `getNearbyPosts should throw ValidationException for invalid radius`() {
        val userLocation = Location(latitude = -6.2088, longitude = 106.8456)

        val exception = assertThrows<ValidationException> {
            postService.getNearbyPosts(
                userLocation = userLocation,
                radiusMeters = 2000.0, // Invalid radius (not 1, 5, 10, or 20 km)
                limit = 50,
                currentUserId = testUserId
            )
        }

        assertEquals("INVALID_RADIUS", exception.errorCode)
    }

    @Test
    fun `getNearbyPosts should throw ValidationException for invalid limit`() {
        val userLocation = Location(latitude = -6.2088, longitude = 106.8456)

        val exception = assertThrows<ValidationException> {
            postService.getNearbyPosts(
                userLocation = userLocation,
                radiusMeters = 1000.0,
                limit = 101, // Exceeds maximum limit
                currentUserId = testUserId
            )
        }

        assertEquals("INVALID_LIMIT", exception.errorCode)
    }

    @Test
    fun `getPostById should return post when it exists`() {
        val request = CreatePostRequest(
            content = "Post for getById test",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )
        val createdPost = postService.createPost(testUserId, request)

        val foundPost = postService.getPostById(createdPost.id, testUserId)

        assertNotNull(foundPost)
        assertEquals(createdPost.id, foundPost.id)
        assertEquals(createdPost.content, foundPost.content)
    }

    @Test
    fun `getPostById should throw NotFoundException when post does not exist`() {
        val nonExistentId = "00000000-0000-0000-0000-000000000000"

        val exception = assertThrows<NotFoundException> {
            postService.getPostById(nonExistentId, testUserId)
        }

        assertEquals("POST_NOT_FOUND", exception.errorCode)
    }

    @Test
    fun `updatePost should update content when user is owner`() {
        val createRequest = CreatePostRequest(
            content = "Original content",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )
        val post = postService.createPost(testUserId, createRequest)

        val updateRequest = UpdatePostRequest(content = "Updated content")
        val updatedPost = postService.updatePost(post.id, testUserId, updateRequest)

        assertEquals("Updated content", updatedPost.content)
        assertEquals(post.id, updatedPost.id)
    }

    @Test
    fun `updatePost should throw AuthorizationException when user is not owner`() {
        val createRequest = CreatePostRequest(
            content = "Post by test user",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )
        val post = postService.createPost(testUserId, createRequest)

        val updateRequest = UpdatePostRequest(content = "Trying to update")

        val exception = assertThrows<AuthorizationException> {
            postService.updatePost(post.id, otherUserId, updateRequest)
        }

        assertEquals("NOT_POST_OWNER", exception.errorCode)
    }

    @Test
    fun `updatePost should throw ValidationException for blank content`() {
        val createRequest = CreatePostRequest(
            content = "Original content",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )
        val post = postService.createPost(testUserId, createRequest)

        val updateRequest = UpdatePostRequest(content = "   ")

        val exception = assertThrows<ValidationException> {
            postService.updatePost(post.id, testUserId, updateRequest)
        }

        assertEquals("INVALID_CONTENT", exception.errorCode)
    }

    @Test
    fun `deletePost should delete post when user is owner`() {
        val createRequest = CreatePostRequest(
            content = "Post to be deleted",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )
        val post = postService.createPost(testUserId, createRequest)

        assertDoesNotThrow {
            postService.deletePost(post.id, testUserId)
        }

        // Verify post is deleted
        val exception = assertThrows<NotFoundException> {
            postService.getPostById(post.id, testUserId)
        }

        assertEquals("POST_NOT_FOUND", exception.errorCode)
    }

    @Test
    fun `deletePost should throw AuthorizationException when user is not owner`() {
        val createRequest = CreatePostRequest(
            content = "Post by test user",
            location = Location(latitude = -6.2088, longitude = 106.8456)
        )
        val post = postService.createPost(testUserId, createRequest)

        val exception = assertThrows<AuthorizationException> {
            postService.deletePost(post.id, otherUserId)
        }

        assertEquals("NOT_POST_OWNER", exception.errorCode)
    }

    @Test
    fun `deletePost should throw NotFoundException when post does not exist`() {
        val nonExistentId = "00000000-0000-0000-0000-000000000000"

        val exception = assertThrows<NotFoundException> {
            postService.deletePost(nonExistentId, testUserId)
        }

        assertEquals("POST_NOT_FOUND", exception.errorCode)
    }
}

