# Database Documentation

**For complete database and infrastructure documentation, see [docs/CORE/INFRA.md](../docs/CORE/INFRA.md)**

This directory contains database initialization scripts and migrations for NearYou ID.

---

## Structure

```
database/
├── init.sql                    # PostGIS extension and initial setup
├── migrations/                 # Database migrations
│   ├── 001_initial_schema.sql # Initial schema with all core tables
│   └── 002_auth_tables.sql    # Authentication tables (otp_codes, refresh_tokens)
└── README.md                   # This file
```

---

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

---

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

---

## Complete Documentation

For detailed information including:
- Complete database schema
- All table definitions
- Database migrations guide
- Performance tuning
- Backup & restore procedures
- Connection pooling configuration
- Troubleshooting guide

**→ See [docs/CORE/INFRA.md](../docs/CORE/INFRA.md#database-setup)**

---

## Security Notes

⚠️ **Important:** The default credentials are for development only!

For production:
1. Use strong, randomly generated passwords
2. Store credentials in environment variables or secrets manager
3. Enable SSL/TLS for database connections
4. Restrict network access to database
5. Enable PostgreSQL audit logging
6. Regular security updates

---

## References

- [PostGIS Documentation](https://postgis.net/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- **[Project Infrastructure Documentation](../docs/CORE/INFRA.md)** ← Main reference
