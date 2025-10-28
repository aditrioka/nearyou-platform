# Testing Logging Implementation

Quick guide to test the production-ready logging system.

## 🚀 Quick Test (Server)

### 1. Start the Server in Development Mode

```bash
# Start dependencies
docker-compose up -d postgres redis

# Start server (development mode by default)
./gradlew :server:run
```

### 2. Trigger Logging with API Calls

**Register a user:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "displayName": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected logs:**
```
INFO [AuthRepository] Registering user: testuser
INFO [AuthRepository] Registration successful, OTP sent to: test@example.com
```

**Check server console for OTP code**, then verify:
```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "code": "YOUR_OTP_CODE",
    "type": "email"
  }'
```

**Expected logs (Development Mode):**
```
INFO [AuthRepository] OTP verification successful for user: testuser
DEBUG [AuthRepository] [SENSITIVE] Access Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
DEBUG [AuthRepository] [SENSITIVE] Refresh Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
DEBUG [AuthRepository] Tokens saved to storage
```

### 3. Test Production Mode

Stop the server and restart with production environment:

```bash
# Set production environment
export ENVIRONMENT=production

# Restart server
./gradlew :server:run
```

Repeat the same API calls. **Expected logs (Production Mode):**
```
INFO [AuthRepository] Registration successful, OTP sent to: test@example.com
INFO [AuthRepository] OTP verification successful for user: testuser
DEBUG [AuthRepository] [SENSITIVE DATA REDACTED]
```

✅ **Tokens are now hidden!**

---

## 📱 Test on Android

### 1. Initialize in Application Class

Create or update `androidApp/src/main/kotlin/id/nearyou/android/NearYouApp.kt`:

```kotlin
package id.nearyou.android

import android.app.Application
import util.AppConfig

class NearYouApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        AppConfig.initialize(isDevelopment = BuildConfig.DEBUG)
    }
}
```

### 2. Register in AndroidManifest.xml

```xml
<application
    android:name=".NearYouApp"
    ...>
```

### 3. Run the App and Check Logcat

```bash
# Filter for NearYou logs
adb logcat | grep -E "AuthRepository|UserRepository"
```

**Expected output:**
```
D/AuthRepository: Registering user: testuser
I/AuthRepository: Registration successful, OTP sent to: test@example.com
I/AuthRepository: OTP verification successful for user: testuser
D/AuthRepository: [SENSITIVE] Access Token: eyJhbGci...
```

---

## 🍎 Test on iOS

### 1. Initialize in App

Update `iosApp/iosApp/iOSApp.swift`:

```swift
import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        // Initialize logging
        #if DEBUG
        AppConfigKt.initialize(isDevelopment: true)
        #else
        AppConfigKt.initialize(isDevelopment: false)
        #endif
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 2. Run the App and Check Console

In Xcode, open the console (⌘+Shift+Y) and look for logs:

```
INFO [AuthRepository] Registering user: testuser
INFO [AuthRepository] Registration successful, OTP sent to: test@example.com
DEBUG [AuthRepository] [SENSITIVE] Access Token: eyJhbGci...
```

---

## 🔍 What to Look For

### ✅ Development Mode Checklist

- [ ] INFO logs appear for important operations
- [ ] DEBUG logs show detailed information
- [ ] Sensitive data (tokens) is visible with `[SENSITIVE]` prefix
- [ ] HTTP request/response bodies are logged (Ktor)
- [ ] Error logs include stack traces

### ✅ Production Mode Checklist

- [ ] INFO logs still appear for important operations
- [ ] DEBUG logs may be filtered (depending on log level)
- [ ] Sensitive data shows `[SENSITIVE DATA REDACTED]`
- [ ] HTTP logging is minimal (INFO level only)
- [ ] No passwords or tokens in logs

---

## 🎯 Quick Verification Script

Save this as `test-logging.sh`:

```bash
#!/bin/bash

echo "🧪 Testing Logging Implementation"
echo "=================================="

# Start services
echo "📦 Starting dependencies..."
docker-compose up -d postgres redis
sleep 3

# Start server in background
echo "🚀 Starting server..."
./gradlew :server:run > server.log 2>&1 &
SERVER_PID=$!
sleep 10

# Test registration
echo "📝 Testing registration..."
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "logtest",
    "displayName": "Log Test",
    "email": "logtest@example.com",
    "password": "test123"
  }' | jq .

# Check logs
echo ""
echo "📋 Server logs:"
echo "==============="
grep -E "AuthRepository|UserRepository" server.log | tail -20

# Cleanup
echo ""
echo "🧹 Cleaning up..."
kill $SERVER_PID
rm server.log

echo "✅ Test complete!"
```

Run it:
```bash
chmod +x test-logging.sh
./test-logging.sh
```

---

## 🐛 Troubleshooting

### No logs appearing?

1. **Check AppConfig is initialized:**
   ```kotlin
   // Add this at app startup
   AppConfig.initialize(isDevelopment = true)
   ```

2. **Check log level:**
   ```bash
   # Server: Set environment variable
   export LOG_LEVEL=DEBUG
   ```

3. **Check Logback configuration:**
   - File: `server/src/main/resources/logback.xml`
   - Ensure root level is DEBUG or INFO

### Logs show in development but not production?

This is **expected behavior**! Production mode:
- Filters DEBUG logs (only INFO, WARN, ERROR)
- Redacts sensitive data
- Reduces HTTP logging verbosity

### Want to see all logs in production?

**Not recommended**, but for debugging:

```kotlin
// Temporarily enable development mode
AppConfig.initialize(isDevelopment = true)
```

Or set log level:
```bash
export LOG_LEVEL=DEBUG
```

---

## 📊 Log Levels Guide

| Level | When to Use | Example |
|-------|-------------|---------|
| **DEBUG** | Detailed diagnostic info | `AppLogger.debug(TAG, "Processing item $id")` |
| **INFO** | Important business events | `AppLogger.info(TAG, "User registered: $username")` |
| **WARN** | Potential issues | `AppLogger.warn(TAG, "Retry attempt $count")` |
| **ERROR** | Failures and exceptions | `AppLogger.error(TAG, "Failed to save", exception)` |

### Sensitive Data

Always use `debugSensitive()` for:
- Authentication tokens (access, refresh)
- Passwords or credentials
- Personal identifiable information (PII)
- API keys or secrets

```kotlin
// ✅ Good
AppLogger.debugSensitive(TAG, "Token: $token")

// ❌ Bad
AppLogger.debug(TAG, "Token: $token")
```

---

## 🎉 Success Criteria

You've successfully implemented logging when:

1. ✅ Logs appear in development mode
2. ✅ Sensitive data is redacted in production mode
3. ✅ Logs are structured with tags and context
4. ✅ Different log levels work correctly
5. ✅ Platform-specific logging works (Android, iOS, Server)
6. ✅ No `println()` statements in production code

---

## 📚 Related Documentation

- [Logging Guide](./LOGGING.md) - Complete logging documentation
- [Testing User Profile](./TESTING_USER_PROFILE.md) - API testing guide
- [Pre-Push Checklist](./PRE_PUSH_CHECKLIST.md) - Production readiness

---

**Need help?** Check the full logging guide at `docs/GUIDES/LOGGING.md`

