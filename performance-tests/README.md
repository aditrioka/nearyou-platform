# Performance Testing with k6

This directory contains performance and load testing scripts for the Near You ID authentication API.

## Prerequisites

- k6 installed (`brew install k6` on macOS)
- Server running on `http://localhost:8080`
- PostgreSQL and Redis running

## Test Scripts

### 1. Authentication Load Test (`auth-load-test.js`)

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

**Performance Thresholds:**
- 95% of requests < 500ms
- Error rate < 10%

## Running Tests

### Run the authentication load test:
```bash
k6 run performance-tests/auth-load-test.js
```

### Run with custom duration:
```bash
k6 run --duration 30s performance-tests/auth-load-test.js
```

### Run with custom VUs (Virtual Users):
```bash
k6 run --vus 50 --duration 1m performance-tests/auth-load-test.js
```

### Run with output to InfluxDB:
```bash
k6 run --out influxdb=http://localhost:8086/k6 performance-tests/auth-load-test.js
```

### Run with JSON output:
```bash
k6 run --out json=test-results.json performance-tests/auth-load-test.js
```

## Interpreting Results

### Key Metrics

**http_req_duration:** Response time
- p(95): 95th percentile - 95% of requests were faster than this
- p(99): 99th percentile - 99% of requests were faster than this
- avg: Average response time
- min/max: Fastest and slowest requests

**http_req_failed:** Failed requests
- Should be < 10% for passing tests

**http_reqs:** Total number of requests
- Higher is better (more throughput)

**vus:** Virtual users
- Number of concurrent users

**iterations:** Number of complete test iterations
- Each iteration runs all 4 endpoint tests

### Example Output

```
✓ register status is 200
✓ register returns OTP sent message
✓ login status is 200 or 404
✓ verify-otp responds
✓ refresh responds

checks.........................: 100.00% ✓ 5000      ✗ 0
data_received..................: 2.5 MB  42 kB/s
data_sent......................: 1.2 MB  20 kB/s
http_req_blocked...............: avg=1.2ms    min=1µs     med=3µs     max=150ms   p(90)=5µs     p(95)=7µs
http_req_connecting............: avg=500µs    min=0s      med=0s      max=50ms    p(90)=0s      p(95)=0s
http_req_duration..............: avg=120ms    min=10ms    med=100ms   max=500ms   p(90)=200ms   p(95)=250ms
  { expected_response:true }...: avg=120ms    min=10ms    med=100ms   max=500ms   p(90)=200ms   p(95)=250ms
http_req_failed................: 0.00%   ✓ 0         ✗ 5000
http_req_receiving.............: avg=50µs     min=10µs    med=40µs    max=5ms     p(90)=80µs    p(95)=100µs
http_req_sending...............: avg=20µs     min=5µs     med=15µs    max=2ms     p(90)=30µs    p(95)=40µs
http_req_tls_handshaking.......: avg=0s       min=0s      med=0s      max=0s      p(90)=0s      p(95)=0s
http_req_waiting...............: avg=119ms    min=9ms     med=99ms    max=499ms   p(90)=199ms   p(95)=249ms
http_reqs......................: 5000    83.33/s
iteration_duration.............: avg=5.5s     min=5s      med=5.5s    max=6s      p(90)=5.8s    p(95)=5.9s
iterations.....................: 1000    16.67/s
vus............................: 1       min=1       max=100
vus_max........................: 100     min=100     max=100
```

## Performance Targets

### Response Time Targets
- **p(95) < 500ms:** 95% of requests should complete in under 500ms
- **p(99) < 1000ms:** 99% of requests should complete in under 1 second
- **avg < 200ms:** Average response time should be under 200ms

### Throughput Targets
- **Minimum:** 50 requests/second
- **Target:** 100 requests/second
- **Stretch:** 200+ requests/second

### Error Rate Targets
- **Maximum:** 10% error rate
- **Target:** < 5% error rate
- **Ideal:** < 1% error rate

## Troubleshooting

### High Response Times
- Check database connection pool settings
- Monitor database query performance
- Check Redis connection
- Review server logs for errors

### High Error Rates
- Check server logs for exceptions
- Verify database and Redis are running
- Check rate limiting configuration
- Monitor server resources (CPU, memory)

### Connection Errors
- Verify server is running on correct port
- Check firewall settings
- Ensure PostgreSQL and Redis are accessible

## Best Practices

1. **Baseline First:** Run tests with low load to establish baseline
2. **Gradual Increase:** Increase load gradually to find breaking points
3. **Monitor Resources:** Watch CPU, memory, database connections
4. **Realistic Data:** Use realistic test data and scenarios
5. **Consistent Environment:** Run tests in consistent environment
6. **Multiple Runs:** Run tests multiple times for reliability

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

## Additional Resources

- [k6 Documentation](https://k6.io/docs/)
- [k6 Best Practices](https://k6.io/docs/testing-guides/test-types/)
- [Performance Testing Guide](https://k6.io/docs/testing-guides/)

