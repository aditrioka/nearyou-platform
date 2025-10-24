package id.nearyou.app.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Database configuration and connection management with HikariCP connection pooling
 */
object DatabaseConfig {

    private var database: Database? = null
    private var dataSource: HikariDataSource? = null

    /**
     * Initialize database connection with HikariCP connection pooling
     */
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = EnvironmentConfig.databaseUrl
            driverClassName = "org.postgresql.Driver"
            username = EnvironmentConfig.databaseUser
            password = EnvironmentConfig.databasePassword

            // Connection pool settings
            maximumPoolSize = 10  // Maximum number of connections in the pool
            minimumIdle = 2       // Minimum number of idle connections
            idleTimeout = 600000  // 10 minutes - max time a connection can sit idle
            connectionTimeout = 30000  // 30 seconds - max time to wait for connection
            maxLifetime = 1800000  // 30 minutes - max lifetime of a connection

            // Performance tuning
            leakDetectionThreshold = 60000  // 1 minute - detect connection leaks

            // Connection test query
            connectionTestQuery = "SELECT 1"

            // Pool name for monitoring
            poolName = "NearYouDB-Pool"
        }

        dataSource = HikariDataSource(config)
        database = Database.connect(dataSource!!)

        // Test connection
        transaction {
            exec("SELECT 1") { rs ->
                if (rs.next()) {
                    println("✓ Database connection pool established (HikariCP)")
                    println("  - Pool name: ${config.poolName}")
                    println("  - Max pool size: ${config.maximumPoolSize}")
                    println("  - Min idle: ${config.minimumIdle}")
                }
            }
        }
    }

    /**
     * Get the database instance
     */
    fun getDatabase(): Database {
        return database ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }

    /**
     * Close the connection pool (call on application shutdown)
     */
    fun close() {
        dataSource?.close()
        println("✓ Database connection pool closed")
    }
}

