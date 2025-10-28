# Performance Testing

**For complete performance testing documentation, see [docs/CORE/PERFORMANCE.md](../docs/CORE/PERFORMANCE.md)**

This directory contains k6 load testing scripts for the Near You ID authentication API.

---

## Quick Start

### Prerequisites
- k6 installed (`brew install k6` on macOS)
- Server running on `http://localhost:8080`
- PostgreSQL and Redis running

### Run Tests

```bash
# Run the authentication load test
k6 run performance-tests/auth-load-test.js

# Run with custom duration
k6 run --duration 30s performance-tests/auth-load-test.js

# Run with custom VUs (Virtual Users)
k6 run --vus 50 --duration 1m performance-tests/auth-load-test.js
```

---

## Test Scripts

### `auth-load-test.js`

Comprehensive load test for all authentication endpoints.

**Test Stages:**
1. Ramp up to 10 users (30s)
2. Ramp up to 50 users (1m)
3. Stay at 50 users (2m)
4. Spike to 100 users (30s)
5. Stay at 100 users (1m)
6. Ramp down to 0 users (30s)

**Total Duration:** ~5.5 minutes

**Endpoints Tested:**
- POST /auth/register
- POST /auth/login
- POST /auth/verify-otp
- POST /auth/refresh

---

## Performance Targets

- **Response Time:** p(95) < 500ms (light load), < 2s (heavy load)
- **Throughput:** 100+ requests/second
- **Error Rate:** < 10%
- **Uptime:** 99.9% target

---

## Documentation

For detailed performance analysis, metrics, bottlenecks, optimization strategies, and monitoring guidelines, see:

**→ [docs/CORE/PERFORMANCE.md](../docs/CORE/PERFORMANCE.md)**

This document includes:
- Complete test results and analysis
- Response time distribution
- Scalability projections
- Bottleneck analysis
- Optimization strategies
- Monitoring guidelines
- Production readiness assessment

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Performance Tests

on:
  pull_request:
    branches: [ main ]

jobs:
  performance:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Start services
        run: docker-compose up -d
      
      - name: Wait for services
        run: sleep 10
      
      - name: Run k6 tests
        uses: grafana/k6-action@v0.3.0
        with:
          filename: performance-tests/auth-load-test.js
          flags: --out json=results.json
      
      - name: Upload results
        uses: actions/upload-artifact@v2
        with:
          name: k6-results
          path: results.json
```

---

## Additional Resources

- [k6 Documentation](https://k6.io/docs/)
- [k6 Best Practices](https://k6.io/docs/testing-guides/test-types/)
- [Performance Testing Guide](https://k6.io/docs/testing-guides/)
- **[Project Performance Documentation](../docs/CORE/PERFORMANCE.md)** ← Main reference
