# Quick Start: Test CI/CD Pipeline

## Prerequisites
✅ All test files committed (already done)  
⚠️ Need to push to GitHub remote

---

## Quick Steps

### 1. Create GitHub Repository
1. Go to https://github.com/new
2. Name: `near-you-id` (or your choice)
3. **Don't** initialize with README
4. Click "Create repository"

### 2. Push Your Code
```bash
# Add remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/near-you-id.git

# Commit the new test guide
git add docs/T-004_SETUP_AND_TEST_GUIDE.md docs/QUICK_START_CI_TEST.md
git commit -m "docs: Add CI/CD testing guide"

# Push to GitHub
git push -u origin main
```

### 3. Watch CI Run
1. Go to your repo on GitHub
2. Click **"Actions"** tab
3. Watch the workflow run automatically
4. All jobs should appear and execute

### 4. Verify Success
Expected results:
- ✅ **Lint**: Passes (may have warnings)
- ✅ **Test Shared Module**: 115/115 tests pass
- ⚠️ **Test Server Module**: May fail (no server tests yet)
- ✅ **Build Android**: APK builds successfully
- ⚠️ **Build iOS**: May have issues (OK, has continue-on-error)
- ✅ **Build Docker**: Image builds successfully
- ⚠️ **Coverage**: May fail (Kover not configured yet)
- ✅ **Summary**: Passes if critical tests pass

### 5. Download Test Reports
1. Click on the workflow run
2. Scroll to "Artifacts"
3. Download `shared-test-reports`
4. Extract and open `index.html` to see test results

---

## What Makes CI Pass?

**Critical Jobs (must pass):**
- Test Shared Module ✅
- Build Android ✅

**Optional Jobs (can fail):**
- Test Server Module (no tests yet)
- Build iOS (may have config issues)
- Coverage (Kover not configured)

**Overall Status:** ✅ PASS if critical jobs succeed

---

## Test Failure Scenario

```bash
# Create failing test
git checkout -b test-failure

cat > shared/src/commonTest/kotlin/domain/model/TestFailure.kt << 'EOF'
package domain.model
import kotlin.test.Test
import kotlin.test.assertTrue

class TestFailure {
    @Test
    fun `intentional failure`() {
        assertTrue(false, "Testing CI failure")
    }
}
EOF

git add .
git commit -m "test: Add failing test"
git push -u origin test-failure
```

**Expected:** CI runs and FAILS ❌

**Cleanup:**
```bash
git checkout main
git branch -D test-failure
git push origin --delete test-failure
```

---

## Quick Troubleshooting

**Workflow doesn't start?**
- Check Actions are enabled: Settings → Actions
- Verify you pushed to `main` or `develop` branch

**Tests fail?**
- Click on failed job
- Expand the failing step
- Read error message

**Need help?**
- See full guide: `docs/T-004_SETUP_AND_TEST_GUIDE.md`

---

## Success Checklist

- [ ] Repository on GitHub
- [ ] CI workflow triggered automatically
- [ ] Test Shared Module: 115/115 tests pass
- [ ] Android APK built successfully
- [ ] Docker image built successfully
- [ ] Test reports downloadable
- [ ] Tested failure scenario

**Time Required:** ~15-20 minutes

---

## Current Status

**Your Repository:**
- Location: `/Users/aditrioka/Source/vibecode/Near You ID`
- Branch: `main`
- Latest commit: `a4285e3 - feat: Add domain model tests and message validation`
- Uncommitted: `docs/T-004_SETUP_AND_TEST_GUIDE.md` (needs commit)

**Next Action:** 
1. Commit the guide files
2. Create GitHub repository
3. Push and watch CI run! 🚀

