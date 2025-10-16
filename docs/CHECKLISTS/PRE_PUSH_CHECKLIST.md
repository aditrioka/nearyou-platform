# Pre-Push Checklist for GitHub

Before pushing to GitHub, verify everything is ready:

## ✅ Code Quality Checks

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

**Should commit:**
- [x] `docs/T-004_SETUP_AND_TEST_GUIDE.md`
- [x] `docs/QUICK_START_CI_TEST.md`
- [x] `PRE_PUSH_CHECKLIST.md`

**Commit command:**
```bash
git add docs/T-004_SETUP_AND_TEST_GUIDE.md docs/QUICK_START_CI_TEST.md PRE_PUSH_CHECKLIST.md
git commit -m "docs: Add CI/CD testing guides and pre-push checklist"
```

---

## ✅ Repository Structure Verification

Verify these critical files exist:

### CI/CD Configuration
- [x] `.github/workflows/ci.yml` - GitHub Actions workflow

### Documentation
- [x] `README.md` - Project overview
- [x] `docs/ARCHITECTURE.md` - Architecture documentation
- [x] `docs/SPEC.md` - Technical specifications
- [x] `docs/INFRA.md` - Infrastructure documentation
- [x] `docs/T-003_TEST_REPORT.md` - Test report for domain models
- [x] `docs/T-004_SETUP_AND_TEST_GUIDE.md` - CI/CD testing guide

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
# 1. Ensure you're on main branch
git branch --show-current
# Expected: main

# 2. Pull latest changes (if working with team)
git pull origin main
# Expected: Already up to date (or merge successful)

# 3. Run tests one more time
./gradlew :shared:allTests --console=plain
# Expected: 115 tests completed, 0 failed

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

Before pushing, create GitHub repository:

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
# Add remote (replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Verify remote
git remote -v
# Expected: origin https://github.com/YOUR_USERNAME/REPO_NAME.git (fetch)
#           origin https://github.com/YOUR_USERNAME/REPO_NAME.git (push)

# Push to GitHub
git push -u origin main

# Expected output:
# Enumerating objects: XXX, done.
# Counting objects: 100% (XXX/XXX), done.
# ...
# To https://github.com/YOUR_USERNAME/REPO_NAME.git
#  * [new branch]      main -> main
```

---

## ✅ Post-Push Verification

After pushing:

1. **Visit repository:** `https://github.com/YOUR_USERNAME/REPO_NAME`
2. **Check files:** Verify all files are present
3. **Check Actions tab:** CI should start automatically
4. **Watch workflow:** Monitor the CI pipeline execution

**Expected CI Results:**
- Lint: ✅ Pass (may have warnings)
- Test Shared: ✅ Pass (115/115 tests)
- Test Server: ⚠️ May fail (no server tests yet)
- Build Android: ✅ Pass
- Build iOS: ⚠️ May fail (continue-on-error)
- Build Docker: ✅ Pass
- Coverage: ⚠️ May fail (Kover not configured)
- Summary: ✅ Pass (if critical jobs pass)

---

## 🚀 Ready to Push?

**Checklist:**
- [ ] All tests pass locally (115/115)
- [ ] Build succeeds locally
- [ ] Documentation files committed
- [ ] No sensitive data in code
- [ ] GitHub repository created
- [ ] Remote URL ready

**If all checked, you're ready to push!**

```bash
git push -u origin main
```

Then watch the magic happen in the Actions tab! 🎉

---

## 📞 Need Help?

- **Full guide:** `docs/T-004_SETUP_AND_TEST_GUIDE.md`
- **Quick start:** `docs/QUICK_START_CI_TEST.md`
- **Test report:** `docs/T-003_TEST_REPORT.md`

