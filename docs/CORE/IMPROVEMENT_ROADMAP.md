# NearYou ID - Improvement Roadmap

**Date:** 2025-10-22  
**Based on:** [BEST_PRACTICES_EVALUATION.md](BEST_PRACTICES_EVALUATION.md)  
**Status:** Proposed

---

## Overview

This roadmap outlines the implementation plan for addressing the findings from the comprehensive best practices evaluation. Improvements are prioritized by impact and urgency.

---

## Phase 1: Critical Fixes (Week 1-2)

### 1.1 Database Connection Pooling ⚡ **CRITICAL**

**Issue:** No connection pooling configured for PostgreSQL  
**Impact:** Production performance and scalability  
**Effort:** 2-4 hours

**Implementation:**

```kotlin
// server/build.gradle.kts
dependencies {
    implementation("com.zaxxer:HikariCP:5.1.0")
}

// server/src/main/kotlin/id/nearyou/app/config/DatabaseConfig.kt
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DatabaseConfig {
    private var database: Database? = null
    
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = EnvironmentConfig.databaseUrl
            driverClassName = "org.postgresql.Driver"
            username = EnvironmentConfig.databaseUser
            password = EnvironmentConfig.databasePassword
            
            // Connection pool settings
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 600000  // 10 minutes
            connectionTimeout = 30000  // 30 seconds
            maxLifetime = 1800000  // 30 minutes
            
            // Performance tuning
            leakDetectionThreshold = 60000  // 1 minute
        }
        
        val dataSource = HikariDataSource(config)
        database = Database.connect(dataSource)
        
        // Test connection
        transaction {
            exec("SELECT 1") { rs ->
                if (rs.next()) {
                    println("✓ Database connection pool established")
                }
            }
        }
    }
}
```

**Validation:**
```bash
./gradlew :server:build
./gradlew :server:run
# Verify connection pool in logs
```

---

### 1.2 Password Security Fix 🔒 **CRITICAL**

**Issue:** Plain passwords stored temporarily in Redis  
**Impact:** Security vulnerability  
**Effort:** 1-2 hours

**Implementation:**

```kotlin
// server/src/main/kotlin/id/nearyou/app/auth/AuthService.kt
fun registerUser(request: RegisterRequest): Result<OtpSentResponse> {
    return try {
        // ... validation code ...
        
        // Hash password BEFORE storing in Redis
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(12))
        
        // Store hashed password instead of plain text
        val registrationData = "${request.username}|${request.displayName}|${request.email}|${request.phone}|${passwordHash}"
        redis.setex("pending_registration:$identifier", 300, registrationData)
        
        // ... rest of code ...
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> {
    return try {
        // ... OTP verification ...
        
        val pendingRegistration = redis.get("pending_registration:${request.identifier}")
        if (pendingRegistration != null) {
            val parts = pendingRegistration.split("|")
            val passwordHash = parts[4]  // Already hashed
            
            // Create user with hashed password
            val user = UserRepository.create(
                username = parts[0],
                displayName = parts[1],
                email = parts[2].takeIf { it != "null" },
                phone = parts[3].takeIf { it != "null" },
                passwordHash = passwordHash  // Use hashed password directly
            )
            
            // ... rest of code ...
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Validation:**
```bash
# Run security audit
./gradlew :server:test
# Manual test: Register user and verify password is hashed in Redis
```

---

### 1.3 Backend Dependency Injection 🏗️ **HIGH PRIORITY**

**Issue:** Manual service instantiation in backend  
**Impact:** Testability and maintainability  
**Effort:** 4-6 hours

**Implementation:**

```kotlin
// server/build.gradle.kts
dependencies {
    implementation("io.insert-koin:koin-ktor:4.0.1")
    implementation("io.insert-koin:koin-logger-slf4j:4.0.1")
}

// server/src/main/kotlin/id/nearyou/app/di/ServerModule.kt
package id.nearyou.app.di

import id.nearyou.app.auth.AuthService
import id.nearyou.app.repository.UserRepository
import org.koin.dsl.module
import redis.clients.jedis.JedisPool

val serverModule = module {
    // Redis
    single { 
        JedisPool(EnvironmentConfig.redisUrl)
    }
    
    // Repositories
    single { UserRepository() }
    
    // Services
    single { AuthService(get()) }
}

// server/src/main/kotlin/id/nearyou/app/Application.kt
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.module() {
    // Install Koin
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }
    
    // Configure plugins
    configureSerialization()
    configureAuthentication()
    
    // Configure routing
    routing {
        get("/") {
            call.respondText("NearYou ID API - Running")
        }
        
        get("/health") {
            call.respond(mapOf(
                "status" to "healthy",
                "service" to "nearyou-id-api",
                "version" to "1.0.0"
            ))
        }
        
        authRoutes()  // No manual injection needed
    }
}

// server/src/main/kotlin/id/nearyou/app/auth/AuthRoutes.kt
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authService: AuthService by inject()  // Inject via Koin
    
    route("/auth") {
        post("/register") {
            // ... use authService ...
        }
    }
}
```

**Validation:**
```bash
./gradlew :server:build
./gradlew :server:test
./gradlew :server:run
```

---

## Phase 2: High Priority Improvements (Week 3-4)

### 2.1 Migrate to StateFlow in ViewModels 📊

**Issue:** Using `mutableStateOf` instead of `StateFlow`  
**Impact:** Best practice alignment, testability  
**Effort:** 3-4 hours

**Implementation:**

```kotlin
// composeApp/src/commonMain/kotlin/id/nearyou/app/ui/auth/AuthViewModel.kt
import kotlinx.coroutines.flow.*

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    fun checkAuthStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isAuth = authRepository.isAuthenticated()
            _uiState.update { it.copy(isAuthenticated = isAuth, isLoading = false) }
        }
    }
    
    suspend fun register(
        username: String,
        identifier: String,
        identifierType: String
    ): Result<Unit> {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        return try {
            val request = domain.model.auth.RegisterRequest(
                username = username,
                displayName = username,
                email = if (identifierType == "email") identifier else null,
                phone = if (identifierType == "phone") identifier else null
            )
            authRepository.register(request)
                .map { 
                    _uiState.update { it.copy(isLoading = false) }
                    Unit 
                }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
            Result.failure(e)
        }
    }
    
    // ... other methods ...
}

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

// Update UI to collect StateFlow
@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.isAuthenticated -> HomeScreen()
        else -> LoginScreen()
    }
}
```

**Validation:**
```bash
./gradlew :composeApp:build
./gradlew :composeApp:testDebugUnitTest
```

---

### 2.2 Centralized Error Handling 🚨

**Issue:** Inconsistent error handling across routes  
**Impact:** Code quality, user experience  
**Effort:** 2-3 hours

**Implementation:**

```kotlin
// server/src/main/kotlin/id/nearyou/app/plugins/ErrorHandling.kt
package id.nearyou.app.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "validation_error",
                    message = cause.message ?: "Validation failed"
                )
            )
        }
        
        exception<AuthenticationException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = "authentication_error",
                    message = cause.message ?: "Authentication failed"
                )
            )
        }
        
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "not_found",
                    message = cause.message ?: "Resource not found"
                )
            )
        }
        
        exception<Throwable> { call, cause ->
            log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "internal_error",
                    message = "An unexpected error occurred"
                )
            )
        }
    }
}

// Custom exceptions
class ValidationException(message: String) : Exception(message)
class AuthenticationException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
```

**Validation:**
```bash
./gradlew :server:build
./gradlew :server:test
```

---

## Phase 3: Testing & Quality (Week 5-6)

### 3.1 Add Repository Tests

**Effort:** 6-8 hours

See [BEST_PRACTICES_EVALUATION.md](BEST_PRACTICES_EVALUATION.md) Section 8.1 for examples.

### 3.2 Add ViewModel Tests

**Effort:** 4-6 hours

### 3.3 Add Integration Tests

**Effort:** 8-10 hours

---

## Phase 4: Optional Enhancements (Future)

- Implement proper navigation library (Voyager)
- Add use case layer for complex business logic
- Implement certificate pinning
- Add structured logging with correlation IDs
- Implement request/response logging middleware

---

## Success Metrics

- ✅ All critical security issues resolved
- ✅ Connection pooling implemented and tested
- ✅ DI framework integrated in backend
- ✅ StateFlow migration complete
- ✅ Test coverage >80%
- ✅ All builds passing
- ✅ Documentation updated

---

## References

- [BEST_PRACTICES_EVALUATION.md](BEST_PRACTICES_EVALUATION.md) - Detailed analysis
- [Ktor Documentation](https://ktor.io/docs/)
- [Koin Documentation](https://insert-koin.io/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)

