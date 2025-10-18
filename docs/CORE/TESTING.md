# NearYou ID - Testing Strategy

**Version:** 1.1  
**Last Updated:** 2025-10-17  
**Status:** Active

---

## Testing Overview

This document outlines the testing strategy for NearYou ID, including unit tests, integration tests, end-to-end tests, and performance tests. The goal is to achieve >80% code coverage and ensure high-quality, reliable software.

---

## Testing Pyramid

```
                    ┌─────────────┐
                    │   E2E Tests │  (10%)
                    └─────────────┘
                  ┌───────────────────┐
                  │ Integration Tests │  (30%)
                  └───────────────────┘
              ┌─────────────────────────────┐
              │       Unit Tests            │  (60%)
              └─────────────────────────────┘
```

### Test Distribution
- **Unit Tests:** 60% - Fast, isolated, test individual components
- **Integration Tests:** 30% - Test component interactions
- **E2E Tests:** 10% - Test complete user flows

---

*(All original sections about Unit, Integration, E2E, Performance, CI/CD, etc. remain the same.)*

---

## Best Practices Reference

Before implementing or testing any feature:
1. **Verify Latest Standards**: Check official documentation for the latest API versions and best practices
2. **Official Examples**: Reference official GitHub repositories for current implementation patterns
3. **Documentation Priority**: Official docs → Official examples → Established community resources
4. **Stay Updated**: Regularly check for updates to frameworks and libraries used in the project

**Key Official Documentation:**
- Kotlin: https://kotlinlang.org/docs/
- Ktor: https://ktor.io/docs/
- Compose Multiplatform: https://www.jetbrains.com/lp/compose-multiplatform/
- PostgreSQL: https://www.postgresql.org/docs/
- PostGIS: https://postgis.net/documentation/

---

## Git Workflow for Testing

### Branch Strategy
**Always create a task-specific branch before any changes:**

```bash
# Create and switch to task branch
git checkout -b task/{task_id}-{description}

# Examples:
git checkout -b task/T-101-backend-auth
git checkout -b task/T-202-post-creation
```

**Never commit directly to main or develop.**

### Testing Workflow
1. Create branch → Implement → Test locally → Create validation report
2. If all tests pass: Push branch → Create PR → Wait for CI
3. If tests fail: Fix issues → Re-test → Update validation report
4. After PR approved: Merge → Delete branch

---

## Incremental Development & Testing

### Error-Free Progression Principle
Never accumulate errors across multiple files. Always verify each change immediately.

**Workflow:**
1. **Create/Modify File** → Verify immediately
2. **Fix Errors** → Test fix works
3. **Move to Next File** → Repeat

**Verification Commands:**
```bash
# After modifying Kotlin file
./gradlew compileKotlin

# After adding dependency
./gradlew build

# After creating test
./gradlew test --tests "SpecificTest"

# After database change
psql -U user -d db -c "SELECT 1;" # Quick connection test
```

**Red Flags (Stop & Fix):**
- ❌ Compilation errors
- ❌ Missing imports
- ❌ Type mismatches
- ❌ Syntax errors
- ❌ Test failures
- ❌ Broken references

**Don't Continue If:**
- Current file has any compilation errors
- Tests related to current change are failing
- Database migration fails to apply
- Dependencies cannot be resolved

**Example Good Flow:**
```
1. Create UserRepository.kt
2. Run: ./gradlew :server:compileKotlin ✅
3. Create UserRepositoryTest.kt
4. Run: ./gradlew :server:test --tests "UserRepositoryTest" ✅
5. Move to next file (AuthService.kt)
```

**Example Bad Flow (Don't Do This):**
```
❌ Create 5 files with errors
❌ Try to run tests → fails
❌ Spend 30 minutes debugging across 5 files
```

---

## Continuous Improvement

1. **Monitor Coverage:** Track coverage trends over time
2. **Review Flaky Tests:** Identify and fix unstable tests
3. **Performance Benchmarks:** Track performance metrics
4. **Test Maintenance:** Regularly update and refactor tests
5. **Team Training:** Ensure team follows testing best practices

---

## 🤖 AI vs Human Testing Responsibilities

### AI Responsibilities (Terminal Access Available)
When AI has terminal access, it should handle:
- ✅ Running test commands (`./gradlew test`, `./gradlew build`)
- ✅ Checking file existence and content
- ✅ Verifying code syntax and structure
- ✅ Running linters and formatters
- ✅ Checking database schema via SQL queries
- ✅ Verifying environment setup (Docker, services)
- ✅ Git operations (branch, commit, status checks)
- ✅ Building and compiling code
- ✅ Inspecting logs and error messages

### Human Responsibilities (Manual Steps Required)
Humans must handle:
- ⚠️ **First-time account setup** (Google, Firebase, Twilio, SendGrid)
- ⚠️ **OAuth flows** (Google Sign-In configuration, callbacks)
- ⚠️ **App store submissions** (Google Play, App Store)
- ⚠️ **First-time app launch** on physical devices
- ⚠️ **Manual UI testing** (visual verification, UX flow)
- ⚠️ **Online configuration** (Firebase Console, AWS Console, GCP Console)
- ⚠️ **Payment gateway testing** (Stripe, subscription flows)
- ⚠️ **Push notification testing** (actual device notifications)
- ⚠️ **External service integration** verification (requires web login)

### Validation Mode Decision Matrix

| Task Type | Terminal Testable? | Validation Owner | Notes |
|-----------|-------------------|------------------|-------|
| Backend API endpoints | ✅ Yes | AI | Can use curl/http tools |
| Database operations | ✅ Yes | AI | Can use psql/SQL queries |
| Unit/Integration tests | ✅ Yes | AI | Can run test commands |
| Build & compilation | ✅ Yes | AI | Can run build commands |
| Code structure | ✅ Yes | AI | Can verify files/syntax |
| First-time app launch | ❌ No | HUMAN | Requires device/emulator UI |
| Google OAuth setup | ❌ No | HUMAN | Requires web console access |
| Push notification flow | ❌ No | HUMAN | Requires physical device |
| Payment processing | ❌ No | HUMAN | Requires payment provider access |
| Visual UI verification | ❌ No | HUMAN | Requires human eyes |
| Third-party service setup | ❌ No | HUMAN | Requires web login/config |

### Example: Task Validation Planning

**Task T-101 (Backend Auth Service):**
- ✅ AI: Build, test, verify endpoints via curl, check database
- ⚠️ HUMAN: Verify in Postman (optional), check logs manually
- **Result:** validation_owner = HYBRID (mostly AI, human optional)

**Task T-102 (Frontend Auth Flows):**
- ✅ AI: Build, compile, verify code structure
- ⚠️ HUMAN: Launch app, test Google Sign-In flow, verify UI
- **Result:** validation_owner = HYBRID (AI for code, human for runtime)

**Task T-303 (FCM Notifications):**
- ✅ AI: Build, verify FCM integration code
- ⚠️ HUMAN: Configure Firebase Console, test actual notifications on device
- **Result:** validation_owner = HYBRID (AI for code, human for service setup)

---

## 🧩 Appendix — How to Validate Changes (Non-technical & Technical)

Not all tasks require running code to verify correctness. Some involve reviewing generated documents, ensuring consistency, or confirming manual steps.  
Use the appropriate validation method below depending on the task type and validation owner.

---

### A. Document-Only Validation
For tasks that modify or generate documentation only (e.g., updating specs, plans, or architecture):

- Create a validation report under `docs/TEST_REPORTS/{task_id}_VALIDATION.md` using the [Task Validation Template](../TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md).
- Review all related documents for:
    - Cross-file consistency (Plan ↔ Architecture ↔ Spec)
    - Correct links, paths, and formatting
    - Removal of placeholders (e.g., “TODO”, “TBD”)
- Attach evidence (quotes, screenshots, timestamps)
- Mark PASS or FAIL in the report

> **validation_owner:** `AI` (for consistency and text checks)  
> **validation_owner:** `HUMAN` (for subjective or domain review)

---

### B. Command-Driven Validation
For tasks that require executing commands or running code:

- Use Gradle or tool-specific commands such as:
  ```bash
  ./gradlew test
  ./gradlew integrationTest
  ./gradlew :composeApp:connectedDebugAndroidTest
  maestro test maestro/
  k6 run k6/load_test.js
  ```
- Capture logs or reports as validation evidence
- Include the results in `docs/TEST_REPORTS/{task_id}_VALIDATION.md`
- Confirm PASS criteria defined in the task plan are met

> **validation_owner:** `AI` (if verifiable through logs/reports)  
> **validation_owner:** `HUMAN` (if manual environment or credentials required)

---

### C. Mixed Validation (HYBRID)
For tasks that involve both documentation updates and technical execution:
1. Perform document validation as in section A
2. Execute relevant commands as in section B
3. Merge evidence into one validation report
4. Indicate clearly which parts were validated by AI and which by a human reviewer
5. Update Progress Ledger and Changelog after both validations pass

---

### D. Validation Reporting
- Store all validation reports in `docs/TEST_REPORTS/`
- File naming format: `T-###_VALIDATION.md`
- Each report must include:
    - validation_owner (`AI`, `HUMAN`, or `HYBRID`)
    - who_validated (agent name or initials)
    - evidence (paths, screenshots, or code snippets)
    - result (PASS/FAIL)
    - follow-up actions if applicable

---

*This appendix is part of the validation-first discipline and complements the short meta prompt in `docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md`.*
