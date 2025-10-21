package di

import data.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-specific module - implemented in each platform
 */
expect val platformModule: Module

/**
 * Common DI module for shared dependencies
 */
val sharedModule = module {
    // AuthRepository depends on TokenStorage which will be provided by platform-specific modules
    single { AuthRepository(get()) }
}

