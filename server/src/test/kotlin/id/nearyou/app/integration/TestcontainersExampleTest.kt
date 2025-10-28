package id.nearyou.app.integration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.lettuce.core.RedisClient
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Example integration test using Testcontainers
 * Demonstrates automatic PostgreSQL and Redis container management
 * No manual Docker setup required!
 */
class TestcontainersExampleTest {
    
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
        private lateinit var dataSource: HikariDataSource
        private lateinit var redisClient: RedisClient
        
        @JvmStatic
        @BeforeAll
        fun setupContainers() {
            println("🚀 Starting Testcontainers...")
            
            // Start containers
            postgresContainer.start()
            redisContainer.start()
            
            println("✅ PostgreSQL started at: ${postgresContainer.jdbcUrl}")
            println("✅ Redis started at: ${redisContainer.host}:${redisContainer.getMappedPort(6379)}")
            
            // Setup database connection
            val config = HikariConfig().apply {
                jdbcUrl = postgresContainer.jdbcUrl
                username = postgresContainer.username
                password = postgresContainer.password
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 5
            }
            
            dataSource = HikariDataSource(config)
            database = Database.connect(dataSource)
            
            // Setup Redis connection
            val redisUri = "redis://${redisContainer.host}:${redisContainer.getMappedPort(6379)}"
            redisClient = RedisClient.create(redisUri)
            
            println("✅ Database and Redis connections established")
        }
        
        @JvmStatic
        @AfterAll
        fun teardownContainers() {
            println("🛑 Stopping Testcontainers...")
            
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
            
            println("✅ Testcontainers stopped")
        }
    }
    
    @Test
    fun `should connect to PostgreSQL container`() {
        println("🧪 Testing PostgreSQL connection...")
        
        val result = transaction(database) {
            exec("SELECT 1") { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        
        assertNotNull(result, "Should execute query successfully")
        assertTrue(result == 1, "Query should return 1")
        
        println("✅ PostgreSQL connection test passed")
    }
    
    @Test
    fun `should connect to Redis container`() {
        println("🧪 Testing Redis connection...")

        val connection = redisClient.connect()
        val redis = connection.sync()

        // Test SET and GET
        redis.set("test_key", "test_value")
        val value = redis.get("test_key")

        assertNotNull(value, "Should retrieve value from Redis")
        assertTrue(value == "test_value", "Value should match")

        // Cleanup
        redis.del("test_key")
        connection.close()

        println("✅ Redis connection test passed")
    }
    
    @Test
    fun `should verify PostGIS extension is available`() {
        println("🧪 Testing PostGIS extension...")
        
        val hasPostGIS = transaction(database) {
            exec("SELECT PostGIS_Version()") { rs ->
                rs.next()
                rs.getString(1)
            }
        }
        
        assertNotNull(hasPostGIS, "PostGIS should be available")
        println("✅ PostGIS version: $hasPostGIS")
    }
}

