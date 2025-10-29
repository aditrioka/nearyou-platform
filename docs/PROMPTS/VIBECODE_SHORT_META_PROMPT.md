# ⚡ VibeCode AI Workflow

**Concise execution guide for AI-assisted development**

Use **[PROJECT_MAP.md](../CORE/PROJECT_MAP.md)** as the main index to locate all required files.

---

## 🧩 Execution Flow

### 1. Plan
Create or update task plan with:
- Scope, dependencies, affected modules
- Required documentation updates
- **Validation Plan:**
    - `validation_owner` (AI/HUMAN/HYBRID)
    - `ai_capability` (what AI can verify)
    - `human_prereq` (what requires manual verification)
    - `evidence_required` (logs, screenshots, test results)
    - `pass_criteria` (specific success conditions)
- Archived task plans can be found in `docs/ARCHIVE/TASK_PLANS/`

### 2. Validate First
**Before implementing:**
- Review **[VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)** for validation procedures
- For doc-only tasks: Review consistency, cross-references, formatting
- For code tasks: Run tests, build, verify compilation
- AI validates: File consistency, automated tests, builds
- Human validates: Manual testing, external services, UI/UX
- HYBRID: Both AI and human validation required

### 3. Update Documentation

#### Before Implementation
Sync and fix outdated or conflicting documentation **before** code changes:
- Review affected documentation files
- Fix any inconsistencies or outdated information
- Update cross-references

#### After Implementation (CRITICAL)
**Whenever you modify code, requirements, or technologies, you MUST update:**

1. **Architecture Documentation** (`docs/CORE/ARCHITECTURE.md`):
    - If adding/modifying components, services, or modules
    - If changing data flow or system interactions
    - If introducing new patterns or technologies

2. **API Documentation** (`docs/CORE/API_DOCUMENTATION.md`):
    - If creating/modifying endpoints
    - If changing request/response formats
    - If updating authentication or error codes

3. **Specification** (`docs/CORE/SPEC.md`):
    - If requirements change
    - If features are added, modified, or removed
    - If business logic changes

4. **Infrastructure** (`docs/CORE/INFRA.md`):
    - If adding/changing databases, caching, or services
    - If modifying deployment configuration
    - If updating environment variables

5. **Testing Documentation** (`docs/CORE/TESTING.md`):
    - If adding new test types or strategies
    - If changing testing tools or frameworks

6. **Changelog** (`docs/CORE/CHANGELOG.md`):
    - ALWAYS add entry for significant changes
    - Format: `| CL-XXX | YYYY-MM-DD | CATEGORY | Description | Related Task |`

**When to update documentation:**
- ✅ After completing implementation
- ✅ After requirements shift
- ✅ After technology changes
- ✅ After architectural decisions
- ✅ When adding new features
- ✅ When deprecating features

**Documentation sync checklist:**
- [ ] Code matches documentation
- [ ] Examples are accurate and tested
- [ ] Version numbers are current
- [ ] Cross-references are valid
- [ ] No conflicting information

### 4. Implement
- **Create task branch:** `git checkout -b task/T-XXX-description`
- **Never commit to main or develop directly**
- **AI must not push to remote** (only prepare commits locally)
- Follow latest industry best practices:
    - Clean architecture
    - Separation of concerns
    - Modular, testable code
- Use `context7` for latest official documentation
- Use `websearch` for current best practices
- **Verify each file immediately after changes** (incremental validation)
- Fix errors before proceeding to next file

#### Test Creation & Maintenance (CRITICAL)
**For every file created or modified, you MUST:**

1. **Create or update corresponding test file(s)**:
    - Unit tests for new functions/classes
    - Integration tests for API endpoints
    - Follow project testing conventions (see `docs/CORE/TESTING.md`)

2. **Run tests immediately after changes**:
   ```bash
   # For backend (Kotlin/Ktor)
   ./gradlew :server:test
   
   # For shared module
   ./gradlew :shared:test
   
   # For specific test class
   ./gradlew test --tests "ClassName"
   ```

3. **If tests fail:**
    - Analyze the failure output carefully
    - Fix the code or update the test as needed
    - Re-run tests until all pass
    - Document any test changes in commit message

4. **Test coverage requirements:**
    - New files: Minimum 80% coverage
    - Modified files: Maintain or improve existing coverage
    - Run coverage report: `./gradlew jacocoTestReport`

5. **Test documentation:**
    - Add inline comments explaining complex test scenarios
    - Update test README if adding new test patterns
    - Document any test-specific setup requirements

### 5. Post-Validate & Log

#### Run Final Validation
- **Execute full test suite:**
  ```bash
  # Run all tests
  ./gradlew test
  
  # Verify builds
  ./gradlew build
  
  # Check test coverage
  ./gradlew jacocoTestReport
  ```
- Verify all tests pass (100% pass rate)
- Check test coverage meets requirements (≥80% for new code)
- Verify no compilation errors or warnings
- **Verify CI passes** (all GitHub Actions jobs must succeed)

#### Create Testing Documentation
**Create comprehensive test report** in `docs/ARCHIVE/TEST_REPORTS/T-XXX_VALIDATION.md`:

```markdown
# Task T-XXX Validation Report

## Task Summary
- **Task ID:** T-XXX
- **Task Title:** [Task title]
- **Date:** YYYY-MM-DD
- **Validator:** AI/HUMAN/HYBRID
- **Status:** PASS/FAIL

## Tests Executed

### Unit Tests
- **Command:** `./gradlew :module:test`
- **Result:** X/Y tests passed
- **Coverage:** XX%
- **Output:**
  ```
[Paste test output]
  ```

### Integration Tests
- **Command:** `./gradlew :module:integrationTest`
- **Result:** X/Y tests passed
- **Output:**
  ```
[Paste test output]
  ```

### Manual Tests (if applicable)
1. Test scenario description
   - Steps: [Detailed steps]
   - Expected: [Expected result]
   - Actual: [Actual result]
   - Status: PASS/FAIL

## Build Verification
- **Command:** `./gradlew build`
- **Status:** SUCCESS/FAILED
- **Output:**
  ```
[Paste relevant build output]
  ```

## Code Quality Checks
- [ ] No compilation errors
- [ ] No lint warnings
- [ ] Code follows project conventions
- [ ] All imports resolved
- [ ] No deprecated API usage

## Evidence
- Screenshots: [List any screenshots]
- Logs: [Link to relevant logs]
- Test reports: [Link to detailed reports]

## Issues Found & Resolved
1. Issue description
   - Root cause: [Explanation]
   - Fix applied: [How it was fixed]
   - Verification: [How fix was verified]

## Follow-up Actions
- [ ] Action item 1
- [ ] Action item 2

## Final Status
**Overall Result:** PASS/FAIL

**Notes:** [Any additional notes or observations]
```

#### Update Progress Ledger
**Update Progress Ledger** in `docs/PLANS/NearYou_ID_MVP_Plan.md`:
- Change task status: `IN_PROGRESS` → `DONE`
- Add completion date in the `Completed` column (format: YYYY-MM-DD)
- Add notes about implementation (deviations, extra features, issues resolved)
- **Example format:**
  ```
  | PL-XXX | T-XXX | Task Description | DONE | 2025-10-29 | 2025-10-29 | Completed successfully. Added extra validation for edge cases. |
  ```

#### Update Changelog
**Update Changelog** in `docs/CORE/CHANGELOG.md`:
- Add entry for task completion
- Include any architectural changes or decisions
- Document any deviations from the plan
- **Example format:**
  ```
  | CL-XXX | 2025-10-29 | TASK_COMPLETE | Completed T-XXX: [Task description]. Added [key changes]. | T-XXX |
  ```

---

## 🔍 Validation Decision Matrix

| Task Type | Validation Owner | Rationale |
|-----------|------------------|-----------|
| Backend API | AI | Can test via curl/http tools |
| Database ops | AI | Can verify via SQL queries |
| Unit/Integration tests | AI | Can run test commands |
| Build & compilation | AI | Can run build commands |
| First-time app launch | HUMAN | Requires device/emulator UI |
| OAuth setup | HUMAN | Requires web console access |
| Push notifications | HUMAN | Requires physical device |
| UI/UX verification | HUMAN | Requires human eyes |
| Complex features | HYBRID | Both automated + manual |

**For complete validation procedures, see [VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)**

---

## 💡 Best Practices

### Code Quality
1. **Verify latest standards** → Check official docs for current API versions
2. **Use official examples** → Reference official GitHub repos for patterns
3. **Documentation priority** → Official docs → Official examples → Community
4. **Stay updated** → Regularly check for framework/library updates

### Incremental Validation
1. **Never accumulate errors** → Verify each change immediately
2. **Workflow:** Create/Modify → Test → Verify → Fix → Next file
3. **Red flags (stop & fix):**
    - ❌ Compilation errors
    - ❌ Missing imports
    - ❌ Type mismatches
    - ❌ Test failures
    - ❌ Broken references

### Git Workflow
1. **Always create task branch** → `git checkout -b task/T-XXX-description`
2. **Never commit to main/develop**
3. **Meaningful commit messages** → `T-XXX: Description of change`
4. **No sensitive data** → API keys, passwords, secrets
5. **AI prepares commits** → Human reviews and pushes

---

## 📚 Key Documentation

- **[PROJECT_MAP.md](../CORE/PROJECT_MAP.md)** → Complete documentation index
- **[ARCHITECTURE.md](../CORE/ARCHITECTURE.md)** → System design
- **[SPEC.md](../CORE/SPEC.md)** → Product specification
- **[VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)** → Validation procedures (detailed)
- **[TESTING.md](../CORE/TESTING.md)** → Testing strategy
- **[INFRA.md](../CORE/INFRA.md)** → Infrastructure setup (includes database guide)
- **[PERFORMANCE.md](../CORE/PERFORMANCE.md)** → Performance testing and optimization
- **[PRE_PUSH_CHECKLIST.md](../GUIDES/PRE_PUSH_CHECKLIST.md)** → Pre-push verification
- **[API_DOCUMENTATION.md](../CORE/API_DOCUMENTATION.md)** → API reference
- **[DESIGN_SYSTEM.md](../CORE/DESIGN_SYSTEM.md)** → UI components and patterns
- **[NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md)** → MVP execution plan

---

## 🎯 Example Task Execution

**Task: T-101 - Implement User Authentication**

1. **Plan:**
    - Create task plan document
    - Define validation_owner = HYBRID (AI for code, HUMAN for OAuth setup)

2. **Validate First:**
    - Review [VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)
    - AI: Can verify code structure, tests, build
    - Human: Must configure Google OAuth console

3. **Update Docs (Before):**
    - Update ARCHITECTURE.md with auth flow
    - Update API_DOCUMENTATION.md with auth endpoints

4. **Implement:**
   ```bash
   git checkout -b task/T-101-user-authentication
   # Implement auth service
   # Create AuthService.kt
   # Create AuthServiceTest.kt (NEW)
   ./gradlew :server:test --tests "AuthServiceTest"  # Verify immediately
   # Implement auth routes
   # Create AuthRoutesTest.kt (NEW)
   ./gradlew :server:test --tests "AuthRoutesTest"  # Verify immediately
   ```

5. **Update Docs (After):**
    - Update SPEC.md with authentication requirements
    - Update CHANGELOG.md with implementation details

6. **Post-Validate:**
    - AI: Run all tests (`./gradlew test`), verify build, check coverage
    - Human: Test OAuth flow in browser
    - Create `docs/ARCHIVE/TEST_REPORTS/T-101_VALIDATION.md` with detailed test results
    - Update Progress Ledger: Change status to DONE, add completion date
    - Update Changelog with task completion

---

## 🧪 Quick Reference: Validation Resources

| Resource | Purpose | Location |
|----------|---------|----------|
| **VALIDATION_GUIDE.md** | Complete validation procedures | [Link](../CORE/VALIDATION_GUIDE.md) |
| **TASK_VALIDATION_TEMPLATE.md** | Validation report template | [Link](../TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md) |
| **TESTING.md** | Testing strategy | [Link](../CORE/TESTING.md) |
| **PERFORMANCE.md** | Performance testing guide | [Link](../CORE/PERFORMANCE.md) |

---

## ✅ Task Completion Checklist

Before marking a task as DONE, verify:

- [ ] All code changes implemented
- [ ] Tests created/updated for all changes
- [ ] All tests pass (100%)
- [ ] Test coverage meets requirements (≥80%)
- [ ] Build succeeds with no errors/warnings
- [ ] CI/CD pipeline passes
- [ ] Documentation updated (Architecture, API, Spec, etc.)
- [ ] Testing documentation created (`T-XXX_VALIDATION.md`)
- [ ] Progress Ledger updated (status → DONE, completion date added)
- [ ] Changelog updated
- [ ] Code committed to task branch
- [ ] No compilation errors
- [ ] No deprecated API usage

---

**This is a concise workflow guide. For complete details, see the linked documentation.**
