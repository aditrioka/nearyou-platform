package id.nearyou.app.di

import id.nearyou.app.auth.AuthService
import id.nearyou.app.user.UserService
import id.nearyou.app.post.PostService
import id.nearyou.app.storage.LocalStorageService
import id.nearyou.app.storage.StorageService
import id.nearyou.app.config.EnvironmentConfig
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.koin.dsl.module

/**
 * Koin dependency injection module for server-side components
 */
val serverModule = module {

    // Redis Client (Singleton)
    single<RedisClient> {
        RedisClient.create(EnvironmentConfig.redisUrl)
    }

    // Redis Connection (Singleton)
    single<StatefulRedisConnection<String, String>> {
        get<RedisClient>().connect()
    }

    // Redis Commands (Singleton)
    single<RedisCommands<String, String>> {
        get<StatefulRedisConnection<String, String>>().sync()
    }

    // Storage Service
    single<StorageService> {
        LocalStorageService(
            uploadDir = EnvironmentConfig.uploadDir,
            baseUrl = EnvironmentConfig.baseUrl
        )
    }

    // Services
    single { AuthService(get()) }
    single { UserService() }
    single { PostService() }
}

