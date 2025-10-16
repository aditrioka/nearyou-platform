package domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class PostValidationTest {

    @Test
    fun `content validation accepts valid content`() {
        val result = PostValidation.validateContent("Hello, world!")
        assertTrue(result.isValid)
    }

    @Test
    fun `content validation rejects empty content`() {
        val result = PostValidation.validateContent("")
        assertFalse(result.isValid)
        assertEquals("Post content cannot be empty", result.error)
    }

    @Test
    fun `content validation rejects too long content`() {
        val result = PostValidation.validateContent("a".repeat(501))
        assertFalse(result.isValid)
        assertEquals("Post content must be at most 500 characters", result.error)
    }

    @Test
    fun `content validation accepts max length content`() {
        val result = PostValidation.validateContent("a".repeat(500))
        assertTrue(result.isValid)
    }

    @Test
    fun `media validation rejects media for free users`() {
        val result = PostValidation.validateMediaUrls(
            mediaUrls = listOf("https://example.com/image.jpg"),
            isPremium = false
        )
        assertFalse(result.isValid)
        assertEquals("Media uploads are only available for premium users", result.error)
    }

    @Test
    fun `media validation accepts media for premium users`() {
        val result = PostValidation.validateMediaUrls(
            mediaUrls = listOf("https://example.com/image.jpg"),
            isPremium = true
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `media validation rejects too many images`() {
        val result = PostValidation.validateMediaUrls(
            mediaUrls = List(5) { "https://example.com/image$it.jpg" },
            isPremium = true
        )
        assertFalse(result.isValid)
        assertEquals("Maximum 4 images allowed per post", result.error)
    }

    @Test
    fun `create post validation accepts valid post for premium user`() {
        val result = PostValidation.validateCreatePost(
            content = "Hello, world!",
            mediaUrls = listOf("https://example.com/image.jpg"),
            isPremium = true
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `create post validation accepts valid post for free user without media`() {
        val result = PostValidation.validateCreatePost(
            content = "Hello, world!",
            mediaUrls = emptyList(),
            isPremium = false
        )
        assertTrue(result.isValid)
    }
}

class CommentValidationTest {

    @Test
    fun `comment validation accepts valid comment`() {
        val result = CommentValidation.validateContent("Great post!")
        assertTrue(result.isValid)
    }

    @Test
    fun `comment validation rejects empty comment`() {
        val result = CommentValidation.validateContent("")
        assertFalse(result.isValid)
        assertEquals("Comment cannot be empty", result.error)
    }

    @Test
    fun `comment validation rejects too long comment`() {
        val result = CommentValidation.validateContent("a".repeat(501))
        assertFalse(result.isValid)
        assertEquals("Comment must be at most 500 characters", result.error)
    }
}

