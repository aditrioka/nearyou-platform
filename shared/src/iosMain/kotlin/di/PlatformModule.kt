package di

import data.TokenStorage
import data.TokenStorageIOS
import org.koin.dsl.module

/**
 * iOS-specific DI module
 * Provides platform-specific implementations
 */
actual val platformModule = module {
    // TokenStorage implementation for iOS
    single<TokenStorage> { TokenStorageIOS() }
}

