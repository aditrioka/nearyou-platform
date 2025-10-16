package domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class MessageValidationTest {

    @Test
    fun `content validation accepts valid content`() {
        val result = MessageValidation.validateContent("Hello, how are you?")
        assertTrue(result.isValid)
    }

    @Test
    fun `content validation rejects empty content`() {
        val result = MessageValidation.validateContent("")
        assertFalse(result.isValid)
        assertEquals("Message cannot be empty", result.error)
    }

    @Test
    fun `content validation rejects blank content`() {
        val result = MessageValidation.validateContent("   ")
        assertFalse(result.isValid)
        assertEquals("Message cannot be empty", result.error)
    }

    @Test
    fun `content validation rejects too long content`() {
        val result = MessageValidation.validateContent("a".repeat(2001))
        assertFalse(result.isValid)
        assertEquals("Message must be at most 2000 characters", result.error)
    }

    @Test
    fun `content validation accepts max length content`() {
        val result = MessageValidation.validateContent("a".repeat(2000))
        assertTrue(result.isValid)
    }

    @Test
    fun `content validation accepts single character`() {
        val result = MessageValidation.validateContent("a")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateSendMessage accepts valid message with conversationId`() {
        val result = MessageValidation.validateSendMessage(
            conversationId = "conv123",
            recipientId = null,
            content = "Hello!"
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `validateSendMessage accepts valid message with recipientId`() {
        val result = MessageValidation.validateSendMessage(
            conversationId = null,
            recipientId = "user123",
            content = "Hello!"
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `validateSendMessage accepts valid message with both conversationId and recipientId`() {
        val result = MessageValidation.validateSendMessage(
            conversationId = "conv123",
            recipientId = "user123",
            content = "Hello!"
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `validateSendMessage rejects message without conversationId or recipientId`() {
        val result = MessageValidation.validateSendMessage(
            conversationId = null,
            recipientId = null,
            content = "Hello!"
        )
        assertFalse(result.isValid)
        assertEquals("Either conversationId or recipientId must be provided", result.error)
    }

    @Test
    fun `validateSendMessage rejects empty content`() {
        val result = MessageValidation.validateSendMessage(
            conversationId = "conv123",
            recipientId = null,
            content = ""
        )
        assertFalse(result.isValid)
        assertEquals("Message cannot be empty", result.error)
    }

    @Test
    fun `validateSendMessage rejects too long content`() {
        val result = MessageValidation.validateSendMessage(
            conversationId = "conv123",
            recipientId = null,
            content = "a".repeat(2001)
        )
        assertFalse(result.isValid)
        assertEquals("Message must be at most 2000 characters", result.error)
    }
}

