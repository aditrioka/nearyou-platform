# Pre-Push Checklist

**Quick verification before pushing code**

---

## ✅ Code Quality

- [ ] Code compiles without errors
  ```bash
  ./gradlew build
  ```

- [ ] All tests pass
  ```bash
  ./gradlew test
  ```

- [ ] No linting errors
  ```bash
  ./gradlew ktlintCheck
  ```

- [ ] Code formatted correctly
  ```bash
  ./gradlew ktlintFormat
  ```

---

## ✅ Documentation

- [ ] Updated relevant documentation (if applicable)
- [ ] Added/updated code comments for complex logic
- [ ] Updated API documentation (if API changed)
- [ ] Cross-references are correct

---

## ✅ Testing

- [ ] Added tests for new features
- [ ] Updated tests for modified features
- [ ] Integration tests pass (if applicable)
  ```bash
  ./gradlew integrationTest
  ```

---

## ✅ Continuous Integration

- [ ] All CI jobs pass on GitHub Actions
  - Verify at: https://github.com/aditrioka/nearyou-platform/actions
  - Required jobs: Lint, Test Shared, Test Server, Build Android, Build iOS, Build Docker
- [ ] No failing workflows on current branch

---

## ✅ Git

- [ ] On correct branch (not `main` or `develop`)
  ```bash
  git branch --show-current
  ```

- [ ] Meaningful commit messages
- [ ] No sensitive data in commits (API keys, passwords)
- [ ] No large binary files added

---

## ✅ Dependencies

- [ ] No unnecessary dependencies added
- [ ] Dependencies added via package manager (not manual edit)
- [ ] Lock files updated (if dependencies changed)

---

## ✅ Validation

- [ ] Validation plan defined (AI/HUMAN/HYBRID)
- [ ] Validation report created (if task complete)
- [ ] See [VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md) for details

---

## Quick Commands

```bash
# Full pre-push check
./gradlew clean build test ktlintCheck

# Check current branch
git branch --show-current

# View uncommitted changes
git status

# View commit history
git log --oneline -5
```

---

## Related Documents

- **[VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)** → Validation procedures
- **[TESTING.md](../CORE/TESTING.md)** → Testing strategy
- **[ARCHITECTURE.md](../CORE/ARCHITECTURE.md)** → System architecture
