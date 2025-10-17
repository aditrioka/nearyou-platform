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

## Continuous Improvement

1. **Monitor Coverage:** Track coverage trends over time
2. **Review Flaky Tests:** Identify and fix unstable tests
3. **Performance Benchmarks:** Track performance metrics
4. **Test Maintenance:** Regularly update and refactor tests
5. **Team Training:** Ensure team follows testing best practices

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
