package data

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy

/**
 * iOS implementation of TokenStorage using Keychain
 */
@OptIn(ExperimentalForeignApi::class)
class TokenStorageIOS : TokenStorage {
    override suspend fun saveAccessToken(token: String) {
        saveToKeychain(KEY_ACCESS_TOKEN, token)
    }

    override suspend fun getAccessToken(): String? = getFromKeychain(KEY_ACCESS_TOKEN)

    override suspend fun saveRefreshToken(token: String) {
        saveToKeychain(KEY_REFRESH_TOKEN, token)
    }

    override suspend fun getRefreshToken(): String? = getFromKeychain(KEY_REFRESH_TOKEN)

    override suspend fun clearTokens() {
        deleteFromKeychain(KEY_ACCESS_TOKEN)
        deleteFromKeychain(KEY_REFRESH_TOKEN)
    }

    override suspend fun isAuthenticated(): Boolean = getAccessToken() != null && getRefreshToken() != null

    private fun saveToKeychain(
        key: String,
        value: String,
    ) {
        // Delete existing item first
        deleteFromKeychain(key)

        // Create query dictionary
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = key
        query[kSecAttrService] = SERVICE_NAME
        query[kSecValueData] = value.encodeToByteArray().toNSData()
        query[kSecAttrAccessible] = kSecAttrAccessibleAfterFirstUnlock

        // Add to keychain
        val status = SecItemAdd(query as CFDictionaryRef, null)
        if (status != errSecSuccess) {
            println("Failed to save to keychain: $status")
        }
    }

    private fun getFromKeychain(key: String): String? {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = key
        query[kSecAttrService] = SERVICE_NAME
        query[kSecReturnData] = kCFBooleanTrue
        query[kSecMatchLimit] = kSecMatchLimitOne

        val result =
            memScoped {
                val resultRef = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query as CFDictionaryRef, resultRef.ptr)

                if (status == errSecSuccess) {
                    val data = resultRef.value as? NSData
                    data?.toByteArray()?.decodeToString()
                } else {
                    null
                }
            }

        return result
    }

    private fun deleteFromKeychain(key: String) {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = key
        query[kSecAttrService] = SERVICE_NAME

        SecItemDelete(query as CFDictionaryRef)
    }

    companion object {
        private const val SERVICE_NAME = "id.nearyou.app"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}

/**
 * Extension to convert NSData to ByteArray
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(this.length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }

/**
 * Extension to convert ByteArray to NSData
 */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    this.usePinned {
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
    }
