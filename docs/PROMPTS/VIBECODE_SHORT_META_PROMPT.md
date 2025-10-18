# ⚡ VibeCode Short Meta Prompt (Full Execution Discipline)

Use `docs/CORE/PROJECT_MAP.md` as the main reference to locate all project files.

---

## 🎯 Development Standards

### Documentation & Best Practices
- **Always** use the latest industry best practices as reference
- **Always** consult the most recent official documentation:
  - Kotlin: https://kotlinlang.org/docs/
  - Ktor: https://ktor.io/docs/
  - Compose Multiplatform: https://www.jetbrains.com/lp/compose-multiplatform/
  - PostgreSQL: https://www.postgresql.org/docs/
  - PostGIS: https://postgis.net/documentation/
- **Priority**: Official docs > GitHub official examples > Community guides
- **When in doubt**: Check the official GitHub repositories for latest examples

---

## 🧩 Execution Flow
Always follow this flow for every task:

1. **Plan & Branch First**  
   - **Create a new branch**: `git checkout -b task/{task_id}-{short-name}`
     - Example: `git checkout -b task/T-102-frontend-auth-flows`
     - **NEVER** commit directly to `main` or `develop`
   - Create or update a short task execution plan at  
     `docs/TASK_PLANS/{task_id}_{task_name}.md`.  
   - The plan must include:
     - Scope, dependencies, affected docs, expected output.
     - **Validation Plan**:
       - validation_owner → `AI`, `HUMAN`, or `HYBRID`
       - ai_capability → what AI can/cannot validate
       - human_prereq → what human must do (access, credentials, etc.)
       - evidence_required → screenshots, quotes, or paths
       - pass_criteria → exact checks to mark PASS

2. **Validation First (Before Any Implementation)**
   - Determine validation_owner based on task type:
     - **AI**: If task is fully testable via terminal (build, tests, file verification)
     - **HUMAN**: If task requires manual steps (account setup, app launch, web config)
     - **HYBRID**: If combination needed (AI tests code, human tests runtime/UI)
   - **AI Responsibility**: If AI has terminal access, handle all terminal-testable validations:
     - Run test commands, verify builds, check files, run SQL queries, inspect logs
   - **Human Responsibility**: Handle steps that cannot be done via terminal:
     - Account setup (Firebase, OAuth providers, payment gateways)
     - First-time app launch and UI verification
     - Online configuration (web consoles, external services)
     - Manual testing flows that require human judgment
   - If document-only task: Review docs end-to-end using Validation Template
   - If code task: Use commands from `docs/CORE/TESTING.md → How to Validate Changes`
   - Record validation approach in task plan's Validation Plan section

3. **Document Updates**
   - Update all outdated documentation before implementing any code changes.
   - Sync with architecture, specs, and plans.
   - Ensure no doc contradicts the task output.

4. **Implementation (Error-Free Progression)**
   - After validation and doc updates are complete, implement the change.
   - Generate or modify files as defined in the task plan.
   - **Critical: Error-Free Progression Rule**
     - After creating/modifying each file, immediately verify it compiles/works
     - Fix any errors in the current file BEFORE proceeding to the next file
     - Never accumulate errors across multiple files
     - If compilation/test fails: STOP, fix the issue, verify fix, then continue
   - Use incremental verification:
     ```bash
     # After each file modification
     ./gradlew compileKotlin  # Verify compilation
     ./gradlew build          # If adding new dependencies
     git status              # Verify changes as expected
     ```

5. **Post-Validation & Logging**
   - Run a final validation pass.
   - Record evidence and results in `docs/TEST_REPORTS/{task_id}_VALIDATION.md`.
   - Mark the task PASS/FAIL and include follow-up actions if needed.
   - Update Progress Ledger (PL) and Changelog (CL) in  
     `docs/PLANS/NearYou_ID_MVP_Plan.md`, linking the validation report.

---

## 🧪 Validation Reference
- Validation Template → `docs/TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md`
- Test & Validation Commands → `docs/CORE/TESTING.md → How to Validate Changes`
- Validation Reports → `docs/TEST_REPORTS/{task_id}_VALIDATION.md`

---

## 💡 Example Usage

When starting a new task, simply type:

```
Use docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md
Execute task T-001: Project Structure Setup
```

The AI will:
1. Create new branch: `git checkout -b task/T-001-project-structure`
2. Read `PROJECT_MAP.md` to locate necessary docs.
3. Create or update `T-001_Project_Structure_Setup.md` under `TASK_PLANS/`.
4. Include a **Validation Plan** with `validation_owner: AI`.
5. Generate and execute a validation report at `TEST_REPORTS/T-001_VALIDATION.md`.
6. Update affected docs (`PROJECT_MAP.md`, `README.md`).
7. Record results and mark progress in PL/CL.

---

## 🔒 Notes
- AI **must never skip validation**, even for document-only tasks.
- Each validation report must explicitly state `validation_owner` and evidence.
- If `validation_owner: HUMAN`, AI must stop after preparing all required files and wait for human confirmation before marking as complete.
- **Always create a task branch** before any changes.
- **Fix errors immediately** - never accumulate errors across files.
- **Reference official documentation** for latest best practices.

---

*This prompt defines the universal discipline for VibeCode execution.  
Mention this file, then simply specify the task you want to run.*
