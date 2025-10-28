# T-004: Setup CI/CD Pipeline - Complete Testing Guide

**Date:** 2025-10-16  
**Status:** Ready to Test  
**Prerequisites:** Repository must be pushed to GitHub

---

## Overview

This guide will walk you through:
1. Creating a GitHub repository
2. Pushing your local code to GitHub
3. Testing the CI/CD pipeline
4. Verifying all jobs pass
5. Testing failure scenarios

---

## Part 1: Push Repository to GitHub

### Step 1: Create a GitHub Repository

1. Go to [GitHub](https://github.com) and log in
2. Click the **"+"** icon in the top right → **"New repository"**
3. Fill in the details:
   - **Repository name:** `near-you-id` (or your preferred name)
   - **Description:** "Near You ID - Location-based social platform MVP"
   - **Visibility:** Choose **Private** or **Public**
   - **DO NOT** initialize with README, .gitignore, or license (we already have these)
4. Click **"Create repository"**

### Step 2: Add Remote and Push Code

```bash
# Navigate to your project directory
cd "/Users/aditrioka/Source/vibecode/Near You ID"

# Check current git status
git status

# Add all your test files (if not already committed)
git add .
git commit -m "Add comprehensive tests for T-003 domain models"

# Add the GitHub remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/near-you-id.git

# Verify remote was added
git remote -v

# Push to GitHub
git push -u origin main
```

**Alternative using SSH:**
```bash
git remote add origin git@github.com:YOUR_USERNAME/near-you-id.git
git push -u origin main
```

### Step 3: Verify Upload

1. Go to your GitHub repository URL: `https://github.com/YOUR_USERNAME/near-you-id`
2. Verify all files are present
3. Check that `.github/workflows/ci.yml` exists

---

## Part 2: Test CI/CD Pipeline

### Step 4: Trigger CI Pipeline Automatically

The CI pipeline will automatically trigger when you push code. Check the status:

1. Go to your GitHub repository
2. Click on the **"Actions"** tab
3. You should see a workflow run for your recent push
4. Click on the workflow run to see details

### Step 5: Monitor Pipeline Execution

Watch the following jobs execute:

#### ✅ Job 1: Lint
- **Purpose:** Check code style with ktlint
- **Expected:** May show warnings (continue-on-error: true)
- **Duration:** ~1-2 minutes

#### ✅ Job 2: Test Shared Module
- **Purpose:** Run all 115 tests for shared domain models
- **Expected:** All tests should pass
- **Duration:** ~2-3 minutes
- **Artifacts:** Test results and reports

#### ✅ Job 3: Test Server Module
- **Purpose:** Run server tests with PostgreSQL and Redis
- **Expected:** May fail if no server tests exist yet (that's OK)
- **Duration:** ~2-3 minutes
- **Services:** PostgreSQL 15 + PostGIS, Redis 7.0

#### ✅ Job 4: Build Android
- **Purpose:** Build Android Debug APK
- **Expected:** Should succeed
- **Duration:** ~3-5 minutes
- **Artifacts:** Debug APK file

#### ✅ Job 5: Build iOS
- **Purpose:** Build iOS framework and app
- **Expected:** May have issues (continue-on-error: true)
- **Duration:** ~5-7 minutes
- **Runner:** macOS

#### ✅ Job 6: Build Docker
- **Purpose:** Build Docker image for server
- **Expected:** Should succeed
- **Duration:** ~3-5 minutes
- **Cache:** Uses GitHub Actions cache

#### ✅ Job 7: Coverage
- **Purpose:** Generate code coverage report
- **Expected:** May fail if Kover not configured (continue-on-error: true)
- **Duration:** ~2-3 minutes

#### ✅ Job 8: Summary
- **Purpose:** Check overall build status
- **Expected:** Should succeed if test-shared and test-server pass
- **Duration:** ~10 seconds

---

## Part 3: Manual Testing

### Step 6: Trigger CI Manually

Test the `workflow_dispatch` trigger:

1. Go to **Actions** tab
2. Click on **"CI"** workflow in the left sidebar
3. Click **"Run workflow"** button
4. Select branch: `main`
5. Click **"Run workflow"**
6. Watch the workflow execute

### Step 7: Download and Verify Artifacts

After the workflow completes:

1. Click on the completed workflow run
2. Scroll down to **"Artifacts"** section
3. Download available artifacts:
   - `shared-test-results` - JUnit XML test results
   - `shared-test-reports` - HTML test reports
   - `android-debug-apk` - Debug APK (if build succeeded)
   - `lint-results` - Ktlint reports

4. Extract and review:
   ```bash
   # Extract test reports
   unzip shared-test-reports.zip
   
   # Open HTML report in browser
   open shared-test-reports/jvmTest/index.html
   ```

---

## Part 4: Test Failure Scenarios

### Step 8: Test Failing Test Scenario

Create a branch with a failing test:

```bash
# Create a new branch
git checkout -b test-ci-failure

# Create a failing test
cat > shared/src/commonTest/kotlin/domain/model/FailingTest.kt << 'EOF'
package domain.model

import kotlin.test.Test
import kotlin.test.assertTrue

class FailingTest {
    @Test
    fun `this test should fail`() {
        assertTrue(false, "Intentional failure to test CI")
    }
}
EOF

# Commit and push
git add .
git commit -m "Test: Add intentionally failing test"
git push -u origin test-ci-failure
```

**Expected Result:**
- CI should run automatically
- `test-shared` job should FAIL
- `summary` job should FAIL
- You should see the failure in the Actions tab

### Step 9: Test Lint Error Scenario

```bash
# Create a file with lint errors
cat > shared/src/commonMain/kotlin/domain/model/BadCode.kt << 'EOF'
package domain.model

// This has intentional lint errors
class badClassName {  // Should be PascalCase
    fun bad_function_name() {  // Should be camelCase
        val x=1+2  // Missing spaces
    }
}
EOF

# Commit and push
git add .
git commit -m "Test: Add code with lint errors"
git push
```

**Expected Result:**
- `lint` job may show warnings/errors
- But continues (continue-on-error: true)

### Step 10: Clean Up Test Branches

```bash
# Switch back to main
git checkout main

# Delete test branch locally
git branch -D test-ci-failure

# Delete test branch remotely
git push origin --delete test-ci-failure
```

---

## Part 5: Verify Definition of Done

### ✅ CI pipeline runs on every push/PR
**Test:**
```bash
# Make a small change
echo "# CI Test" >> README.md
git add README.md
git commit -m "Test: Trigger CI"
git push
```
**Verify:** Check Actions tab - new workflow should start automatically

### ✅ All tests execute successfully
**Verify:** 
- Go to Actions → Latest workflow run
- Check `test-shared` job
- Should show: "115 tests completed, 0 failed"

### ✅ Code coverage report generated
**Verify:**
- Check `coverage` job in workflow
- Look for "Generate coverage report" step
- May need to configure Kover plugin first

### ✅ Docker image builds successfully
**Verify:**
- Check `build-docker` job
- Should complete without errors
- Image tagged with commit SHA

---

## Part 6: Create Pull Request Test

### Step 11: Test PR Workflow

```bash
# Create a feature branch
git checkout -b feature/test-pr

# Make a change
echo "# Test PR" >> docs/T-004_TEST.md
git add .
git commit -m "Test: PR workflow"
git push -u origin feature/test-pr
```

**On GitHub:**
1. Go to your repository
2. Click **"Pull requests"** → **"New pull request"**
3. Base: `main` ← Compare: `feature/test-pr`
4. Click **"Create pull request"**
5. Fill in title and description
6. Click **"Create pull request"**

**Expected Result:**
- CI should run automatically for the PR
- You'll see status checks at the bottom of the PR
- All jobs should be listed with their status

---

## Part 7: Advanced Testing (Optional)

### Step 12: Test Branch Protection Rules

1. Go to **Settings** → **Branches**
2. Click **"Add rule"** under "Branch protection rules"
3. Branch name pattern: `main`
4. Enable:
   - ✅ Require a pull request before merging
   - ✅ Require status checks to pass before merging
   - ✅ Select status checks: `Test Shared Module`, `Test Server Module`
5. Click **"Create"**

**Test:** Try to push directly to main - should be blocked

### Step 13: Monitor Resource Usage

Check GitHub Actions usage:
1. Go to **Settings** → **Billing and plans** → **Plans and usage**
2. Click **"Actions"** tab
3. View minutes used (free tier: 2,000 minutes/month for private repos)

---

## Troubleshooting

### Issue: Workflow doesn't trigger
**Solution:**
- Ensure `.github/workflows/ci.yml` is in the repository
- Check that you pushed to `main` or `develop` branch
- Verify Actions are enabled: Settings → Actions → General

### Issue: Test job fails with "No tests found"
**Solution:**
- Verify test files are in correct location
- Check that tests are committed and pushed
- Review job logs for specific error

### Issue: Docker build fails
**Solution:**
- Check `Dockerfile` exists and is valid
- Review Docker build logs in the job
- May need to add `.dockerignore` file

### Issue: iOS build fails
**Solution:**
- This is expected if iOS app isn't fully configured
- Job has `continue-on-error: true`
- Won't fail the overall pipeline

### Issue: Coverage job fails
**Solution:**
- Need to add Kover plugin to `build.gradle.kts`
- Job has `continue-on-error: true`
- Won't fail the overall pipeline

---

## Success Criteria Checklist

- [ ] Repository pushed to GitHub
- [ ] CI workflow visible in Actions tab
- [ ] Workflow triggers on push to main
- [ ] Workflow triggers on pull request
- [ ] `test-shared` job passes (115/115 tests)
- [ ] `build-android` job passes
- [ ] `build-docker` job passes
- [ ] Artifacts are generated and downloadable
- [ ] Failing test causes CI to fail (tested)
- [ ] Summary job reports overall status

---

## Next Steps

After successful CI/CD setup:

1. **Configure Kover for coverage:**
   ```bash
   # Add to build.gradle.kts
   plugins {
       id("org.jetbrains.kotlinx.kover") version "0.7.5"
   }
   ```

2. **Add deployment job** (when ready):
   - Deploy to staging environment
   - Manual approval for production

3. **Set up notifications:**
   - Slack/Discord integration
   - Email notifications for failures

4. **Document in INFRA.md:**
   - CI/CD process
   - How to interpret results
   - Troubleshooting guide

---

## Summary

**T-004 Testing Complete When:**
✅ Code pushed to GitHub  
✅ CI runs automatically on push/PR  
✅ All critical jobs pass (test-shared, build-android, build-docker)  
✅ Failure scenarios tested and verified  
✅ Artifacts downloadable  
✅ Process documented  

**Estimated Time:** 30-45 minutes for complete testing

