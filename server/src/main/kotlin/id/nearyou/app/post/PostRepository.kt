package id.nearyou.app.post

import domain.model.Location
import domain.model.Post
import domain.model.SubscriptionTier
import domain.model.UserSummary
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PGobject
import java.sql.ResultSet
import java.util.*

/**
 * Custom column type for PostgreSQL GEOGRAPHY type
 */
class GeographyColumnType : ColumnType() {
    override fun sqlType(): String = "GEOGRAPHY(Point, 4326)"

    override fun valueFromDB(value: Any): Location =
        when (value) {
            is PGobject -> {
                // Parse WKT format: POINT(longitude latitude)
                val wkt = value.value ?: error("Geography value is null")
                val coords = wkt.removePrefix("POINT(").removeSuffix(")").split(" ")
                Location(
                    latitude = coords[1].toDouble(),
                    longitude = coords[0].toDouble(),
                )
            }
            else -> error("Unexpected value type for Geography: ${value::class.java}")
        }

    override fun setParameter(
        stmt: PreparedStatementApi,
        index: Int,
        value: Any?,
    ) {
        val obj = PGobject()
        obj.type = "geography"
        obj.value =
            when (value) {
                is Location -> "POINT(${value.longitude} ${value.latitude})"
                else -> value as? String
            }
        stmt[index] = obj
    }

    override fun notNullValueToDB(value: Any): Any =
        when (value) {
            is Location -> "POINT(${value.longitude} ${value.latitude})"
            else -> value
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
     * Uses parameterized queries to prevent SQL injection.
     */
    fun findNearbyPosts(
        userLocation: Location,
        radiusMeters: Double = 1000.0,
        limit: Int = 50,
        currentUserId: String? = null,
    ): List<Post> =
        transaction {
            val hasCurrentUser = currentUserId != null

            val query =
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
                    ST_Distance(p.location, ST_MakePoint(?, ?)::geography) as distance_meters,
                    ${if (hasCurrentUser) {
                    """
                EXISTS(
                    SELECT 1 FROM likes l
                    WHERE l.post_id = p.id AND l.user_id = ?::uuid
                ) as is_liked
                """
                } else {
                    "false as is_liked"
                }}
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

            val posts = mutableListOf<Post>()
            val conn = this.connection.connection as java.sql.Connection
            conn.prepareStatement(query).use { stmt ->
                var idx = 1
                // ST_Distance params
                stmt.setDouble(idx++, userLocation.longitude)
                stmt.setDouble(idx++, userLocation.latitude)
                // currentUserId for likes subquery
                if (hasCurrentUser) {
                    stmt.setString(idx++, currentUserId)
                }
                // ST_DWithin params
                stmt.setDouble(idx++, userLocation.longitude)
                stmt.setDouble(idx++, userLocation.latitude)
                stmt.setDouble(idx++, radiusMeters)
                // LIMIT
                stmt.setInt(idx++, limit)

                val rs = stmt.executeQuery()
                while (rs.next()) {
                    posts.add(resultSetToPost(rs))
                }
            }
            posts
        }

    /**
     * Create a new post using parameterized queries.
     */
    fun createPost(
        userId: String,
        content: String,
        location: Location,
        mediaUrls: List<String> = emptyList(),
    ): Post? =
        transaction {
            val now =
                kotlinx.datetime.Clock.System
                    .now()
            val postId = UUID.randomUUID()

            val insertQuery =
                """
                INSERT INTO posts (
                    id, user_id, content, location, media_urls,
                    like_count, comment_count, is_deleted, created_at, updated_at
                ) VALUES (
                    ?::uuid,
                    ?::uuid,
                    ?,
                    ST_MakePoint(?, ?)::geography,
                    ?,
                    0,
                    0,
                    false,
                    ?::timestamp,
                    ?::timestamp
                )
                """.trimIndent()

            val conn = this.connection.connection as java.sql.Connection
            conn.prepareStatement(insertQuery).use { stmt ->
                stmt.setString(1, postId.toString())
                stmt.setString(2, userId)
                stmt.setString(3, content)
                stmt.setDouble(4, location.longitude)
                stmt.setDouble(5, location.latitude)
                if (mediaUrls.isNotEmpty()) {
                    val sqlArray = conn.createArrayOf("varchar", mediaUrls.toTypedArray())
                    stmt.setArray(6, sqlArray)
                } else {
                    stmt.setNull(6, java.sql.Types.ARRAY)
                }
                stmt.setString(7, now.toString())
                stmt.setString(8, now.toString())
                stmt.executeUpdate()
            }

            findById(postId.toString())
        }

    /**
     * Find post by ID using parameterized queries.
     */
    fun findById(
        postId: String,
        currentUserId: String? = null,
    ): Post? =
        transaction {
            val hasCurrentUser = currentUserId != null

            val query =
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
                    ${if (hasCurrentUser) {
                    """
                EXISTS(
                    SELECT 1 FROM likes l
                    WHERE l.post_id = p.id AND l.user_id = ?::uuid
                ) as is_liked
                """
                } else {
                    "false as is_liked"
                }}
                FROM posts p
                JOIN users u ON p.user_id = u.id
                WHERE p.id = ?::uuid
                AND p.is_deleted = FALSE
                """.trimIndent()

            var result: Post? = null
            val conn = this.connection.connection as java.sql.Connection
            conn.prepareStatement(query).use { stmt ->
                var idx = 1
                if (hasCurrentUser) {
                    stmt.setString(idx++, currentUserId)
                }
                stmt.setString(idx++, postId)

                val rs = stmt.executeQuery()
                if (rs.next()) {
                    result = resultSetToPost(rs)
                }
            }
            result
        }

    /**
     * Update post content
     */
    fun updatePost(
        postId: String,
        content: String,
    ): Post? =
        transaction {
            val updated =
                Posts.update({ Posts.id eq UUID.fromString(postId) }) {
                    it[Posts.content] = content
                    it[updatedAt] =
                        kotlinx.datetime.Clock.System
                            .now()
                }
            if (updated > 0) findById(postId) else null
        }

    /**
     * Soft delete a post
     */
    fun deletePost(postId: String): Boolean =
        transaction {
            val updated =
                Posts.update({ Posts.id eq UUID.fromString(postId) }) {
                    it[isDeleted] = true
                    it[updatedAt] =
                        kotlinx.datetime.Clock.System
                            .now()
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
        val location =
            Location(
                latitude = coords[1].toDouble(),
                longitude = coords[0].toDouble(),
            )

        // Parse media URLs
        val mediaUrlsArray = rs.getArray("media_urls")
        val mediaUrls =
            if (mediaUrlsArray != null) {
                (mediaUrlsArray.array as Array<*>).filterIsInstance<String>()
            } else {
                emptyList()
            }

        // Parse distance (nullable)
        val distance = rs.getObject("distance_meters")?.toString()?.toDoubleOrNull()

        return Post(
            id = rs.getString("id"),
            userId = rs.getString("user_id"),
            user =
                UserSummary(
                    id = rs.getString("user_id"),
                    username = rs.getString("username"),
                    displayName = rs.getString("display_name"),
                    profilePhotoUrl = rs.getString("profile_photo_url"),
                    subscriptionTier = SubscriptionTier.valueOf(rs.getString("subscription_tier").uppercase()),
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
            updatedAt = Instant.parse(rs.getTimestamp("updated_at").toInstant().toString()),
        )
    }
}
