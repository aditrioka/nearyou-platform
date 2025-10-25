# Validation Guide

**Version:** 1.0  
**Last Updated:** 2025-10-25  
**Status:** Active

---

## Overview

This guide defines how to validate changes in the NearYou ID project. All tasks must be validated before being marked complete.

---

## Validation Modes

| Mode | Who Validates | When to Use |
|------|---------------|-------------|
| **AI** | AI assistant | File consistency, structure, automated tests, build verification |
| **HUMAN** | Human developer | Manual testing, external services, UI/UX, first-time setup |
| **HYBRID** | Both AI & Human | Complex features requiring both automated and manual validation |

---

## AI Validation Capabilities

AI can validate when terminal access is available:

✅ **Can validate:**
- Running test commands (`./gradlew test`, `./gradlew build`)
- Checking file existence and content
- Verifying code syntax and structure
- Running linters and formatters
- Checking database schema via SQL queries
- Verifying environment setup (Docker, services)
- Git operations (branch, commit, status checks)
- Building and compiling code
- Inspecting logs and error messages

❌ **Cannot validate:**
- First-time account setup (Google, Firebase, Twilio, SendGrid)
- OAuth flows (Google Sign-In configuration, callbacks)
- App store submissions (Google Play, App Store)
- First-time app launch on physical devices
- Manual UI testing (visual verification, UX flow)
- Online configuration (Firebase Console, AWS Console, GCP Console)
- Payment gateway testing (Stripe, subscription flows)
- Push notification testing (actual device notifications)
- External service integration verification (requires web login)

---

## Validation Methods

### A. Document-Only Validation

**For:** Documentation updates, spec changes, architecture revisions

**Steps:**
1. Review all related documents for consistency
2. Check cross-file references (Plan ↔ Architecture ↔ Spec)
3. Verify links, paths, and formatting
4. Remove placeholders (e.g., "TODO", "TBD")
5. Create validation report: `docs/TEST_REPORTS/{task_id}_VALIDATION.md`

**Evidence:** Quotes, screenshots, timestamps

**Owner:** `AI` (for consistency checks) or `HUMAN` (for subjective review)

---

### B. Command-Driven Validation

**For:** Code changes, infrastructure updates, feature implementation

**Steps:**
1. Run appropriate commands:
   ```bash
   ./gradlew test
   ./gradlew integrationTest
   ./gradlew :composeApp:connectedDebugAndroidTest
   maestro test maestro/
   k6 run k6/load_test.js
   ```
2. Capture logs or reports as evidence
3. Include results in `docs/TEST_REPORTS/{task_id}_VALIDATION.md`
4. Confirm PASS criteria from task plan are met

**Evidence:** Command output, test reports, logs

**Owner:** `AI` (if verifiable through logs) or `HUMAN` (if manual environment required)

---

### C. Hybrid Validation

**For:** Features requiring both automated and manual validation

**Steps:**
1. Perform document validation (Section A)
2. Execute relevant commands (Section B)
3. Human performs manual testing (UI, external services)
4. Merge evidence into one validation report
5. Indicate clearly which parts were validated by AI vs Human
6. Update Progress Ledger and Changelog after both validations pass

**Evidence:** Combined automated + manual evidence

**Owner:** `HYBRID`

---

## Validation Decision Matrix

| Task Type | Terminal Testable? | Validation Owner |
|-----------|-------------------|------------------|
| Backend API endpoints | ✅ Yes | AI |
| Database operations | ✅ Yes | AI |
| Unit/Integration tests | ✅ Yes | AI |
| Build & compilation | ✅ Yes | AI |
| Code structure | ✅ Yes | AI |
| First-time app launch | ❌ No | HUMAN |
| Google OAuth setup | ❌ No | HUMAN |
| Push notification flow | ❌ No | HUMAN |
| Payment processing | ❌ No | HUMAN |
| Visual UI verification | ❌ No | HUMAN |
| Third-party service setup | ❌ No | HUMAN |

---

## Validation Report Template

Use this template for all validation reports:

```markdown
# Task Validation Report: T-XXX

**Task:** [Task Name]  
**Validation Owner:** AI | HUMAN | HYBRID  
**Validated By:** [Name/Agent]  
**Date:** YYYY-MM-DD

---

## Validation Plan

**Pass Criteria:**
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

**Validation Method:** Document-only | Command-driven | Hybrid

---

## Evidence

### AI Validation
[Command outputs, test results, build logs]

### Human Validation
[Screenshots, manual test results, external service confirmations]

---

## Result

**Status:** ✅ PASS | ❌ FAIL

**Issues Found:** [List any issues]

**Follow-up Actions:** [Required next steps]

---

## Sign-off

- AI: [Timestamp]
- Human: [Name, Timestamp]
```

---

## Incremental Validation

**Error-Free Progression Principle:**  
Never accumulate errors across multiple files. Always verify each change immediately.

**Workflow:**
1. Create/Modify File → Verify immediately
2. Fix Errors → Test fix works
3. Move to Next File → Repeat

**Verification Commands:**
```bash
# After modifying Kotlin file
./gradlew compileKotlin

# After adding dependency
./gradlew build

# After creating test
./gradlew test --tests "SpecificTest"

# After database change
psql -U user -d db -c "SELECT 1;"
```

**Red Flags (Stop & Fix):**
- ❌ Compilation errors
- ❌ Missing imports
- ❌ Type mismatches
- ❌ Syntax errors
- ❌ Test failures
- ❌ Broken references

---

## Git Workflow for Validation

### Branch Strategy
Always create a task-specific branch before any changes:

```bash
# Create and switch to task branch
git checkout -b task/{task_id}-{description}

# Examples:
git checkout -b task/T-101-backend-auth
git checkout -b task/T-202-post-creation
```

**Never commit directly to main or develop.**

### Validation Workflow
1. Create branch → Implement → Test locally → Create validation report
2. If all tests pass: Push branch → Create PR → Wait for CI
3. If tests fail: Fix issues → Re-test → Update validation report
4. After PR approved: Merge → Delete branch

---

## Best Practices

1. **Verify Latest Standards:** Check official documentation for latest API versions
2. **Official Examples:** Reference official GitHub repositories for current patterns
3. **Documentation Priority:** Official docs → Official examples → Community resources
4. **Stay Updated:** Regularly check for framework/library updates

**Key Official Documentation:**
- Kotlin: https://kotlinlang.org/docs/
- Ktor: https://ktor.io/docs/
- Compose Multiplatform: https://www.jetbrains.com/lp/compose-multiplatform/
- PostgreSQL: https://www.postgresql.org/docs/
- PostGIS: https://postgis.net/documentation/

---

## Related Documents

- **[TESTING.md](./TESTING.md)** → Testing strategy and guidelines
- **[TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md](../TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md)** → Validation report template
- **[PROMPTS/VIBECODE_SHORT_META_PROMPT.md](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md)** → AI workflow guide
- **[CHECKLISTS/PRE_PUSH_CHECKLIST.md](../CHECKLISTS/PRE_PUSH_CHECKLIST.md)** → Pre-push checklist

