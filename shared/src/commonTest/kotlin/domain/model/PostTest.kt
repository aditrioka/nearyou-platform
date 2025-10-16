package domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PostTest {

    private val now = Clock.System.now()
    private val testLocation = Location(latitude = -6.2088, longitude = 106.8456)
    private val testUser = UserSummary(
        id = "user123",
        username = "testuser",
        displayName = "Test User",
        profilePhotoUrl = null,
        subscriptionTier = SubscriptionTier.FREE
    )

    @Test
    fun `post creation with valid data should succeed`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Hello, world!",
            location = testLocation,
            mediaUrls = emptyList(),
            likeCount = 0,
            commentCount = 0,
            isLikedByCurrentUser = false,
            distance = null,
            isDeleted = false,
            createdAt = now,
            updatedAt = now
        )

        assertNotNull(post)
        assertEquals("post123", post.id)
        assertEquals("user123", post.userId)
        assertEquals("Hello, world!", post.content)
        assertEquals(testLocation, post.location)
        assertEquals(0, post.likeCount)
        assertEquals(0, post.commentCount)
        assertFalse(post.isLikedByCurrentUser)
        assertFalse(post.isDeleted)
    }

    @Test
    fun `post with media URLs should have hasMedia true`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Check out this photo!",
            location = testLocation,
            mediaUrls = listOf("https://example.com/photo1.jpg", "https://example.com/photo2.jpg"),
            createdAt = now,
            updatedAt = now
        )

        assertTrue(post.hasMedia)
        assertEquals(2, post.mediaUrls.size)
    }

    @Test
    fun `post without media URLs should have hasMedia false`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Just text",
            location = testLocation,
            mediaUrls = emptyList(),
            createdAt = now,
            updatedAt = now
        )

        assertFalse(post.hasMedia)
    }

    @Test
    fun `post with distance should format distance correctly`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Nearby post",
            location = testLocation,
            distance = 1500.0,
            createdAt = now,
            updatedAt = now
        )

        assertNotNull(post.formattedDistance)
        assertEquals("1.5 km", post.formattedDistance)
    }

    @Test
    fun `post without distance should have null formattedDistance`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Post without distance",
            location = testLocation,
            distance = null,
            createdAt = now,
            updatedAt = now
        )

        assertEquals(null, post.formattedDistance)
    }

    @Test
    fun `post serialization and deserialization should work`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Test post",
            location = testLocation,
            mediaUrls = listOf("https://example.com/photo.jpg"),
            likeCount = 10,
            commentCount = 5,
            isLikedByCurrentUser = true,
            distance = 500.0,
            isDeleted = false,
            createdAt = now,
            updatedAt = now
        )

        val json = Json.encodeToString(post)
        val decoded = Json.decodeFromString<Post>(json)

        assertEquals(post, decoded)
    }

    @Test
    fun `CreatePostRequest serialization should work`() {
        val request = CreatePostRequest(
            content = "New post",
            location = testLocation,
            mediaUrls = listOf("https://example.com/photo.jpg")
        )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<CreatePostRequest>(json)

        assertEquals(request, decoded)
    }

    @Test
    fun `CreatePostRequest without media should work`() {
        val request = CreatePostRequest(
            content = "New post without media",
            location = testLocation,
            mediaUrls = emptyList()
        )

        assertNotNull(request)
        assertTrue(request.mediaUrls.isEmpty())
    }

    @Test
    fun `UpdatePostRequest serialization should work`() {
        val request = UpdatePostRequest(
            content = "Updated content"
        )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<UpdatePostRequest>(json)

        assertEquals(request, decoded)
    }

    @Test
    fun `PostListResponse serialization should work`() {
        val post = Post(
            id = "post123",
            userId = "user123",
            user = testUser,
            content = "Test post",
            location = testLocation,
            createdAt = now,
            updatedAt = now
        )

        val response = PostListResponse(
            posts = listOf(post),
            hasMore = true,
            nextCursor = "cursor123"
        )

        val json = Json.encodeToString(response)
        val decoded = Json.decodeFromString<PostListResponse>(json)

        assertEquals(response, decoded)
    }

    @Test
    fun `PostListResponse without more pages should work`() {
        val response = PostListResponse(
            posts = emptyList(),
            hasMore = false,
            nextCursor = null
        )

        assertNotNull(response)
        assertFalse(response.hasMore)
        assertEquals(null, response.nextCursor)
    }
}

