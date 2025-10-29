package id.nearyou.app.post

import domain.model.Location
import domain.model.Post
import domain.model.SubscriptionTier
import domain.model.UserSummary
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PGobject
import java.util.*
import kotlinx.datetime.Instant

/**
 * Custom column type for PostgreSQL GEOGRAPHY type
 */
class GeographyColumnType : ColumnType() {
    override fun sqlType(): String = "GEOGRAPHY(Point, 4326)"

    override fun valueFromDB(value: Any): Location {
        return when (value) {
            is PGobject -> {
                // Parse WKT format: POINT(longitude latitude)
                val wkt = value.value ?: error("Geography value is null")
                val coords = wkt.removePrefix("POINT(").removeSuffix(")").split(" ")
                Location(
                    latitude = coords[1].toDouble(),
                    longitude = coords[0].toDouble()
                )
            }
            else -> error("Unexpected value type for Geography: ${value::class.java}")
        }
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "geography"
        obj.value = when (value) {
            is Location -> "POINT(${value.longitude} ${value.latitude})"
            else -> value as? String
        }
        stmt[index] = obj
    }

    override fun notNullValueToDB(value: Any): Any {
        return when (value) {
            is Location -> "POINT(${value.longitude} ${value.latitude})"
            else -> value
        }
    }
}

/**
 * Posts table definition using Exposed ORM
 */
object Posts : Table("posts") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id")
    val content = text("content")
    val location = registerColumn<Location>("location", GeographyColumnType())
    val mediaUrls = array<String>("media_urls", VarCharColumnType(255)).nullable()
    val likeCount = integer("like_count").default(0)
    val commentCount = integer("comment_count").default(0)
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Repository for post-related database operations
 */
object PostRepository {

    /**
     * Find posts within a radius of a location using PostGIS ST_DWithin
     *
     * @param userLocation User's current location
     * @param radiusMeters Radius in meters (default: 1000m = 1km)
     * @param limit Maximum number of posts to return
     * @param currentUserId ID of the current user (for isLikedByCurrentUser)
     * @return List of posts within the radius, sorted by distance
     */
    fun findNearbyPosts(
        userLocation: Location,
        radiusMeters: Double = 1000.0,
        limit: Int = 50,
        currentUserId: String? = null
    ): List<Post> = transaction {
        val query = """
            SELECT 
                p.id,
                p.user_id,
                p.content,
                ST_AsText(p.location) as location_text,
                p.media_urls,
                p.like_count,
                p.comment_count,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                u.username,
                u.display_name,
                u.profile_photo_url,
                u.subscription_tier,
                ST_Distance(p.location, ST_MakePoint(?, ?)::geography) as distance_meters,
                ${if (currentUserId != null) """
                EXISTS(
                    SELECT 1 FROM likes l 
                    WHERE l.post_id = p.id AND l.user_id = ?
                ) as is_liked
                """ else "false as is_liked"}
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE ST_DWithin(
                p.location, 
                ST_MakePoint(?, ?)::geography, 
                ?
            )
            AND p.is_deleted = FALSE
            ORDER BY distance_meters ASC, p.created_at DESC
            LIMIT ?
        """.trimIndent()

        // Build the final query with parameters substituted
        val finalQuery = if (currentUserId != null) {
            """
            SELECT
                p.id,
                p.user_id,
                p.content,
                ST_AsText(p.location) as location_text,
                p.media_urls,
                p.like_count,
                p.comment_count,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                u.username,
                u.display_name,
                u.profile_photo_url,
                u.subscription_tier,
                ST_Distance(p.location, ST_MakePoint(${userLocation.longitude}, ${userLocation.latitude})::geography) as distance_meters,
                EXISTS(
                    SELECT 1 FROM likes l
                    WHERE l.post_id = p.id AND l.user_id = '${UUID.fromString(currentUserId)}'
                ) as is_liked
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE ST_DWithin(
                p.location,
                ST_MakePoint(${userLocation.longitude}, ${userLocation.latitude})::geography,
                $radiusMeters
            )
            AND p.is_deleted = FALSE
            ORDER BY distance_meters ASC, p.created_at DESC
            LIMIT $limit
            """.trimIndent()
        } else {
            """
            SELECT
                p.id,
                p.user_id,
                p.content,
                ST_AsText(p.location) as location_text,
                p.media_urls,
                p.like_count,
                p.comment_count,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                u.username,
                u.display_name,
                u.profile_photo_url,
                u.subscription_tier,
                ST_Distance(p.location, ST_MakePoint(${userLocation.longitude}, ${userLocation.latitude})::geography) as distance_meters,
                false as is_liked
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE ST_DWithin(
                p.location,
                ST_MakePoint(${userLocation.longitude}, ${userLocation.latitude})::geography,
                $radiusMeters
            )
            AND p.is_deleted = FALSE
            ORDER BY distance_meters ASC, p.created_at DESC
            LIMIT $limit
            """.trimIndent()
        }

        val posts = mutableListOf<Post>()
        exec(finalQuery) { rs ->
            while (rs.next()) {
                posts.add(resultSetToPost(rs))
            }
        }
        posts
    }

    /**
     * Create a new post
     *
     * @param userId ID of the user creating the post
     * @param content Post content
     * @param location Geographic location
     * @param mediaUrls List of media URLs (optional)
     * @return Created post or null if failed
     */
    fun createPost(
        userId: String,
        content: String,
        location: Location,
        mediaUrls: List<String> = emptyList()
    ): Post? = transaction {
        val now = kotlinx.datetime.Clock.System.now()
        val postId = UUID.randomUUID()

        // Use raw SQL for insert to handle array and geography types properly
        val mediaUrlsArray = if (mediaUrls.isNotEmpty()) {
            "ARRAY[${mediaUrls.joinToString(",") { "'$it'" }}]"
        } else {
            "NULL"
        }

        val insertQuery = """
            INSERT INTO posts (
                id, user_id, content, location, media_urls,
                like_count, comment_count, is_deleted, created_at, updated_at
            ) VALUES (
                '$postId',
                '${UUID.fromString(userId)}',
                '${content.replace("'", "''")}',
                ST_MakePoint(${location.longitude}, ${location.latitude})::geography,
                $mediaUrlsArray,
                0,
                0,
                false,
                '${now}',
                '${now}'
            )
        """.trimIndent()

        exec(insertQuery) { }

        findById(postId.toString())
    }

    /**
     * Find post by ID
     *
     * @param postId Post ID
     * @param currentUserId ID of the current user (for isLikedByCurrentUser)
     * @return Post or null if not found
     */
    fun findById(postId: String, currentUserId: String? = null): Post? = transaction {
        val query = """
            SELECT 
                p.id,
                p.user_id,
                p.content,
                ST_AsText(p.location) as location_text,
                p.media_urls,
                p.like_count,
                p.comment_count,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                u.username,
                u.display_name,
                u.profile_photo_url,
                u.subscription_tier,
                NULL as distance_meters,
                ${if (currentUserId != null) """
                EXISTS(
                    SELECT 1 FROM likes l 
                    WHERE l.post_id = p.id AND l.user_id = ?
                ) as is_liked
                """ else "false as is_liked"}
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.id = ?
            AND p.is_deleted = FALSE
        """.trimIndent()

        val finalQuery = if (currentUserId != null) {
            """
            SELECT
                p.id,
                p.user_id,
                p.content,
                ST_AsText(p.location) as location_text,
                p.media_urls,
                p.like_count,
                p.comment_count,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                u.username,
                u.display_name,
                u.profile_photo_url,
                u.subscription_tier,
                NULL as distance_meters,
                EXISTS(
                    SELECT 1 FROM likes l
                    WHERE l.post_id = p.id AND l.user_id = '${UUID.fromString(currentUserId)}'
                ) as is_liked
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.id = '${UUID.fromString(postId)}'
            AND p.is_deleted = FALSE
            """.trimIndent()
        } else {
            """
            SELECT
                p.id,
                p.user_id,
                p.content,
                ST_AsText(p.location) as location_text,
                p.media_urls,
                p.like_count,
                p.comment_count,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                u.username,
                u.display_name,
                u.profile_photo_url,
                u.subscription_tier,
                NULL as distance_meters,
                false as is_liked
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.id = '${UUID.fromString(postId)}'
            AND p.is_deleted = FALSE
            """.trimIndent()
        }

        var result: Post? = null
        exec(finalQuery) { rs ->
            if (rs.next()) {
                result = resultSetToPost(rs)
            }
        }
        result
    }

    /**
     * Update post content
     *
     * @param postId Post ID
     * @param content New content
     * @return Updated post or null if not found
     */
    fun updatePost(postId: String, content: String): Post? = transaction {
        val updated = Posts.update({ Posts.id eq UUID.fromString(postId) }) {
            it[Posts.content] = content
            it[updatedAt] = kotlinx.datetime.Clock.System.now()
        }
        if (updated > 0) findById(postId) else null
    }

    /**
     * Soft delete a post
     *
     * @param postId Post ID
     * @return True if deleted, false otherwise
     */
    fun deletePost(postId: String): Boolean = transaction {
        val updated = Posts.update({ Posts.id eq UUID.fromString(postId) }) {
            it[isDeleted] = true
            it[updatedAt] = kotlinx.datetime.Clock.System.now()
        }
        updated > 0
    }

    /**
     * Convert ResultSet to Post domain model
     */
    private fun resultSetToPost(rs: java.sql.ResultSet): Post {
        // Parse location from WKT format
        val locationText = rs.getString("location_text")
        val coords = locationText.removePrefix("POINT(").removeSuffix(")").split(" ")
        val location = Location(
            latitude = coords[1].toDouble(),
            longitude = coords[0].toDouble()
        )

        // Parse media URLs
        val mediaUrlsArray = rs.getArray("media_urls")
        val mediaUrls = if (mediaUrlsArray != null) {
            (mediaUrlsArray.array as Array<*>).filterIsInstance<String>()
        } else {
            emptyList()
        }

        // Parse distance (nullable)
        val distance = rs.getObject("distance_meters")?.toString()?.toDoubleOrNull()

        return Post(
            id = rs.getString("id"),
            userId = rs.getString("user_id"),
            user = UserSummary(
                id = rs.getString("user_id"),
                username = rs.getString("username"),
                displayName = rs.getString("display_name"),
                profilePhotoUrl = rs.getString("profile_photo_url"),
                subscriptionTier = SubscriptionTier.valueOf(rs.getString("subscription_tier").uppercase())
            ),
            content = rs.getString("content"),
            location = location,
            mediaUrls = mediaUrls,
            likeCount = rs.getInt("like_count"),
            commentCount = rs.getInt("comment_count"),
            isLikedByCurrentUser = rs.getBoolean("is_liked"),
            distance = distance,
            isDeleted = rs.getBoolean("is_deleted"),
            createdAt = Instant.parse(rs.getTimestamp("created_at").toInstant().toString()),
            updatedAt = Instant.parse(rs.getTimestamp("updated_at").toInstant().toString())
        )
    }
}

