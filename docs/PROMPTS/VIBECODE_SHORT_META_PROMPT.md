# ⚡ VibeCode Short Meta Prompt (Concise Full Discipline)

Use `docs/CORE/PROJECT_MAP.md` as the main index to locate all required files.

---

## 🧩 Execution Flow

1. **Plan** — Create or update `docs/TASK_PLANS/{task_id}_{task_name}.md` with:
    - Scope, dependencies, affected docs, outputs.
    - Validation Plan: `validation_owner (AI/HUMAN/HYBRID)`, `ai_capability`, `human_prereq`, `evidence_required`, and `pass_criteria`.

2. **Validate First** — Before coding or writing:
    - For doc-only tasks, review generated docs via `docs/TEST_REPORTS/{task_id}_VALIDATION.md`.
    - For code tasks, use commands in `docs/CORE/TESTING.md → How to Validate Changes`.
    - AI validates consistency; human handles manual/external checks; HYBRID splits responsibility.

3. **Update Docs** — Sync and fix outdated or conflicting documentation before code updates.

4. **Implement** —
    - Create a **new branch** for each task (`feature/T-005_update_auth_system`).
    - **AI must not commit or push**; only the maintainer (Adi) may review, commit, and merge.
    - AI may prepare commits locally.
    - Follow the latest **industry best practices**, **clean architecture**, and **separation of concerns**.
    - Use `context7` to reference the latest GitHub documentation and `websearch` to confirm current practices.
    - Ensure all code is modular, testable, framework-independent, and error-free before proceeding.
    - After each file update, immediately check for errors before continuing.

5. **Post-Validate & Log** —
    - Run final validation; record evidence and results in `docs/TEST_REPORTS/{task_id}_VALIDATION.md`.
    - Mark PASS/FAIL, add follow-ups, and update Progress Ledger & Changelog in `docs/PLANS/NearYou_ID_MVP_Plan.md`.

---

## 🧪 Validation Reference
- Template → `docs/TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md`
- Commands → `docs/CORE/TESTING.md → How to Validate Changes`
- Reports → `docs/TEST_REPORTS/{task_id}_VALIDATION.md`

---

## 💡 Example Usage
Evaluate the entire project codebase — both backend and frontend — for compliance with the latest industry best practices and architecture standards.

Use context7 to reference the most recent official GitHub repositories and documentation of all technologies used in this project
(e.g., Kotlin Multiplatform, Jetpack Compose, Ktor, and related dependencies),
and use websearch to identify the latest community-validated best practices and coding conventions from credible engineering sources.

Perform a comprehensive analysis including:
1. Architectural alignment with clean architecture, separation of concerns, and modular design.
2. Code quality in terms of readability, testability, maintainability, and scalability.
3. Consistency of naming, structure, and package organization across backend and frontend modules.
4. Use of modern APIs, recommended libraries, and avoidance of deprecated or unsafe practices.
5. Security practices (authentication, data handling, network communication).
6. Performance and memory optimization patterns.
7. Proper application of dependency injection, concurrency (coroutines/flows), and UI state management.

For each issue or improvement:
- Cite specific best-practice sources from context7 or websearch.
- Suggest corrected approaches or modern replacements.
- Provide reasoning linked to documentation or examples.

If any documentation inside `docs/` (e.g., `ARCHITECTURE.md`, `PROJECT_MAP.md`, or `TESTING.md`) is outdated or inconsistent with your findings,
update or rewrite those files accordingly to align them with verified modern standards.

Deliverables:
- Summary of backend and frontend compliance status.
- Detailed improvement recommendations.
- References (context7/websearch sources).
- Updated documentation under `docs/` if required.
```
Use docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md
Execute task T-001: Project Structure Setup
```

AI will:
- Read `PROJECT_MAP.md`, create/update task plan, run validation, update docs, and log results.
- Stop before commit; only Adi commits and merges.

---

## 🔒 Notes
- Always follow official docs, GitHub examples, and modern best practices.
- AI must never skip validation, nor commit or push.
- Each validation report must specify owner and evidence.
- If `validation_owner: HUMAN`, AI must stop and await human review.
- All implementations must use clean architecture, modular structure, and verified practices (via context7 & websearch).
- Fix all detected errors before moving to the next file.

---

*Defines universal discipline for VibeCode execution. Mention this file, then specify the task to run.*
