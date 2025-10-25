package id.nearyou.app.repository

import domain.model.User
import domain.model.SubscriptionTier
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PGobject
import java.util.*
import kotlinx.datetime.Instant

/**
 * Custom column type for PostgreSQL ENUM
 */
class PgEnum<T : Enum<T>>(
    name: String,
    private val enumTypeName: String,
    private val enumClass: Class<T>
) : ColumnType() {
    override fun sqlType(): String = enumTypeName

    override fun valueFromDB(value: Any): T {
        return when (value) {
            is String -> java.lang.Enum.valueOf(enumClass, value)
            is PGobject -> java.lang.Enum.valueOf(enumClass, value.value!!)
            else -> error("Unexpected value type: ${value::class.java}")
        }
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = enumTypeName
        obj.value = (value as? Enum<*>)?.name ?: value as? String
        stmt[index] = obj
    }

    override fun notNullValueToDB(value: Any): Any {
        return when (value) {
            is String -> value
            is Enum<*> -> value.name
            else -> error("Unexpected value type: ${value::class.java}")
        }
    }
}

/**
 * Internal enum for database mapping (lowercase to match DB)
 */
enum class DbSubscriptionTier {
    free, premium;

    fun toSharedModel(): SubscriptionTier = when (this) {
        free -> SubscriptionTier.FREE
        premium -> SubscriptionTier.PREMIUM
    }

    companion object {
        fun fromSharedModel(tier: SubscriptionTier): DbSubscriptionTier = when (tier) {
            SubscriptionTier.FREE -> free
            SubscriptionTier.PREMIUM -> premium
        }
    }
}

/**
 * Repository for user database operations
 */
object UserRepository {

    /**
     * Users table definition
     */
    object Users : Table("users") {
        val id = uuid("id").autoGenerate()
        val username = varchar("username", 20).uniqueIndex()
        val displayName = varchar("display_name", 50)
        val email = varchar("email", 255).nullable().uniqueIndex()
        val phone = varchar("phone", 20).nullable().uniqueIndex()
        val passwordHash = varchar("password_hash", 255).nullable()
        val bio = varchar("bio", 200).nullable()
        val profilePhotoUrl = text("profile_photo_url").nullable()
        val isVerified = bool("is_verified").default(false)
        val subscriptionTier = registerColumn<DbSubscriptionTier>(
            "subscription_tier",
            PgEnum("subscription_tier", "subscription_tier", DbSubscriptionTier::class.java)
        ).default(DbSubscriptionTier.free)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

        override val primaryKey = PrimaryKey(id)
    }
    
    /**
     * Create a new user
     */
    fun createUser(
        username: String,
        displayName: String,
        email: String? = null,
        phone: String? = null,
        passwordHash: String? = null
    ): User? = transaction {
        val now = kotlinx.datetime.Clock.System.now()
        val userId = UUID.randomUUID()

        Users.insert {
            it[id] = userId
            it[Users.username] = username
            it[Users.displayName] = displayName
            it[Users.email] = email
            it[Users.phone] = phone
            it[Users.passwordHash] = passwordHash
            it[isVerified] = false
            it[subscriptionTier] = DbSubscriptionTier.free
            it[createdAt] = now
            it[updatedAt] = now
        }

        findById(userId.toString())
    }
    
    /**
     * Find user by ID
     */
    fun findById(userId: String): User? = transaction {
        Users.select { Users.id eq UUID.fromString(userId) }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    /**
     * Find user by email
     */
    fun findByEmail(email: String): User? = transaction {
        Users.select { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    /**
     * Find user by phone
     */
    fun findByPhone(phone: String): User? = transaction {
        Users.select { Users.phone eq phone }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    /**
     * Find user by username
     */
    fun findByUsername(username: String): User? = transaction {
        Users.select { Users.username eq username }
            .map { rowToUser(it) }
            .singleOrNull()
    }
    
    /**
     * Check if email exists
     */
    fun emailExists(email: String): Boolean = transaction {
        Users.select { Users.email eq email }.count() > 0
    }
    
    /**
     * Check if phone exists
     */
    fun phoneExists(phone: String): Boolean = transaction {
        Users.select { Users.phone eq phone }.count() > 0
    }
    
    /**
     * Check if username exists
     */
    fun usernameExists(username: String): Boolean = transaction {
        Users.select { Users.username eq username }.count() > 0
    }
    
    /**
     * Update user verification status
     */
    fun updateVerificationStatus(userId: String, isVerified: Boolean): Boolean = transaction {
        val updated = Users.update({ Users.id eq UUID.fromString(userId) }) {
            it[Users.isVerified] = isVerified
            it[updatedAt] = kotlinx.datetime.Clock.System.now()
        }
        updated > 0
    }
    
    /**
     * Get password hash for user
     */
    fun getPasswordHash(identifier: String): String? = transaction {
        Users.select { 
            (Users.email eq identifier) or (Users.phone eq identifier) or (Users.username eq identifier)
        }
        .map { it[Users.passwordHash] }
        .singleOrNull()
    }
    
    /**
     * Convert database row to User (shared model)
     */
    private fun rowToUser(row: ResultRow): User {
        val createdAtKotlinx = row[Users.createdAt]
        val updatedAtKotlinx = row[Users.updatedAt]

        return User(
            id = row[Users.id].toString(),
            username = row[Users.username],
            displayName = row[Users.displayName],
            email = row[Users.email],
            phone = row[Users.phone],
            bio = row[Users.bio],
            profilePhotoUrl = row[Users.profilePhotoUrl],
            isVerified = row[Users.isVerified],
            subscriptionTier = row[Users.subscriptionTier].toSharedModel(),
            createdAt = createdAtKotlinx,
            updatedAt = updatedAtKotlinx
        )
    }
}

