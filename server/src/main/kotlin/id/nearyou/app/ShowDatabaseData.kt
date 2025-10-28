package id.nearyou.app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Utility to show database data
 * Run with: ./gradlew :server:showData
 */
fun main() {
    println("=== NearYou Database Data Viewer ===\n")

    // Connect to database
    val db = Database.connect(
        url = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5433/nearyou_db",
        driver = "org.postgresql.Driver",
        user = System.getenv("DATABASE_USER") ?: "nearyou_user",
        password = System.getenv("DATABASE_PASSWORD") ?: "nearyou_password"
    )

    transaction(db) {
        // Show all tables
        println("📋 Available Tables:")
        println("=" .repeat(80))
        exec("""
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            ORDER BY table_name
        """.trimIndent()) { rs ->
            while (rs.next()) {
                println("  - ${rs.getString(1)}")
            }
        }
        println()

        // Show users count
        println("👥 Users:")
        println("=" .repeat(80))
        exec("SELECT COUNT(*) FROM users") { rs ->
            if (rs.next()) {
                println("Total users: ${rs.getInt(1)}")
            }
        }
        
        // Show recent users
        exec("""
            SELECT id, username, email, display_name, is_verified, created_at 
            FROM users 
            ORDER BY created_at DESC 
            LIMIT 10
        """.trimIndent()) { rs ->
            if (!rs.isBeforeFirst) {
                println("No users found.\n")
            } else {
                println("\nRecent users:")
                println("-".repeat(80))
                var count = 0
                while (rs.next()) {
                    count++
                    println("$count. ${rs.getString("username")} (${rs.getString("email")})")
                    println("   Display Name: ${rs.getString("display_name")}")
                    println("   Verified: ${rs.getBoolean("is_verified")}")
                    println("   Created: ${rs.getTimestamp("created_at")}")
                    println()
                }
            }
        }

        // Show posts count
        println("📝 Posts:")
        println("=" .repeat(80))
        exec("SELECT COUNT(*) FROM posts") { rs ->
            if (rs.next()) {
                println("Total posts: ${rs.getInt(1)}")
            }
        }
        
        // Show recent posts
        exec("""
            SELECT p.id, p.content, u.username, p.created_at,
                   ST_AsText(p.location) as location_text
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.is_deleted = FALSE
            ORDER BY p.created_at DESC 
            LIMIT 10
        """.trimIndent()) { rs ->
            if (!rs.isBeforeFirst) {
                println("No posts found.\n")
            } else {
                println("\nRecent posts:")
                println("-".repeat(80))
                var count = 0
                while (rs.next()) {
                    count++
                    println("$count. @${rs.getString("username")}")
                    println("   Content: ${rs.getString("content")}")
                    println("   Location: ${rs.getString("location_text")}")
                    println("   Created: ${rs.getTimestamp("created_at")}")
                    println()
                }
            }
        }

        // Show comments count
        println("💬 Comments:")
        println("=" .repeat(80))
        exec("SELECT COUNT(*) FROM comments WHERE is_deleted = FALSE") { rs ->
            if (rs.next()) {
                println("Total comments: ${rs.getInt(1)}\n")
            }
        }

        // Show likes count
        println("❤️ Likes:")
        println("=" .repeat(80))
        exec("SELECT COUNT(*) FROM likes") { rs ->
            if (rs.next()) {
                println("Total likes: ${rs.getInt(1)}\n")
            }
        }

        // Show follows count
        println("👤 Follows:")
        println("=" .repeat(80))
        exec("SELECT COUNT(*) FROM follows") { rs ->
            if (rs.next()) {
                println("Total follows: ${rs.getInt(1)}\n")
            }
        }

        // Show OTP codes (active)
        println("🔐 Active OTP Codes:")
        println("=" .repeat(80))
        exec("""
            SELECT user_identifier, code, type, expires_at
            FROM otp_codes
            WHERE expires_at > NOW() AND is_used = FALSE
            ORDER BY created_at DESC
            LIMIT 5
        """.trimIndent()) { rs ->
            if (!rs.isBeforeFirst) {
                println("No active OTP codes.\n")
            } else {
                var count = 0
                while (rs.next()) {
                    count++
                    println("$count. ${rs.getString("user_identifier")} (${rs.getString("type")})")
                    println("   Code: ${rs.getString("code")}")
                    println("   Expires: ${rs.getTimestamp("expires_at")}")
                    println()
                }
            }
        }

        // Show database stats
        println("📊 Database Statistics:")
        println("=" .repeat(80))
        exec("""
            SELECT 
                schemaname,
                tablename,
                pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
            FROM pg_tables
            WHERE schemaname = 'public'
            ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
        """.trimIndent()) { rs ->
            println("Table sizes:")
            while (rs.next()) {
                println("  ${rs.getString("tablename")}: ${rs.getString("size")}")
            }
        }
        println()

        println("=" .repeat(80))
        println("✅ Data viewer completed successfully!")
    }
}

