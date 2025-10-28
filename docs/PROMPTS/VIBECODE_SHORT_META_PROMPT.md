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
- Archived task plans can be found in `docs/ARCHIVE/TASK_PLANS/`
  - `evidence_required` (logs, screenshots, test results)
  - `pass_criteria` (specific success conditions)

### 2. Validate First
**Before implementing:**
- Review **[VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)** for validation procedures
- For doc-only tasks: Review consistency, cross-references, formatting
- For code tasks: Run tests, build, verify compilation
- AI validates: File consistency, automated tests, builds
- Human validates: Manual testing, external services, UI/UX
- HYBRID: Both AI and human validation required

### 3. Update Documentation
Sync and fix outdated or conflicting documentation **before** code changes.

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

### 5. Post-Validate & Log
- Run final validation
- **Verify CI passes** (all GitHub Actions jobs must succeed)
- Record evidence (archived test reports in `docs/ARCHIVE/TEST_REPORTS/`)
- Mark PASS/FAIL
- Add follow-up actions if needed
- Update Progress Ledger in `docs/PLANS/NearYou_ID_MVP_Plan.md`
- Update `docs/CORE/CHANGELOG.md`

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
2. **Workflow:** Create/Modify → Verify → Fix → Next file
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

3. **Update Docs:**
   - Update ARCHITECTURE.md with auth flow
   - Update API_DOCUMENTATION.md with auth endpoints

4. **Implement:**
   ```bash
   git checkout -b task/T-101-user-authentication
   # Implement auth service
   ./gradlew :server:test  # Verify after each change
   ```

5. **Post-Validate:**
   - AI: Run tests, verify build
   - Human: Test OAuth flow in browser
   - Create `docs/TEST_REPORTS/T-101_VALIDATION.md`
   - Update Progress Ledger and Changelog

---

## 🧪 Quick Reference: Validation Resources

| Resource | Purpose | Location |
|----------|---------|----------|
| **VALIDATION_GUIDE.md** | Complete validation procedures | [Link](../CORE/VALIDATION_GUIDE.md) |
| **TASK_VALIDATION_TEMPLATE.md** | Validation report template | [Link](../TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md) |
| **TESTING.md** | Testing strategy | [Link](../CORE/TESTING.md) |
| **PERFORMANCE.md** | Performance testing guide | [Link](../CORE/PERFORMANCE.md) |

---

**This is a concise workflow guide. For complete details, see the linked documentation.**
