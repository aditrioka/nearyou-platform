# Logging Guide

**Production-ready logging implementation for Near You ID**

---

## Overview

The project uses a **multiplatform logging system** that works across Android, iOS, and JVM (server):

- **Android**: Uses Android's `Log` API
- **iOS**: Uses `NSLog`
- **JVM/Server**: Uses **SLF4J + Logback**
- **Shared Code**: Uses custom `AppLogger` wrapper

---

## Quick Start

### 1. Initialize Logging (App Startup)

```kotlin
// In your app's main entry point
import util.AppConfig

fun main() {
    // Development mode
    AppConfig.initialize(isDevelopment = true)
    
    // Production mode
    // AppConfig.initialize(isDevelopment = false)
}
```

### 2. Use Logger in Your Code

```kotlin
import util.AppLogger

class MyRepository {
    companion object {
        private const val TAG = "MyRepository"
    }
    
    fun doSomething() {
        // Regular logging
        AppLogger.debug(TAG, "Starting operation")
        AppLogger.info(TAG, "Operation completed successfully")
        AppLogger.warn(TAG, "This might be a problem")
        AppLogger.error(TAG, "Something went wrong", exception)
        
        // Sensitive data (only logged in development)
        AppLogger.debugSensitive(TAG, "Access Token: $token")
    }
}
```

---

## Log Levels

### Development Mode
```kotlin
AppConfig.initialize(isDevelopment = true)
```

- **Min Level**: `DEBUG` (shows everything)
- **Sensitive Data**: `ENABLED` (tokens, passwords visible)
- **Ktor HTTP Logging**: `BODY` (full request/response)

**Output Example:**
```
DEBUG [AuthRepository] Registering user: testuser
INFO [AuthRepository] Registration successful, OTP sent to: test@example.com
[SENSITIVE] Access Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
[SENSITIVE] Refresh Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Production Mode
```kotlin
AppConfig.initialize(isDevelopment = false)
```

- **Min Level**: `INFO` (hides debug logs)
- **Sensitive Data**: `DISABLED` (tokens, passwords hidden)
- **Ktor HTTP Logging**: `INFO` (basic request info only)

**Output Example:**
```
INFO [AuthRepository] Registration successful, OTP sent to: test@example.com
DEBUG [AuthRepository] [SENSITIVE DATA REDACTED]
```

---

## Configuration

### Environment-Based Configuration

**Server (JVM):**

Set log level via environment variable:
```bash
# Development
export LOG_LEVEL=DEBUG
./gradlew :server:run

# Production
export LOG_LEVEL=INFO
./gradlew :server:run
```

**Android:**

In your `Application` class:
```kotlin
class NearYouApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Check if debug build
        val isDev = BuildConfig.DEBUG
        AppConfig.initialize(isDevelopment = isDev)
    }
}
```

**iOS:**

In your app delegate:
```swift
@main
struct NearYouApp: App {
    init() {
        #if DEBUG
        AppConfigKt.initialize(isDevelopment: true)
        #else
        AppConfigKt.initialize(isDevelopment: false)
        #endif
    }
}
```

---

## Logback Configuration (Server)

**File:** `server/src/main/resources/logback.xml`

### Console Logging (Default)
```xml
<root level="${LOG_LEVEL:-INFO}">
    <appender-ref ref="STDOUT"/>
</root>
```

### File Logging (Production)

Uncomment in `logback.xml`:
```xml
<root level="${LOG_LEVEL:-INFO}">
    <appender-ref ref="STDOUT"/>
    <appender-ref ref="FILE"/>  <!-- Enable this -->
</root>
```

Logs will be saved to:
- `logs/nearyou-app.log` (current)
- `logs/nearyou-app.2025-10-28.log` (daily rotation)
- Keeps last 30 days

---

## Best Practices

### ✅ DO

1. **Use appropriate log levels:**
   ```kotlin
   AppLogger.debug(TAG, "Detailed debugging info")
   AppLogger.info(TAG, "Important business events")
   AppLogger.warn(TAG, "Potential issues")
   AppLogger.error(TAG, "Errors that need attention", exception)
   ```

2. **Use `debugSensitive()` for sensitive data:**
   ```kotlin
   // ✅ Good - automatically redacted in production
   AppLogger.debugSensitive(TAG, "Token: $accessToken")
   
   // ❌ Bad - always visible
   AppLogger.debug(TAG, "Token: $accessToken")
   ```

3. **Include context in messages:**
   ```kotlin
   // ✅ Good
   AppLogger.info(TAG, "User profile updated: ${user.username}")
   
   // ❌ Bad
   AppLogger.info(TAG, "Profile updated")
   ```

4. **Log exceptions with context:**
   ```kotlin
   try {
       // ...
   } catch (e: Exception) {
       AppLogger.error(TAG, "Failed to update profile for user: $userId", e)
   }
   ```

### ❌ DON'T

1. **Don't use `println()` or `print()`:**
   ```kotlin
   // ❌ Bad - not production-ready
   println("User logged in: $username")
   
   // ✅ Good
   AppLogger.info(TAG, "User logged in: $username")
   ```

2. **Don't log sensitive data without `debugSensitive()`:**
   ```kotlin
   // ❌ Bad - exposes tokens in production
   AppLogger.info(TAG, "Token: $token")
   
   // ✅ Good
   AppLogger.debugSensitive(TAG, "Token: $token")
   ```

3. **Don't log in tight loops:**
   ```kotlin
   // ❌ Bad - performance impact
   list.forEach { item ->
       AppLogger.debug(TAG, "Processing: $item")
   }
   
   // ✅ Good
   AppLogger.debug(TAG, "Processing ${list.size} items")
   ```

---

## Viewing Logs

### Android
```bash
# View all logs
adb logcat

# Filter by tag
adb logcat -s AuthRepository

# Filter by level
adb logcat *:E  # Errors only
```

### iOS
- Open **Xcode Console** while running the app
- Or use `Console.app` on macOS

### Server (JVM)
```bash
# Console output
./gradlew :server:run

# Tail log file (if file logging enabled)
tail -f logs/nearyou-app.log

# Search logs
grep "ERROR" logs/nearyou-app.log
```

---

## Testing Logging

### Unit Tests

Logging is automatically disabled in tests (no output noise).

### Integration Tests

To see logs during integration tests:
```bash
./gradlew :server:test --info
```

---

## Production Checklist

Before deploying to production:

- [ ] Set `AppConfig.initialize(isDevelopment = false)`
- [ ] Set `LOG_LEVEL=INFO` or `LOG_LEVEL=WARN` environment variable
- [ ] Enable file logging in `logback.xml` (optional)
- [ ] Verify no `println()` statements in code
- [ ] Verify sensitive data is logged with `debugSensitive()`
- [ ] Set up log rotation (already configured for 30 days)
- [ ] Consider log aggregation (e.g., ELK, Datadog, CloudWatch)

---

## Troubleshooting

### Logs not showing up

**Android:**
- Check Logcat filter settings
- Verify `AppConfig.initialize()` is called

**iOS:**
- Check Xcode console is open
- Verify app is running in debug mode

**Server:**
- Check `LOG_LEVEL` environment variable
- Verify `logback.xml` configuration
- Check console output or log file

### Too much noise in logs

**Reduce third-party library logs:**

Edit `server/src/main/resources/logback.xml`:
```xml
<logger name="io.ktor" level="WARN"/>
<logger name="org.postgresql" level="ERROR"/>
```

### Sensitive data showing in production

**Verify configuration:**
```kotlin
// Should be false in production
println("Sensitive logging: ${LoggerConfig.logSensitiveData}")
```

---

## Related Documents

- **[TESTING_USER_PROFILE.md](./TESTING_USER_PROFILE.md)** → Testing guide
- **[PRE_PUSH_CHECKLIST.md](./PRE_PUSH_CHECKLIST.md)** → Pre-push verification
- **[API_DOCUMENTATION.md](../CORE/API_DOCUMENTATION.md)** → API reference

