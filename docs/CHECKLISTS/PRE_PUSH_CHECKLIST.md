# Pre-Push Checklist for GitHub

Before pushing to GitHub, verify everything is ready:

## ✅ Git Workflow

### 1. Verify You're on the Correct Branch
```bash
git branch --show-current
```
**Expected:** `task/T-XXX-short-name` (NOT main or develop)

**If on main/develop:**
```bash
# Create task branch first
git checkout -b task/T-XXX-short-name
```

### 2. Branch Naming Convention
Format: `task/{task_id}-{short-kebab-case-description}`

**Examples:**
- ✅ `task/T-101-backend-auth-service`
- ✅ `task/T-102-frontend-auth-flows`
- ✅ `task/T-201-postgis-geo-queries`
- ❌ `feature/auth` (too vague)
- ❌ `T-101` (missing description)
- ❌ `main` (never commit directly to main!)

---

## ✅ Code Quality Checks

### 0. Incremental Verification (During Development)
**Did you verify each file immediately after creation/modification?**

If you followed error-free progression:
- [ ] Each file was verified to compile after creation
- [ ] Each test was run immediately after writing
- [ ] No accumulated errors across multiple files

If NOT, run incremental checks now:
```bash
# Check each modified file compiles
./gradlew compileKotlin

# Check specific tests pass
./gradlew test --tests "ModifiedTest*"
```

### 1. Run All Tests Locally
```bash
./gradlew :shared:allTests
```
**Expected:** 115 tests completed, 0 failed ✅

### 2. Build All Modules
```bash
./gradlew build
```
**Expected:** BUILD SUCCESSFUL ✅

### 3. Check for Uncommitted Changes
```bash
git status
```
**Expected:** Clean working tree or only intended changes

---

## ✅ Files to Commit Before Push

Current uncommitted files:
```bash
git status --short
```

**Commit command:**
```bash
git add <files>
git commit -m "feat(scope): description (T-XXX)"
```

**Commit Message Format (Conventional Commits):**
```
feat(scope): description (T-XXX)
fix(scope): description (T-XXX)
docs(scope): description (T-XXX)
test(scope): description (T-XXX)
```

**Examples:**
- `feat(auth): implement backend auth service (T-101)`
- `fix(database): correct timestamp handling in migrations (T-101)`
- `docs(testing): add validation guide (T-101)`
- `test(auth): add OTP verification tests (T-101)`

---

## ✅ Repository Structure Verification

Verify these critical files exist:

### CI/CD Configuration
- [x] `.github/workflows/ci.yml` - GitHub Actions workflow

### Documentation
- [x] `README.md` - Project overview
- [x] `docs/CORE/ARCHITECTURE.md` - Architecture documentation
- [x] `docs/CORE/SPEC.md` - Technical specifications
- [x] `docs/CORE/INFRA.md` - Infrastructure documentation

### Source Code
- [x] `shared/src/commonMain/kotlin/domain/model/` - Domain models
- [x] `shared/src/commonMain/kotlin/domain/validation/` - Validation logic
- [x] `shared/src/commonTest/kotlin/domain/` - Unit tests

### Build Configuration
- [x] `build.gradle.kts` - Root build file
- [x] `shared/build.gradle.kts` - Shared module build
- [x] `server/build.gradle.kts` - Server module build
- [x] `settings.gradle.kts` - Project settings

### Docker
- [x] `Dockerfile` - Server Docker image
- [x] `docker-compose.yml` - Local development setup

---

## ✅ Sensitive Data Check

**IMPORTANT:** Ensure no sensitive data is committed!

Check for:
- [ ] No API keys in code
- [ ] No passwords in configuration files
- [ ] No personal tokens
- [ ] No database credentials (except example/test values)

**Files to review:**
```bash
# Search for potential secrets
grep -r "password" --include="*.kt" --include="*.yml" --include="*.properties" .
grep -r "api_key" --include="*.kt" --include="*.yml" --include="*.properties" .
grep -r "secret" --include="*.kt" --include="*.yml" --include="*.properties" .
```

**Safe patterns:**
- ✅ `POSTGRES_PASSWORD: test_password` (in CI config)
- ✅ `password = "test"` (in test files)
- ❌ Real production credentials

---

## ✅ .gitignore Verification

Ensure these are ignored:
```bash
# Check .gitignore includes:
cat .gitignore | grep -E "(build|\.gradle|\.idea|local\.properties)"
```

**Should be ignored:**
- Build outputs (`build/`, `*.apk`, `*.ipa`)
- IDE files (`.idea/`, `*.iml`)
- Local config (`local.properties`)
- Gradle cache (`.gradle/`)

---

## ✅ Final Pre-Push Commands

Run these in order:

```bash
# 1. Ensure you're on task branch (NOT main)
git branch --show-current
# Expected: task/T-XXX-description

# 2. Pull latest main (if working with team)
git fetch origin main
git rebase origin/main  # Or merge if preferred
# Expected: Up to date or rebase successful

# 3. Run tests one more time
./gradlew :shared:allTests --console=plain
# Expected: All tests pass

# 4. Check git status
git status
# Expected: Clean or only intended changes

# 5. View commit history
git log --oneline -5
# Verify commits look good

# 6. Ready to push!
```

---

## ✅ GitHub Repository Setup

Before first push, create GitHub repository:

1. **Go to:** https://github.com/new
2. **Repository name:** `near-you-id` (or your choice)
3. **Description:** "Near You ID - Location-based social platform MVP"
4. **Visibility:** Private or Public (your choice)
5. **Initialize:** ❌ Do NOT check any boxes
6. **Click:** "Create repository"

**Copy the remote URL shown** (you'll need it for next step)

---

## ✅ Push Commands

```bash
# Add remote (first time only, replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Verify remote
git remote -v
# Expected: origin https://github.com/YOUR_USERNAME/REPO_NAME.git (fetch)
#           origin https://github.com/YOUR_USERNAME/REPO_NAME.git (push)

# Push task branch to GitHub
git push -u origin task/T-XXX-description

# Expected output:
# Enumerating objects: XXX, done.
# Counting objects: 100% (XXX/XXX), done.
# ...
# To https://github.com/YOUR_USERNAME/REPO_NAME.git
#  * [new branch]      task/T-XXX-description -> task/T-XXX-description
```

---

## ✅ Post-Push Verification

After pushing:

1. **Visit repository:** `https://github.com/YOUR_USERNAME/REPO_NAME`
2. **Check branches:** Verify task branch is present
3. **Create Pull Request:** Click "Compare & pull request"
4. **Fill PR details:**
   - Title: Same as commit message (e.g., "feat(auth): implement backend auth service (T-101)")
   - Description: Link to task plan and validation report
   - Reviewers: Assign if working with team
5. **Check Actions tab:** CI should start automatically
6. **Watch workflow:** Monitor the CI pipeline execution

**Expected CI Results:**
- Lint: ✅ Pass (may have warnings)
- Test Shared: ✅ Pass (all tests)
- Test Server: ✅ Pass (if tests exist)
- Build Android: ✅ Pass
- Build iOS: ⚠️ May fail (continue-on-error)
- Build Docker: ✅ Pass
- Coverage: ✅ Pass
- Summary: ✅ Pass (if critical jobs pass)

---

## 🚀 Ready to Push?

**Checklist:**
- [ ] On task branch (NOT main/develop)
- [ ] All tests pass locally
- [ ] Build succeeds locally
- [ ] No compilation errors
- [ ] Documentation files committed
- [ ] No sensitive data in code
- [ ] GitHub repository created (first time)
- [ ] Remote URL configured (first time)
- [ ] Followed error-free progression during development

**If all checked, you're ready to push!**

```bash
git push -u origin task/T-XXX-description
```

Then create a Pull Request and watch the CI pipeline! 🎉

---

## 📞 Need Help?

- **Full guide:** `docs/TEST_REPORTS/T-004_SETUP_AND_TEST_GUIDE.md`
- **Quick start:** `docs/PLANS/QUICK_START.md`
- **Git workflow:** `docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md`
