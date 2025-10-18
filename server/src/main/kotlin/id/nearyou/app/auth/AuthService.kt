package id.nearyou.app.auth

import id.nearyou.app.auth.models.*
import id.nearyou.app.config.EnvironmentConfig
import id.nearyou.app.repository.UserRepository
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.random.Random

/**
 * Authentication service handling user registration, login, and OTP verification
 */
class AuthService {
    
    private val redisClient: RedisClient = RedisClient.create(EnvironmentConfig.redisUrl)
    private val redisConnection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val redis: RedisCommands<String, String> = redisConnection.sync()
    
    /**
     * OTP Codes table definition
     */
    object OtpCodes : Table("otp_codes") {
        val id = uuid("id").autoGenerate()
        val userIdentifier = varchar("user_identifier", 255)
        val code = varchar("code", 6)
        val type = varchar("type", 10)
        val expiresAt = timestamp("expires_at")
        val createdAt = timestamp("created_at")
        val isUsed = bool("is_used").default(false)

        override val primaryKey = PrimaryKey(id)
    }

    /**
     * Refresh Tokens table definition
     */
    object RefreshTokens : Table("refresh_tokens") {
        val id = uuid("id").autoGenerate()
        val token = varchar("token", 512).uniqueIndex()
        val userId = uuid("user_id")
        val expiresAt = timestamp("expires_at")
        val createdAt = timestamp("created_at")
        val isRevoked = bool("is_revoked").default(false)
        val revokedAt = timestamp("revoked_at").nullable()

        override val primaryKey = PrimaryKey(id)
    }
    
    /**
     * Register a new user
     */
    fun registerUser(request: RegisterRequest): Result<OtpSentResponse> {
        return try {
            // Check if user already exists
            val identifier = request.email ?: request.phone!!
            val type = if (request.email != null) "email" else "phone"
            
            if (request.email != null && UserRepository.emailExists(request.email)) {
                return Result.failure(Exception("Email already registered"))
            }
            if (request.phone != null && UserRepository.phoneExists(request.phone)) {
                return Result.failure(Exception("Phone already registered"))
            }
            if (UserRepository.usernameExists(request.username)) {
                return Result.failure(Exception("Username already taken"))
            }
            
            // Check rate limiting
            if (!checkRateLimit(identifier)) {
                return Result.failure(Exception("Too many OTP requests. Please try again later."))
            }
            
            // Generate and store OTP
            val otp = generateOtp()
            storeOtp(identifier, otp, type)
            
            // Send OTP (mock for MVP)
            sendOtp(identifier, otp, type)
            
            // Store pending registration in Redis
            val registrationData = "${request.username}|${request.displayName}|${request.email}|${request.phone}|${request.password}"
            redis.setex("pending_registration:$identifier", 300, registrationData) // 5 minutes
            
            Result.success(
                OtpSentResponse(
                    message = "OTP sent successfully",
                    identifier = identifier,
                    type = type,
                    expiresInSeconds = 300
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify OTP and complete registration or login
     */
    fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> {
        return try {
            // Verify OTP
            if (!verifyOtpCode(request.identifier, request.code, request.type)) {
                return Result.failure(Exception("Invalid or expired OTP"))
            }
            
            // Check if this is a registration or login
            val pendingRegistration = redis.get("pending_registration:${request.identifier}")
            
            val user = if (pendingRegistration != null) {
                // Complete registration
                val parts = pendingRegistration.split("|")
                val username = parts[0]
                val displayName = parts[1]
                val email = parts.getOrNull(2)?.takeIf { it != "null" }
                val phone = parts.getOrNull(3)?.takeIf { it != "null" }
                val password = parts.getOrNull(4)?.takeIf { it != "null" }
                
                val passwordHash = password?.let { hashPassword(it) }
                
                val createdUser = UserRepository.createUser(
                    username = username,
                    displayName = displayName,
                    email = email,
                    phone = phone,
                    passwordHash = passwordHash
                ) ?: return Result.failure(Exception("Failed to create user"))
                
                // Mark user as verified
                UserRepository.updateVerificationStatus(createdUser.id, true)
                
                // Clean up pending registration
                redis.del("pending_registration:${request.identifier}")
                
                createdUser
            } else {
                // Login existing user
                val existingUser = if (request.type == "email") {
                    UserRepository.findByEmail(request.identifier)
                } else {
                    UserRepository.findByPhone(request.identifier)
                } ?: return Result.failure(Exception("User not found"))
                
                // Mark user as verified if not already
                if (!existingUser.isVerified) {
                    UserRepository.updateVerificationStatus(existingUser.id, true)
                }
                
                existingUser
            }
            
            // Generate tokens
            val accessToken = JwtConfig.generateAccessToken(user.id, user.subscriptionTier)
            val refreshToken = JwtConfig.generateRefreshToken(user.id)
            
            // Store refresh token
            storeRefreshToken(refreshToken, user.id)
            
            // Mark OTP as used
            markOtpAsUsed(request.identifier, request.code)
            
            Result.success(
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = user
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    fun refreshToken(request: RefreshTokenRequest): Result<TokenResponse> {
        return try {
            // Validate refresh token
            val userId = JwtConfig.getUserIdFromToken(request.refreshToken)
                ?: return Result.failure(Exception("Invalid refresh token"))
            
            // Check if refresh token is revoked
            if (isRefreshTokenRevoked(request.refreshToken)) {
                return Result.failure(Exception("Refresh token has been revoked"))
            }
            
            // Get user
            val user = UserRepository.findById(userId)
                ?: return Result.failure(Exception("User not found"))
            
            // Generate new tokens
            val newAccessToken = JwtConfig.generateAccessToken(user.id, user.subscriptionTier)
            val newRefreshToken = JwtConfig.generateRefreshToken(user.id)
            
            // Revoke old refresh token
            revokeRefreshToken(request.refreshToken)
            
            // Store new refresh token
            storeRefreshToken(newRefreshToken, user.id)
            
            Result.success(
                TokenResponse(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate a 6-digit OTP code
     */
    private fun generateOtp(): String {
        return Random.nextInt(100000, 999999).toString()
    }
    
    /**
     * Store OTP in database
     */
    private fun storeOtp(identifier: String, code: String, type: String) {
        transaction {
            val now = kotlinx.datetime.Clock.System.now()
            val expiresAt = now.plus(kotlin.time.Duration.parse("PT5M"))

            OtpCodes.insert {
                it[userIdentifier] = identifier
                it[OtpCodes.code] = code
                it[OtpCodes.type] = type
                it[OtpCodes.expiresAt] = expiresAt
                it[createdAt] = now
            }
        }
    }
    
    /**
     * Verify OTP code
     */
    private fun verifyOtpCode(identifier: String, code: String, type: String): Boolean {
        return transaction {
            val now = kotlinx.datetime.Clock.System.now()

            OtpCodes.select {
                (OtpCodes.userIdentifier eq identifier) and
                (OtpCodes.code eq code) and
                (OtpCodes.type eq type) and
                (OtpCodes.isUsed eq false) and
                (OtpCodes.expiresAt greater now)
            }.count() > 0
        }
    }
    
    /**
     * Mark OTP as used
     */
    private fun markOtpAsUsed(identifier: String, code: String) {
        transaction {
            OtpCodes.update({
                (OtpCodes.userIdentifier eq identifier) and (OtpCodes.code eq code)
            }) {
                it[isUsed] = true
            }
        }
    }
    
    /**
     * Send OTP (mock implementation for MVP)
     */
    private fun sendOtp(identifier: String, code: String, type: String) {
        when (EnvironmentConfig.otpProvider) {
            "mock" -> {
                println("=== MOCK OTP ===")
                println("To: $identifier ($type)")
                println("Code: $code")
                println("Expires in: 5 minutes")
                println("================")
            }
            else -> {
                // TODO: Implement Twilio/SendGrid integration
                println("OTP provider ${EnvironmentConfig.otpProvider} not implemented yet")
            }
        }
    }
    
    /**
     * Check rate limiting for OTP requests
     */
    private fun checkRateLimit(identifier: String): Boolean {
        val key = "rate_limit:otp:$identifier"
        val count = redis.get(key)?.toIntOrNull() ?: 0
        
        if (count >= EnvironmentConfig.otpRateLimit) {
            return false
        }
        
        if (count == 0) {
            redis.setex(key, 3600, "1") // 1 hour TTL
        } else {
            redis.incr(key)
        }
        
        return true
    }
    
    /**
     * Hash password using BCrypt
     */
    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(EnvironmentConfig.bcryptRounds))
    }
    
    /**
     * Store refresh token in database
     */
    private fun storeRefreshToken(token: String, userId: String) {
        transaction {
            val now = kotlinx.datetime.Clock.System.now()
            val expiresAt = now.plus(kotlin.time.Duration.parse("P30D"))

            RefreshTokens.insert {
                it[RefreshTokens.token] = token
                it[RefreshTokens.userId] = UUID.fromString(userId)
                it[RefreshTokens.expiresAt] = expiresAt
                it[createdAt] = now
            }
        }
    }
    
    /**
     * Check if refresh token is revoked
     */
    private fun isRefreshTokenRevoked(token: String): Boolean {
        return transaction {
            RefreshTokens.select {
                (RefreshTokens.token eq token) and (RefreshTokens.isRevoked eq true)
            }.count() > 0
        }
    }
    
    /**
     * Revoke refresh token
     */
    private fun revokeRefreshToken(token: String) {
        transaction {
            RefreshTokens.update({ RefreshTokens.token eq token }) {
                it[isRevoked] = true
                it[revokedAt] = kotlinx.datetime.Clock.System.now()
            }
        }
    }
    
    /**
     * Close Redis connection
     */
    fun close() {
        redisConnection.close()
        redisClient.shutdown()
    }
}

