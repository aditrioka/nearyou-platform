package data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.KeyStore

/**
 * Android implementation of TokenStorage using EncryptedSharedPreferences
 */
class TokenStorageAndroid(private val context: Context) : TokenStorage {

    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    /**
     * Create EncryptedSharedPreferences with error handling for corrupted keys
     */
    private fun createEncryptedSharedPreferences(): SharedPreferences {
        return try {
            createEncryptedPrefs(PREFS_FILE_NAME)
        } catch (e: Exception) {
            // Handle corrupted EncryptedSharedPreferences (common on reinstall)
            // This can happen when the app is reinstalled and the encryption keys don't match
            println("EncryptedSharedPreferences creation failed: ${e.message}")

            try {
                // Delete all related files
                deleteAllEncryptedPrefsFiles()

                // Delete the master key from Keystore
                deleteMasterKeyFromKeystore()

                // Try to recreate with fresh keys
                createEncryptedPrefs(PREFS_FILE_NAME)
            } catch (retryException: Exception) {
                // If recreation still fails, use a different file name as last resort
                println("Retry failed, using fallback file: ${retryException.message}")

                try {
                    createEncryptedPrefs(PREFS_FILE_NAME_FALLBACK)
                } catch (fallbackException: Exception) {
                    // Ultimate fallback: use regular SharedPreferences (not encrypted)
                    // This is not ideal but prevents app crash
                    println("All attempts failed, using unencrypted fallback: ${fallbackException.message}")
                    context.getSharedPreferences(PREFS_FILE_NAME_UNENCRYPTED, Context.MODE_PRIVATE)
                }
            }
        }
    }

    /**
     * Create EncryptedSharedPreferences with the given file name
     */
    private fun createEncryptedPrefs(fileName: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Delete all EncryptedSharedPreferences related files
     */
    private fun deleteAllEncryptedPrefsFiles() {
        try {
            val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
            if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                // Delete all files related to our encrypted prefs
                sharedPrefsDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith(PREFS_FILE_NAME)) {
                        val deleted = file.delete()
                        println("Deleted file: ${file.name}, success: $deleted")
                    }
                }
            }
        } catch (e: Exception) {
            println("Failed to delete SharedPreferences files: ${e.message}")
        }
    }

    /**
     * Delete the master key from Android Keystore
     */
    private fun deleteMasterKeyFromKeystore() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            println("Failed to delete master key: ${e.message}")
        }
    }

    override suspend fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    override suspend fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    override suspend fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    override suspend fun isAuthenticated(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }

    companion object {
        private const val PREFS_FILE_NAME = "nearyou_secure_prefs"
        private const val PREFS_FILE_NAME_FALLBACK = "nearyou_secure_prefs_v2"
        private const val PREFS_FILE_NAME_UNENCRYPTED = "nearyou_prefs_unencrypted"
        private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
