# ✅ Task Validation Template (AI/HUMAN/HYBRID)

> Use this file to validate a task after execution — whether document-only, command-driven, or mixed.
> Save as: `docs/TEST_REPORTS/{task_id}_VALIDATION.md` (e.g., `T-001_VALIDATION.md`).

---

## 1️⃣ Context
- **Task ID:** T-XXX  
- **Title:**  
- **Related Plan:** docs/PLANS/NearYou_ID_MVP_Plan.md (link to task)  
- **Affected Docs/Areas:** (list files or domains)

---

## 2️⃣ Definition of Done (copied or adapted from the task plan)
- [ ] DoD-1  
- [ ] DoD-2  
- [ ] DoD-3  

---

## 3️⃣ Validation Mode
| Field | Description |
|-------|--------------|
| **validation_owner** | AI / HUMAN / HYBRID |
| **who_validated** | Name / “AI Agent” / Both |
| **ai_capability** | What AI can check (paths, docs, structure) |
| **human_prereq** | What human needs to do (e.g., external login, manual test) |
| **evidence_required** | Screenshots, quotes, URLs, timestamps |
| **pass_criteria** | Clear measurable result for PASS |

---

## 4️⃣ Validation Steps
Describe how the validation was performed.

Example:
1. Open updated docs:
   - [ ] ARCHITECTURE.md → check section “Client-Server Interface”
   - [ ] SPEC.md → ensure consistency with feature name
   - [ ] PROJECT_MAP.md → confirm file paths
2. Verify correctness:
   - [ ] No TODO or placeholder remains  
   - [ ] All paths and links are valid  
   - [ ] Document matches plan and architecture
3. Manual/external actions (if needed):
   - [ ] Register on site X / test API endpoint Y  
   - [ ] Verify token or permission flow  
   - [ ] Capture screenshot evidence

---

## 5️⃣ Evidence
Attach or describe proof of validation (copy snippets, screenshots, or notes).

Example:
- “Verified that PROJECT_MAP.md contains link to VIBECODE_SHORT_META_PROMPT.md.”  
- Screenshot of folder structure validation.  
- Output log from successful Gradle test.

---

## 6️⃣ Result Summary
| Status | Notes |
|--------|--------|
| ✅ PASS | Meets all DoD and validation checks |
| ⚠️ FAIL | Issues found, list below |

**Follow-up Tasks (if any):**
- [ ] Gap-1 → (Create T-### follow-up task)  
- [ ] Gap-2 → (Describe issue)

---

🧠 *Tip:* Keep validation factual and auditable. This file serves as the traceable proof that the task output matches project standards.
