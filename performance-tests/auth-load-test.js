import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up to 10 users
    { duration: '1m', target: 50 },   // Ramp up to 50 users
    { duration: '2m', target: 50 },   // Stay at 50 users
    { duration: '30s', target: 100 }, // Spike to 100 users
    { duration: '1m', target: 100 },  // Stay at 100 users
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.1'],    // Error rate should be less than 10%
    errors: ['rate<0.1'],              // Custom error rate should be less than 10%
  },
};

const BASE_URL = 'http://localhost:8080';

// Generate random email
function randomEmail() {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000);
  return `loadtest${timestamp}${random}@example.com`;
}

// Generate random username
function randomUsername() {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000);
  return `user${timestamp}${random}`;
}

export default function () {
  // Test 1: Register new user
  const email = randomEmail();
  const username = randomUsername();
  
  const registerPayload = JSON.stringify({
    username: username,
    displayName: `Load Test User ${username}`,
    email: email,
    password: 'TestPassword123!',
  });

  const registerParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const registerRes = http.post(
    `${BASE_URL}/auth/register`,
    registerPayload,
    registerParams
  );

  const registerSuccess = check(registerRes, {
    'register status is 200': (r) => r.status === 200,
    'register returns OTP sent message': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.message === 'OTP sent successfully';
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!registerSuccess);

  sleep(1);

  // Test 2: Login existing user (should return 401 VERIFICATION_PENDING for new users)
  const loginPayload = JSON.stringify({
    email: email,
  });

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    loginPayload,
    registerParams
  );

  const loginCheck = check(loginRes, {
    'login returns 401 for unverified user': (r) => r.status === 401,
    'login returns VERIFICATION_PENDING error': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.error && body.error.code === 'VERIFICATION_PENDING';
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!loginCheck);

  sleep(1);

  // Test 3: Verify OTP (should return 401 for invalid OTP)
  const verifyPayload = JSON.stringify({
    identifier: email,
    code: '123456', // Dummy OTP - will fail
    type: 'email',
  });

  const verifyRes = http.post(
    `${BASE_URL}/auth/verify-otp`,
    verifyPayload,
    registerParams
  );

  const verifyCheck = check(verifyRes, {
    'verify-otp returns 401 for invalid OTP': (r) => r.status === 401,
    'verify-otp returns proper error response': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.error && body.error.code;
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!verifyCheck);

  sleep(1);

  // Test 4: Refresh token (should return 401 for invalid token)
  const refreshPayload = JSON.stringify({
    refreshToken: 'invalid_token_for_load_testing',
  });

  const refreshRes = http.post(
    `${BASE_URL}/auth/refresh`,
    refreshPayload,
    registerParams
  );

  const refreshCheck = check(refreshRes, {
    'refresh returns 401 for invalid token': (r) => r.status === 401,
    'refresh returns proper error response': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.error && body.error.code;
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!refreshCheck);

  sleep(2);
}

// Setup function - runs once before the test
export function setup() {
  console.log('Starting load test...');
  console.log(`Target: ${BASE_URL}`);
  console.log('Test duration: ~5.5 minutes');
  console.log('Max concurrent users: 100');
}

// Teardown function - runs once after the test
export function teardown(data) {
  console.log('Load test completed!');
}

