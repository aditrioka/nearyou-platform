package id.nearyou.app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseConnectionTest {

    private fun getDatabaseUrl() = System.getenv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/nearyou_db"

    private fun getDatabaseUser() = System.getenv("DATABASE_USER")
        ?: "nearyou_user"

    private fun getDatabasePassword() = System.getenv("DATABASE_PASSWORD")
        ?: "nearyou_password"

    @Test
    @Ignore("Requires PostgreSQL with PostGIS to be running. Use docker-compose up -d to start infrastructure, or use Testcontainers-based tests instead.")
    fun testDatabaseConnection() {
        // Connect to database
        Database.connect(
            url = getDatabaseUrl(),
            driver = "org.postgresql.Driver",
            user = getDatabaseUser(),
            password = getDatabasePassword()
        )

        transaction {
            // Test PostGIS version
            val result = exec("SELECT PostGIS_Version();") { rs ->
                if (rs.next()) rs.getString(1) else null
            }

            assertNotNull(result, "PostGIS should be installed")
            println("PostGIS Version: $result")
        }
    }

    @Test
    @Ignore("Requires PostgreSQL with PostGIS to be running. Use docker-compose up -d to start infrastructure, or use Testcontainers-based tests instead.")
    fun testPostGISFunctions() {
        Database.connect(
            url = getDatabaseUrl(),
            driver = "org.postgresql.Driver",
            user = getDatabaseUser(),
            password = getDatabasePassword()
        )
        
        transaction {
            // Test ST_MakePoint and ST_Distance
            val query = """
                SELECT 
                    ST_Distance(
                        ST_MakePoint(106.8456, -6.2088)::geography,
                        ST_MakePoint(106.8500, -6.2100)::geography
                    ) as distance_meters
            """.trimIndent()
            
            val distance = exec(query) { rs ->
                if (rs.next()) rs.getDouble(1) else null
            }
            
            assertNotNull(distance, "Distance calculation should work")
            assertTrue(distance > 0, "Distance should be greater than 0")
            println("Distance between points: $distance meters")
        }
    }

    @Test
    @Ignore("Table 'posts' does not exist in test database yet. Will be enabled once schema migration is implemented.")
    fun testGeoQueryWithIndex() {
        Database.connect(
            url = getDatabaseUrl(),
            driver = "org.postgresql.Driver",
            user = getDatabaseUser(),
            password = getDatabasePassword()
        )

        transaction {
            // Check if GIST index exists
            val indexQuery = """
                SELECT indexname, indexdef
                FROM pg_indexes
                WHERE tablename = 'posts' AND indexdef ILIKE '%gist%'
            """.trimIndent()

            val indexes = mutableListOf<Pair<String, String>>()
            exec(indexQuery) { rs ->
                while (rs.next()) {
                    indexes.add(rs.getString(1) to rs.getString(2))
                }
            }

            assertTrue(indexes.isNotEmpty(), "GIST index should exist on posts table")
            println("Found GIST indexes:")
            indexes.forEach { (name, def) ->
                println("  - $name: $def")
            }
        }
    }

    @Test
    @Ignore("Table 'posts' does not exist in test database yet. Will be enabled once schema migration is implemented.")
    fun testSampleGeoQuery() {
        Database.connect(
            url = getDatabaseUrl(),
            driver = "org.postgresql.Driver",
            user = getDatabaseUser(),
            password = getDatabasePassword()
        )

        transaction {
            // Test geo query (should use GIST index)
            val query = """
                EXPLAIN ANALYZE
                SELECT id, content,
                       ST_Distance(location, ST_MakePoint(106.8456, -6.2088)::geography) as distance_meters
                FROM posts
                WHERE ST_DWithin(location, ST_MakePoint(106.8456, -6.2088)::geography, 1000)
                  AND is_deleted = FALSE
                ORDER BY distance_meters
            """.trimIndent()

            println("\nQuery Plan:")
            exec(query) { rs ->
                while (rs.next()) {
                    println(rs.getString(1))
                }
            }
        }
    }
}

