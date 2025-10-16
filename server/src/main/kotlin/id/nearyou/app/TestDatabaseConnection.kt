package id.nearyou.app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun main() {
    println("=== Testing Database Connection ===\n")

    // Connect to database
    val db = Database.connect(
        url = "jdbc:postgresql://localhost:5433/nearyou_db",
        driver = "org.postgresql.Driver",
        user = "nearyou_user",
        password = "nearyou_password"
    )

    transaction(db) {
        // Test 1: PostGIS Version
        println("1. Testing PostGIS Version:")
        exec("SELECT PostGIS_Version();") { rs ->
            if (rs.next()) {
                println("   ✓ PostGIS Version: ${rs.getString(1)}\n")
            }
        }
        
        // Test 2: Check GIST Index
        println("2. Checking GIST Index on posts table:")
        exec("""
            SELECT indexname, indexdef
            FROM pg_indexes
            WHERE tablename = 'posts' AND indexdef ILIKE '%gist%'
        """.trimIndent()) { rs ->
            var found = false
            while (rs.next()) {
                found = true
                println("   ✓ Index: ${rs.getString(1)}")
                println("   Definition: ${rs.getString(2)}\n")
            }
            if (!found) {
                println("   ✗ No GIST index found!\n")
            }
        }
        
        // Test 3: Test ST_Distance
        println("3. Testing ST_Distance function:")
        exec("""
            SELECT 
                ST_Distance(
                    ST_MakePoint(106.8456, -6.2088)::geography,
                    ST_MakePoint(106.8500, -6.2100)::geography
                ) as distance_meters
        """.trimIndent()) { rs ->
            if (rs.next()) {
                val distance = rs.getDouble(1)
                println("   ✓ Distance between two points: ${String.format("%.2f", distance)} meters\n")
            }
        }
        
        // Test 4: Count posts in database
        println("4. Counting posts in database:")
        exec("SELECT COUNT(*) FROM posts WHERE is_deleted = FALSE") { rs ->
            if (rs.next()) {
                println("   ✓ Total posts: ${rs.getInt(1)}\n")
            }
        }
        
        // Test 5: Test actual geo query
        println("5. Testing actual geo query:")
        println("   Query: Find posts within 1km of (106.8456, -6.2088)")
        exec("""
            SELECT id, content,
                   ST_Distance(location, ST_MakePoint(106.8456, -6.2088)::geography) as distance_meters
            FROM posts
            WHERE ST_DWithin(location, ST_MakePoint(106.8456, -6.2088)::geography, 1000)
              AND is_deleted = FALSE
            ORDER BY distance_meters
            LIMIT 10
        """.trimIndent()) { rs ->
            var count = 0
            while (rs.next()) {
                count++
                val id = rs.getString(1)
                val content = rs.getString(2)
                val distance = rs.getDouble(3)
                println("   - Post $id: ${content.take(50)}... (${String.format("%.2f", distance)}m away)")
            }
            if (count == 0) {
                println("   ✓ No posts found within 1km (database is empty)")
            } else {
                println("   ✓ Found $count posts within 1km")
            }
        }
    }
    
    println("\n=== All tests completed ===")
}

