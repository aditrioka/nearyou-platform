package id.nearyou.app.integration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import id.nearyou.app.config.DatabaseConfig
import id.nearyou.app.repository.UserRepository
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base class for integration tests using Testcontainers
 * Provides PostgreSQL and Redis containers for testing
 */
abstract class BaseIntegrationTest {
    
    companion object {
        // PostgreSQL container with PostGIS extension
        private val postgresContainer = PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:15-3.4")
        ).apply {
            withDatabaseName("nearyou_test")
            withUsername("test")
            withPassword("test")
        }
        
        // Redis container
        private val redisContainer = GenericContainer(
            DockerImageName.parse("redis:7-alpine")
        ).apply {
            withExposedPorts(6379)
        }
        
        lateinit var database: Database
        lateinit var dataSource: HikariDataSource
        lateinit var redis: RedisCommands<String, String>
        private lateinit var redisClient: RedisClient
        
        @JvmStatic
        @BeforeAll
        fun setupContainers() {
            // Start containers
            postgresContainer.start()
            redisContainer.start()
            
            // Setup database
            val config = HikariConfig().apply {
                jdbcUrl = postgresContainer.jdbcUrl
                username = postgresContainer.username
                password = postgresContainer.password
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 5
            }
            
            dataSource = HikariDataSource(config)
            database = Database.connect(dataSource)
            
            // Create tables
            transaction(database) {
                SchemaUtils.create(UserRepository.Users)
                SchemaUtils.create(id.nearyou.app.auth.AuthService.Companion.OtpCodes)
                SchemaUtils.create(id.nearyou.app.auth.AuthService.Companion.RefreshTokens)
            }
            
            // Setup Redis
            val redisUri = "redis://${redisContainer.host}:${redisContainer.getMappedPort(6379)}"
            redisClient = RedisClient.create(redisUri)
            redis = redisClient.connect().sync()
        }
        
        @JvmStatic
        @AfterAll
        fun teardownContainers() {
            // Close connections
            if (::redisClient.isInitialized) {
                redisClient.shutdown()
            }
            if (::dataSource.isInitialized) {
                dataSource.close()
            }
            
            // Stop containers
            redisContainer.stop()
            postgresContainer.stop()
        }
        
        /**
         * Clear all data from database tables
         */
        fun clearDatabase() {
            transaction(database) {
                SchemaUtils.drop(UserRepository.Users)
                SchemaUtils.drop(id.nearyou.app.auth.AuthService.Companion.OtpCodes)
                SchemaUtils.drop(id.nearyou.app.auth.AuthService.Companion.RefreshTokens)

                SchemaUtils.create(UserRepository.Users)
                SchemaUtils.create(id.nearyou.app.auth.AuthService.Companion.OtpCodes)
                SchemaUtils.create(id.nearyou.app.auth.AuthService.Companion.RefreshTokens)
            }
        }
        
        /**
         * Clear all data from Redis
         */
        fun clearRedis() {
            redis.flushall()
        }
    }
}

