# NearYou ID - Infrastructure Documentation

**Version:** 1.0  
**Last Updated:** 2025-10-16  
**Status:** Active

---

## Infrastructure Overview

This document describes the infrastructure setup, database schema, deployment strategy, and operational procedures for NearYou ID.

---

## Development Environment

### Prerequisites

- **Docker:** 20.10+ (for PostgreSQL, Redis)
- **Docker Compose:** 2.0+
- **JDK:** 17+ (for Kotlin/Ktor)
- **Gradle:** 8.0+ (included via wrapper)
- **Android Studio:** Latest stable (for Android development)
- **Xcode:** 15+ (for iOS development, macOS only)

### Local Setup

1. **Clone Repository:**
   ```bash
   git clone <repository-url>
   cd nearyou-id
   ```

2. **Start Infrastructure:**
   ```bash
   docker-compose up -d
   ```

3. **Run Database Migrations:**
   ```bash
   ./gradlew flywayMigrate
   ```

4. **Start Backend Server:**
   ```bash
   ./gradlew :server:run
   ```

5. **Run Android App:**
   ```bash
   ./gradlew :composeApp:installDebug
   ```

6. **Run iOS App:**
   ```bash
   cd iosApp
   xcodebuild -scheme iosApp -configuration Debug
   ```

---

## Database Setup

### PostgreSQL with PostGIS

**Version:** PostgreSQL 15.3 with PostGIS 3.3

**Docker Compose Configuration:**
```yaml
services:
  postgres:
    image: postgis/postgis:15-3.3
    container_name: nearyou-postgres
    environment:
      POSTGRES_DB: nearyou_db
      POSTGRES_USER: nearyou_user
      POSTGRES_PASSWORD: nearyou_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U nearyou_user"]
      interval: 10s
      timeout: 5s
      retries: 5
```

**Connection String:**
```
jdbc:postgresql://localhost:5432/nearyou_db?user=nearyou_user&password=nearyou_password
```

### Database Schema

#### Initial Schema (Migration 001)

**users table:**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(20) UNIQUE NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255),
    bio VARCHAR(200),
    profile_photo_url TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    subscription_tier VARCHAR(20) DEFAULT 'free',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
```

**posts table:**
```sql
CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL CHECK (LENGTH(content) <= 500 AND LENGTH(content) >= 1),
    location GEOGRAPHY(Point, 4326) NOT NULL,
    media_urls TEXT[],
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_posts_location ON posts USING GIST(location) WHERE is_deleted = FALSE;
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC) WHERE is_deleted = FALSE;
```

**likes table:**
```sql
CREATE TABLE likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(post_id, user_id)
);

CREATE INDEX idx_likes_post_id ON likes(post_id);
CREATE INDEX idx_likes_user_id ON likes(user_id);
```

**comments table:**
```sql
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL CHECK (LENGTH(content) <= 500),
    parent_comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_comments_post_id ON comments(post_id, created_at DESC);
CREATE INDEX idx_comments_user_id ON comments(user_id);
```

**conversations table:**
```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant_1_id UUID REFERENCES users(id) ON DELETE CASCADE,
    participant_2_id UUID REFERENCES users(id) ON DELETE CASCADE,
    post_context_id UUID REFERENCES posts(id) ON DELETE SET NULL,
    last_message_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(participant_1_id, participant_2_id)
);

CREATE INDEX idx_conversations_participant_1 ON conversations(participant_1_id, last_message_at DESC);
CREATE INDEX idx_conversations_participant_2 ON conversations(participant_2_id, last_message_at DESC);
```

**messages table:**
```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'sent',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_messages_sender ON messages(sender_id);
```

### PostGIS Verification

**Enable PostGIS Extension:**
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

**Verify Installation:**
```sql
SELECT PostGIS_Version();
```

Expected output:
```
           postgis_version
-------------------------------------
 3.3 USE_GEOS=1 USE_PROJ=1 USE_STATS=1
```

**Test Geo Query:**
```sql
-- Insert test post
INSERT INTO posts (user_id, content, location)
VALUES (
    gen_random_uuid(),
    'Test post',
    ST_MakePoint(106.8456, -6.2088)::geography
);

-- Find posts within 1 km of a location
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
ORDER BY created_at DESC
LIMIT 50;
```

### Database Migrations

The database migrations are located in `database/migrations/` directory.

**Structure:**
```
database/
├── init.sql                    # PostGIS extension and initial setup
├── migrations/                 # Database migrations
│   ├── 001_initial_schema.sql # Initial schema with all core tables
│   └── 002_auth_tables.sql    # Authentication tables (otp_codes, refresh_tokens)
└── README.md                   # Merged into this document
```

#### Adding New Migrations

1. Create new file: `database/migrations/00X_description.sql`
2. Follow naming convention: `XXX_description.sql` (e.g., `003_add_user_settings.sql`)
3. Include rollback instructions in comments
4. Test migration on local database
5. Update this document with migration details

#### Migration Template

```sql
-- Migration XXX: Description
-- Created: YYYY-MM-DD
-- Description: What this migration does

-- Forward migration
CREATE TABLE ...;

-- Rollback (in comments)
-- DROP TABLE ...;
```

### Database Performance

#### Analyze Query Performance

```sql
EXPLAIN ANALYZE
SELECT * FROM posts
WHERE ST_DWithin(location, ST_MakePoint(106.8456, -6.2088)::geography, 1000);
```

Look for:
- "Index Scan using idx_posts_location" (good)
- "Seq Scan" (bad - means index not used)

#### Vacuum and Analyze

```sql
VACUUM ANALYZE posts;
```

#### Check Index Usage

```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
```

#### Database Backup & Restore

**Backup:**
```bash
docker exec nearyou-postgres pg_dump -U nearyou_user nearyou_db > backup.sql
```

**Restore:**
```bash
docker exec -i nearyou-postgres psql -U nearyou_user nearyou_db < backup.sql
```

---

## Redis Setup

**Version:** Redis 7.0

**Docker Compose Configuration:**
```yaml
services:
  redis:
    image: redis:7.0-alpine
    container_name: nearyou-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
```

**Connection String:**
```
redis://localhost:6379
```

**Use Cases:**
- Query result caching (geo queries)
- Rate limiting counters
- Session storage (optional)
- Temporary OTP storage

---

## Environment Variables

### Backend Server

Create `.env` file in `/server` directory:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/nearyou_db
DATABASE_USER=nearyou_user
DATABASE_PASSWORD=nearyou_password

# Redis
REDIS_URL=redis://localhost:6379

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_ISSUER=nearyou-id
JWT_AUDIENCE=nearyou-api
JWT_REALM=nearyou

# S3/GCS (for media storage)
STORAGE_BUCKET=nearyou-media
STORAGE_REGION=us-east-1
STORAGE_ACCESS_KEY=your-access-key
STORAGE_SECRET_KEY=your-secret-key

# FCM (for push notifications)
FCM_PROJECT_ID=your-firebase-project-id
FCM_CREDENTIALS_PATH=/path/to/firebase-credentials.json

# OTP (for development, use mock)
OTP_PROVIDER=mock
# For production:
# OTP_PROVIDER=twilio
# TWILIO_ACCOUNT_SID=your-account-sid
# TWILIO_AUTH_TOKEN=your-auth-token
# TWILIO_PHONE_NUMBER=+1234567890

# Email (for development, use mock)
EMAIL_PROVIDER=mock
# For production:
# EMAIL_PROVIDER=sendgrid
# SENDGRID_API_KEY=your-api-key
# SENDGRID_FROM_EMAIL=noreply@nearyou.id

# Server
SERVER_PORT=8080
SERVER_HOST=0.0.0.0
```

### Mobile Apps

**Android (`local.properties`):**
```properties
sdk.dir=/path/to/Android/sdk
GOOGLE_SERVICES_JSON=/path/to/google-services.json
API_BASE_URL=http://10.0.2.2:8080
```

**iOS (`Config.xcconfig`):**
```
API_BASE_URL = http://localhost:8080
GOOGLE_SERVICES_PLIST = /path/to/GoogleService-Info.plist
```

---

## CI/CD Pipeline

### GitHub Actions Workflow

**File:** `.github/workflows/ci.yml`

**Jobs:**
1. **Lint:** Kotlin code style check (ktlint)
2. **Test Shared:** Run shared module tests
3. **Test Server:** Run backend tests
4. **Test Android:** Run Android instrumented tests
5. **Build Android:** Build APK
6. **Build iOS:** Build iOS app (macOS runner)
7. **Build Docker:** Build server Docker image
8. **Deploy:** Deploy to staging (manual trigger)

**Triggers:**
- Push to `main` branch
- Pull requests to `main`
- Manual workflow dispatch

---

## Deployment

### Docker Image

**Dockerfile for Server:**
```dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :server:shadowJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/server/build/libs/server-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build Image:**
```bash
docker build -t nearyou-server:latest .
```

**Run Container:**
```bash
docker run -p 8080:8080 --env-file .env nearyou-server:latest
```

### Kubernetes Deployment

**Deployment Manifest (`k8s/deployment.yaml`):**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nearyou-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nearyou-server
  template:
    metadata:
      labels:
        app: nearyou-server
    spec:
      containers:
      - name: server
        image: nearyou-server:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: nearyou-secrets
              key: database-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

**Service Manifest (`k8s/service.yaml`):**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: nearyou-server
spec:
  selector:
    app: nearyou-server
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

**Apply Manifests:**
```bash
kubectl apply -f k8s/
```

---

## Monitoring & Logging

### Prometheus Metrics

**Exposed Metrics:**
- `http_requests_total` - Total HTTP requests
- `http_request_duration_seconds` - Request latency
- `database_query_duration_seconds` - Database query time
- `geo_query_duration_seconds` - Geo query execution time
- `active_users` - Current active users

**Prometheus Configuration:**
```yaml
scrape_configs:
  - job_name: 'nearyou-server'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/metrics'
```

### Grafana Dashboards

**Key Dashboards:**
1. **API Health:** Request rate, error rate, latency
2. **Geo Query Performance:** Query execution time, cache hit rate
3. **User Activity:** Active users, posts created, messages sent
4. **Database Performance:** Connection pool, query time, slow queries

### Structured Logging

**Log Format (JSON):**
```json
{
  "timestamp": "2025-10-16T10:30:00Z",
  "level": "INFO",
  "logger": "com.nearyou.server.routes.PostRoutes",
  "message": "Post created",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "postId": "987fcdeb-51a2-43f7-8d9e-123456789abc",
  "duration_ms": 45
}
```

---

## Database Backups

### Automated Backups

**Daily Backup Script:**
```bash
#!/bin/bash
BACKUP_DIR="/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/nearyou_db_$TIMESTAMP.sql.gz"

pg_dump -h localhost -U nearyou_user nearyou_db | gzip > $BACKUP_FILE

# Keep last 30 days of backups
find $BACKUP_DIR -name "nearyou_db_*.sql.gz" -mtime +30 -delete
```

**Cron Schedule:**
```
0 2 * * * /path/to/backup.sh
```

### Restore from Backup

```bash
gunzip -c /backups/nearyou_db_20251016_020000.sql.gz | psql -h localhost -U nearyou_user nearyou_db
```

---

## Security

### SSL/TLS Configuration

**Let's Encrypt Certificate:**
```bash
certbot certonly --standalone -d api.nearyou.id
```

**Nginx Configuration:**
```nginx
server {
    listen 443 ssl http2;
    server_name api.nearyou.id;

    ssl_certificate /etc/letsencrypt/live/api.nearyou.id/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.nearyou.id/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Secrets Management

**Kubernetes Secrets:**
```bash
kubectl create secret generic nearyou-secrets \
  --from-literal=database-url=jdbc:postgresql://... \
  --from-literal=jwt-secret=... \
  --from-literal=storage-access-key=...
```

---

## Troubleshooting

### Common Issues

**1. Database Connection Failed:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U nearyou_user -d nearyou_db
```

**2. PostGIS Extension Not Found:**
```sql
-- Enable extension
CREATE EXTENSION IF NOT EXISTS postgis;
```

**3. Redis Connection Failed:**
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli ping
```

**4. Slow Geo Queries:**
```sql
-- Verify index usage
EXPLAIN ANALYZE
SELECT * FROM posts
WHERE ST_DWithin(location, ST_MakePoint(106.8456, -6.2088)::geography, 1000);

-- Should show "Index Scan using idx_posts_location"
```

---

## Performance Tuning

### PostgreSQL Configuration

**postgresql.conf:**
```
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 4MB
min_wal_size = 1GB
max_wal_size = 4GB
max_connections = 100
```

### Connection Pooling (HikariCP)

```kotlin
val config = HikariConfig().apply {
    jdbcUrl = System.getenv("DATABASE_URL")
    username = System.getenv("DATABASE_USER")
    password = System.getenv("DATABASE_PASSWORD")
    maximumPoolSize = 20
    minimumIdle = 5
    connectionTimeout = 30000
    idleTimeout = 600000
    maxLifetime = 1800000
}
```

---

## Runbook

### Deployment Checklist

- [ ] Run all tests locally
- [ ] Update version in `CHANGELOG.md`
- [ ] Build Docker image
- [ ] Push image to registry
- [ ] Update Kubernetes manifests
- [ ] Apply database migrations
- [ ] Deploy to staging
- [ ] Run smoke tests
- [ ] Deploy to production
- [ ] Monitor metrics for 1 hour
- [ ] Notify stakeholders

### Rollback Procedure

1. Identify problematic deployment version
2. Revert Kubernetes deployment: `kubectl rollout undo deployment/nearyou-server`
3. Verify rollback: `kubectl rollout status deployment/nearyou-server`
4. Revert database migrations if needed
5. Monitor metrics
6. Notify stakeholders

