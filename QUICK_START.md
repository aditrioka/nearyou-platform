# NearYou ID - Quick Start Guide

This guide will help you get the NearYou ID development environment up and running.

---

## Prerequisites

Ensure you have the following installed:

- **Docker Desktop** 20.10+ ([Download](https://www.docker.com/products/docker-desktop))
- **JDK** 17+ ([Download](https://adoptium.net/))
- **Android Studio** Latest stable ([Download](https://developer.android.com/studio))
- **Xcode** 15+ (macOS only, for iOS development)

---

## Quick Start (5 minutes)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd nearyou-id
```

### 2. Start Infrastructure

Start PostgreSQL and Redis using Docker Compose:

```bash
docker-compose up -d
```

Verify services are running:

```bash
docker ps
```

You should see:
- `nearyou-postgres` on port 5432
- `nearyou-redis` on port 6379

### 3. Verify Database

Check PostgreSQL and PostGIS:

```bash
docker exec -it nearyou-postgres psql -U nearyou_user -d nearyou_db -c "SELECT PostGIS_Version();"
```

Expected output:
```
           postgis_version
-------------------------------------
 3.3 USE_GEOS=1 USE_PROJ=1 USE_STATS=1
```

### 4. Run Tests

Run all tests to verify setup:

```bash
./gradlew test
```

All tests should pass ✅

### 5. Build the Project

Build all modules:

```bash
./gradlew build
```

---

## Running the Application

### Backend Server

Start the Ktor server:

```bash
./gradlew :server:run
```

Server will start on `http://localhost:8080`

### Android App

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select `composeApp` configuration
4. Click Run ▶️

Or via command line:

```bash
./gradlew :composeApp:installDebug
```

### iOS App

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator (iPhone 15)
3. Click Run ▶️

Or via command line:

```bash
cd iosApp
xcodebuild -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 15' build
```

---

## Development Workflow

### Running Tests

```bash
# All tests
./gradlew test

# Shared module only
./gradlew :shared:test

# Server module only
./gradlew :server:test

# Android tests
./gradlew :composeApp:testDebugUnitTest

# With coverage
./gradlew koverHtmlReport
open build/reports/kover/html/index.html
```

### Code Style

Check code style:

```bash
./gradlew ktlintCheck
```

Auto-format code:

```bash
./gradlew ktlintFormat
```

### Database Management

**Connect to database:**

```bash
docker exec -it nearyou-postgres psql -U nearyou_user -d nearyou_db
```

**Run SQL queries:**

```sql
-- List all tables
\dt

-- Describe a table
\d users

-- Query users
SELECT * FROM users;

-- Test geo query
SELECT 
    id, 
    content, 
    ST_Distance(location, ST_MakePoint(106.8456, -6.2088)::geography) AS distance_meters
FROM posts
WHERE ST_DWithin(
    location, 
    ST_MakePoint(106.8456, -6.2088)::geography, 
    1000
)
ORDER BY created_at DESC;
```

**Exit psql:**

```
\q
```

### Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# Restart services
docker-compose restart

# Remove all data (⚠️ destructive)
docker-compose down -v
```

---

## Project Structure

```
NearYou ID/
├── composeApp/          # Android & iOS UI (Compose Multiplatform)
├── shared/              # Shared business logic (KMP)
│   ├── commonMain/      # Platform-independent code
│   ├── commonTest/      # Shared tests
│   ├── androidMain/     # Android-specific code
│   ├── iosMain/         # iOS-specific code
│   └── jvmMain/         # JVM-specific code
├── server/              # Backend API (Ktor)
├── iosApp/              # iOS app wrapper
├── database/            # Database scripts
│   ├── init.sql         # PostGIS setup
│   └── migrations/      # Schema migrations
├── docs/                # Documentation
└── .github/workflows/   # CI/CD pipelines
```

---

## Common Issues

### Issue: Docker containers won't start

**Solution:**
```bash
docker-compose down -v
docker-compose up -d
```

### Issue: PostgreSQL connection refused

**Solution:**
```bash
# Check if container is running
docker ps | grep postgres

# Check logs
docker logs nearyou-postgres

# Restart container
docker-compose restart postgres
```

### Issue: Gradle build fails

**Solution:**
```bash
# Clean build
./gradlew clean build

# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build
```

### Issue: Android app can't connect to server

**Solution:**

Use `10.0.2.2` instead of `localhost` in Android emulator:

```kotlin
// In your API configuration
const val API_BASE_URL = "http://10.0.2.2:8080"
```

### Issue: iOS build fails

**Solution:**
```bash
# Clean Xcode build
cd iosApp
xcodebuild clean
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# Rebuild
xcodebuild -scheme iosApp -configuration Debug build
```

---

## Environment Variables

Create `.env` file in `/server` directory:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/nearyou_db
DATABASE_USER=nearyou_user
DATABASE_PASSWORD=nearyou_password

# Redis
REDIS_URL=redis://localhost:6379

# JWT (change in production!)
JWT_SECRET=your-secret-key-change-in-production
JWT_ISSUER=nearyou-id
JWT_AUDIENCE=nearyou-api

# Server
SERVER_PORT=8080
SERVER_HOST=0.0.0.0
```

---

## Useful Commands

```bash
# Check Java version
java -version

# Check Docker version
docker --version

# Check Gradle version
./gradlew --version

# List Gradle tasks
./gradlew tasks

# Build specific module
./gradlew :shared:build
./gradlew :server:build
./gradlew :composeApp:build

# Run with debug logging
./gradlew :server:run --debug

# Generate dependency report
./gradlew dependencies
```

---

## Next Steps

1. ✅ Complete Phase 0 setup (you are here!)
2. 📖 Read the documentation in `/docs`
3. 🔨 Start Phase 1: Authentication & User Management
4. 🧪 Write tests for new features
5. 🚀 Deploy to staging environment

---

## Resources

- **Documentation:** `/docs` directory
- **Project Plan:** `NearYou_ID_MVP_Plan.md`
- **Changelog:** `CHANGELOG.md`
- **Database Docs:** `database/README.md`
- **Phase 0 Summary:** `PHASE_0_COMPLETION_SUMMARY.md`

---

## Support

For issues or questions:
1. Check the documentation in `/docs`
2. Review the project plan: `NearYou_ID_MVP_Plan.md`
3. Check common issues above
4. Review ADRs in `docs/DECISIONS.md`

---

**Happy Coding! 🚀**

