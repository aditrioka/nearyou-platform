package di

import data.TokenStorage
import data.TokenStorageAndroid
import org.koin.dsl.module

/**
 * Android-specific DI module
 * Provides platform-specific implementations
 */
actual val platformModule = module {
    // TokenStorage implementation for Android
    // Context is automatically provided by Koin Android
    single<TokenStorage> { TokenStorageAndroid(get()) }
}

