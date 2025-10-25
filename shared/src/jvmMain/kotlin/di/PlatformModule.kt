package di

import data.TokenStorage
import data.TokenStorageJVM
import org.koin.dsl.module

/**
 * JVM-specific DI module
 * Provides platform-specific implementations
 */
actual val platformModule = module {
    // TokenStorage implementation for JVM (in-memory for testing)
    single<TokenStorage> { TokenStorageJVM() }
}

