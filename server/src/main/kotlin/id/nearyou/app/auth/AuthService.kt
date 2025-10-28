package id.nearyou.app.auth

import domain.model.auth.*
import id.nearyou.app.config.EnvironmentConfig
import id.nearyou.app.exceptions.*
import id.nearyou.app.repository.UserRepository
import io.lettuce.core.api.sync.RedisCommands
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import util.AppLogger
import java.util.*
import kotlin.random.Random

/**
 * Authentication service handling user registration, login, and OTP verification
 * Dependencies are injected via constructor (Dependency Injection)
 */
class AuthService(
    private val redis: RedisCommands<String, String>
) {
    companion object {
        private const val TAG = "AuthService"
    }
    
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

            val email = request.email
            if (email != null && UserRepository.emailExists(email)) {
                return Result.failure(ConflictException("Email already registered", "EMAIL_EXISTS"))
            }
            val phone = request.phone
            if (phone != null && UserRepository.phoneExists(phone)) {
                return Result.failure(ConflictException("Phone already registered", "PHONE_EXISTS"))
            }
            if (UserRepository.usernameExists(request.username)) {
                return Result.failure(ConflictException("Username already taken", "USERNAME_EXISTS"))
            }

            // Check rate limiting
            if (!checkRateLimit(identifier)) {
                return Result.failure(RateLimitException("Too many OTP requests. Please try again later."))
            }

            // Generate and store OTP
            val otp = generateOtp()
            storeOtp(identifier, otp, type)

            // Send OTP (mock for MVP)
            sendOtp(identifier, otp, type)

            // Hash password BEFORE storing in Redis (SECURITY: Never store plain passwords)
            val passwordHash = request.password?.let { hashPassword(it) } ?: ""

            // Store pending registration in Redis with hashed password
            val registrationData = "${request.username}|${request.displayName}|${request.email}|${request.phone}|${passwordHash}"
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
     * Login existing user (sends OTP)
     */
    fun loginUser(request: LoginRequest): Result<OtpSentResponse> {
        return try {
            // Check if user exists
            val identifier = request.email ?: request.phone!!
            val type = if (request.email != null) "email" else "phone"

            // First, check if there's a pending registration in Redis
            val pendingRegistration = redis.get("pending_registration:$identifier")
            if (pendingRegistration != null) {
                // User has registered but not verified OTP yet
                return Result.failure(
                    AuthenticationException(
                        "Please verify your email/phone first. Check your inbox for the OTP code.",
                        "VERIFICATION_PENDING"
                    )
                )
            }

            // Check if user exists in database (verified users)
            val email = request.email
            if (email != null && !UserRepository.emailExists(email)) {
                return Result.failure(NotFoundException("Email not registered", "EMAIL_NOT_FOUND"))
            }
            val phone = request.phone
            if (phone != null && !UserRepository.phoneExists(phone)) {
                return Result.failure(NotFoundException("Phone not registered", "PHONE_NOT_FOUND"))
            }

            // Check rate limiting
            if (!checkRateLimit(identifier)) {
                return Result.failure(RateLimitException("Too many OTP requests. Please try again later."))
            }

            // Generate and store OTP
            val otp = generateOtp()
            storeOtp(identifier, otp, type)

            // Send OTP (mock for MVP)
            sendOtp(identifier, otp, type)

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
                return Result.failure(AuthenticationException("Invalid or expired OTP", "INVALID_OTP"))
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
                val passwordHash = parts.getOrNull(4)?.takeIf { it != "null" }

                // Password is already hashed from registerUser(), use it directly
                val createdUser = UserRepository.createUser(
                    username = username,
                    displayName = displayName,
                    email = email,
                    phone = phone,
                    passwordHash = passwordHash
                ) ?: return Result.failure(InternalServerException("Failed to create user", "USER_CREATION_FAILED"))
                
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
                } ?: return Result.failure(NotFoundException("User not found", "USER_NOT_FOUND"))
                
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
    
}

