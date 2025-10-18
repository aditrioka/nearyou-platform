package id.nearyou.app.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Database configuration and connection management
 */
object DatabaseConfig {
    
    private var database: Database? = null
    
    /**
     * Initialize database connection
     */
    fun init() {
        database = Database.connect(
            url = EnvironmentConfig.databaseUrl,
            driver = "org.postgresql.Driver",
            user = EnvironmentConfig.databaseUser,
            password = EnvironmentConfig.databasePassword
        )
        
        // Test connection
        transaction {
            exec("SELECT 1") { rs ->
                if (rs.next()) {
                    println("✓ Database connection established")
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
}

