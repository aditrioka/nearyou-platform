# ⚡ VibeCode Short Meta Prompt (Full Execution Discipline)

Use `docs/CORE/PROJECT_MAP.md` as the main reference to locate all project files.

---

## 🧩 Execution Flow
Always follow this flow for every task:

1. **Plan First**  
   Create or update a short task execution plan at  
   `docs/TASK_PLANS/{task_id}_{task_name}.md`.  
   The plan must include:
    - Scope, dependencies, affected docs, expected output.
    - **Validation Plan**:
        - validation_owner → `AI`, `HUMAN`, or `HYBRID`
        - ai_capability → what AI can/cannot validate
        - human_prereq → what human must do (access, credentials, etc.)
        - evidence_required → screenshots, quotes, or paths
        - pass_criteria → exact checks to mark PASS

2. **Validation First (Before Any Implementation)**
    - If the task only involves document creation or modification,  
      perform validation by **reading and reviewing the generated docs end-to-end**  
      using the Validation Template (`docs/TEST_REPORTS/{task_id}_VALIDATION.md`).
    - If the task involves executable code, use commands defined in  
      `docs/CORE/TESTING.md → How to Validate Changes`.
    - Validation can be done by:
        - **AI** → for text, structure, and repository consistency
        - **HUMAN** → for external or manual validation (web flow, UI, API tokens)
        - **HYBRID** → both, depending on capability and context

3. **Document Updates**
    - Update all outdated documentation before implementing any code changes.
    - Sync with architecture, specs, and plans.
    - Ensure no doc contradicts the task output.

4. **Implementation**
    - After validation and doc updates are complete, implement the change.
    - Generate or modify files as defined in the task plan.

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
1. Read `PROJECT_MAP.md` to locate necessary docs.
2. Create or update `T-001_Project_Structure_Setup.md` under `TASK_PLANS/`.
3. Include a **Validation Plan** with `validation_owner: AI`.
4. Generate and execute a validation report at `TEST_REPORTS/T-001_VALIDATION.md`.
5. Update affected docs (`PROJECT_MAP.md`, `README.md`).
6. Record results and mark progress in PL/CL.

---

## 🔒 Notes
- AI **must never skip validation**, even for document-only tasks.
- Each validation report must explicitly state `validation_owner` and evidence.
- If `validation_owner: HUMAN`, AI must stop after preparing all required files and wait for human confirmation before marking as complete.

---

*This prompt defines the universal discipline for VibeCode execution.  
Mention this file, then simply specify the task you want to run.*
