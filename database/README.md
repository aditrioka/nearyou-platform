# Database Documentation

## Overview

This directory contains database initialization scripts and migrations for NearYou ID.

## Structure

```
database/
├── init.sql                    # PostGIS extension and initial setup
├── migrations/                 # Database migrations
│   └── 001_initial_schema.sql # Initial schema with all core tables
└── README.md                   # This file
```

## Quick Start

### 1. Start Database

```bash
docker-compose up -d postgres
```

### 2. Verify PostGIS Installation

```bash
docker exec -it nearyou-postgres psql -U nearyou_user -d nearyou_db -c "SELECT PostGIS_Version();"
```

Expected output:
```
           postgis_version
-------------------------------------
 3.3 USE_GEOS=1 USE_PROJ=1 USE_STATS=1
```

### 3. Test Geo Query

```bash
docker exec -it nearyou-postgres psql -U nearyou_user -d nearyou_db
```

Then run:
```sql
-- Insert test post
INSERT INTO posts (user_id, content, location)
VALUES (
    gen_random_uuid(),
    'Test post',
    ST_MakePoint(106.8456, -6.2088)::geography
);

-- Find posts within 1 km
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

## Database Schema

### Core Tables

1. **users** - User accounts and profiles
2. **posts** - User posts with geospatial data
3. **likes** - Post likes
4. **comments** - Post comments
5. **conversations** - Chat conversations
6. **messages** - Chat messages
7. **follows** - User follow relationships
8. **notifications** - Push notifications
9. **device_tokens** - FCM device tokens
10. **subscriptions** - Subscription management
11. **usage_logs** - Quota tracking
12. **reports** - Content/user reports
13. **blocks** - User blocks

### Key Indexes

- **GiST Index on posts.location** - Critical for geo queries
- **B-tree indexes** - For foreign keys and frequently queried columns
- **Partial indexes** - For filtered queries (e.g., is_deleted = FALSE)

## Connection Details

### Local Development

```
Host: localhost
Port: 5432
Database: nearyou_db
Username: nearyou_user
Password: nearyou_password
```

**JDBC URL:**
```
jdbc:postgresql://localhost:5432/nearyou_db?user=nearyou_user&password=nearyou_password
```

**Connection String:**
```
postgresql://nearyou_user:nearyou_password@localhost:5432/nearyou_db
```

## Migrations

### Adding New Migrations

1. Create new file: `database/migrations/00X_description.sql`
2. Follow naming convention: `XXX_description.sql` (e.g., `002_add_user_settings.sql`)
3. Include rollback instructions in comments
4. Test migration on local database
5. Update this README with migration details

### Migration Template

```sql
-- Migration XXX: Description
-- Created: YYYY-MM-DD
-- Description: What this migration does

-- Forward migration
CREATE TABLE ...;

-- Rollback (in comments)
-- DROP TABLE ...;
```

## Backup & Restore

### Backup

```bash
docker exec nearyou-postgres pg_dump -U nearyou_user nearyou_db > backup.sql
```

### Restore

```bash
docker exec -i nearyou-postgres psql -U nearyou_user nearyou_db < backup.sql
```

## Performance Tips

### Analyze Query Performance

```sql
EXPLAIN ANALYZE
SELECT * FROM posts
WHERE ST_DWithin(location, ST_MakePoint(106.8456, -6.2088)::geography, 1000);
```

Look for:
- "Index Scan using idx_posts_location" (good)
- "Seq Scan" (bad - means index not used)

### Vacuum and Analyze

```sql
VACUUM ANALYZE posts;
```

### Check Index Usage

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

## Troubleshooting

### PostGIS Extension Not Found

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

### Connection Refused

```bash
# Check if container is running
docker ps | grep postgres

# Check logs
docker logs nearyou-postgres

# Restart container
docker-compose restart postgres
```

### Slow Queries

1. Check if indexes are being used: `EXPLAIN ANALYZE`
2. Run `VACUUM ANALYZE` on affected tables
3. Check PostgreSQL configuration (shared_buffers, effective_cache_size)
4. Consider adding more specific indexes

## Security Notes

⚠️ **Important:** The default credentials are for development only!

For production:
1. Use strong, randomly generated passwords
2. Store credentials in environment variables or secrets manager
3. Enable SSL/TLS for database connections
4. Restrict network access to database
5. Enable PostgreSQL audit logging
6. Regular security updates

## References

- [PostGIS Documentation](https://postgis.net/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spatial Queries Guide](https://postgis.net/workshops/postgis-intro/)

