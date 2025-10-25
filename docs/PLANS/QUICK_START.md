# Quick Start Guide

**Get NearYou ID running in 5 minutes**

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| **Docker Desktop** | 20.10+ | [docker.com](https://www.docker.com/products/docker-desktop) |
| **JDK** | 17+ | [adoptium.net](https://adoptium.net/) |
| **Android Studio** | Latest | [developer.android.com](https://developer.android.com/studio) |
| **Xcode** | 15+ (macOS) | App Store |

---

## Setup Steps

### 1. Clone Repository

```bash
git clone <repository-url>
cd nearyou-id
```

### 2. Start Infrastructure

```bash
# Start PostgreSQL + Redis
docker-compose up -d

# Verify services
docker ps
```

Expected output:
- `nearyou-postgres` on port 5432
- `nearyou-redis` on port 6379

### 3. Run Backend Server

```bash
./gradlew :server:run
```

Server starts on `http://localhost:8080`

**Verify:**
```bash
curl http://localhost:8080/health
# Expected: {"status":"ok"}
```

### 4. Run Android App

```bash
# Install on connected device/emulator
./gradlew :composeApp:installDebug

# Or open in Android Studio
# File → Open → Select project root
# Run → Run 'composeApp'
```

### 5. Run iOS App (macOS only)

```bash
# Open Xcode project
open iosApp/iosApp.xcodeproj

# Select simulator and press Run (⌘R)
```

---

## Environment Variables

Create `.env` file in project root:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/nearyou_db
DATABASE_USER=nearyou_user
DATABASE_PASSWORD=nearyou_pass

# Redis
REDIS_URL=redis://localhost:6379

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_ISSUER=nearyou-id
JWT_AUDIENCE=nearyou-api

# Google OAuth (optional for development)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

**For production setup, see [INFRA.md](../CORE/INFRA.md)**

---

## Verify Setup

### Check Database

```bash
docker exec -it nearyou-postgres psql -U nearyou_user -d nearyou_db -c "SELECT PostGIS_Version();"
```

### Run Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :server:test
./gradlew :shared:test
```

### Check Build

```bash
./gradlew build
```

---

## Common Issues

### Port Already in Use

```bash
# Check what's using port 5432
lsof -i :5432

# Stop conflicting service or change port in docker-compose.yml
```

### Docker Not Running

```bash
# Start Docker Desktop
open -a Docker

# Wait for Docker to start, then retry
docker-compose up -d
```

### Gradle Build Fails

```bash
# Clean and rebuild
./gradlew clean build

# If still fails, check JDK version
java -version  # Should be 17+
```

---

## Development Workflow

1. **Create task branch**
   ```bash
   git checkout -b task/T-XXX-description
   ```

2. **Make changes**
   - Edit code
   - Add tests
   - Update documentation

3. **Verify locally**
   ```bash
   ./gradlew build test
   ```

4. **Commit and push**
   ```bash
   git add .
   git commit -m "T-XXX: Description"
   git push origin task/T-XXX-description
   ```

5. **Create Pull Request**
   - See [VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md) for validation procedures

---

## Next Steps

- **[ARCHITECTURE.md](../CORE/ARCHITECTURE.md)** → Understand system design
- **[SPEC.md](../CORE/SPEC.md)** → Read product specification
- **[TESTING.md](../CORE/TESTING.md)** → Learn testing strategy
- **[VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)** → Validation procedures
- **[API_DOCUMENTATION.md](../API_DOCUMENTATION.md)** → API reference

---

## Getting Help

- Check [PROJECT_MAP.md](../CORE/PROJECT_MAP.md) for documentation index
- Review [INFRA.md](../CORE/INFRA.md) for detailed infrastructure setup
- See [DECISIONS.md](../CORE/DECISIONS.md) for architectural decisions
