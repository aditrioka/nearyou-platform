# 🗺️ PROJECT_MAP.md

This document serves as the main navigation index for the NearYou ID repository.  
It helps both developers and AI tools (like Augment Code or Copilot Workspaces) locate all relevant project files efficiently.

---

## 📘 Core Documentation
- **docs/CORE/PROJECT_MAP.md** → Main index for all documentation and references.
- **docs/CORE/ARCHITECTURE.md** → System architecture, layers, and modular design.
- **docs/CORE/SPEC.md** → Product specifications and user flow references.
- **docs/CORE/INFRA.md** → CI/CD, deployment, environment setup, and secret management.
- **docs/CORE/TESTING.md** → Testing strategy, automation, and coverage goals.
- **docs/CORE/DECISIONS.md** → Key architectural and product decisions (ADR-style).
- **docs/CORE/CHANGELOG.md** → Repository-wide changelog and release notes.

---

## 🗺️ Project Plans
- **docs/PLANS/NearYou_ID_MVP_Plan.md** → Full execution plan with milestones, ledger, and changelog.
- **docs/PLANS/PHASE_0_COMPLETION_SUMMARY.md** → Summary of Phase 0 progress and results.
- **docs/PLANS/QUICK_START.md** → Environment setup and developer onboarding guide.

---

## ⚙️ Task-Level Plans
- **docs/TASK_PLANS/** → Contains granular task execution plans (T-###) for AI-assisted development. Each file defines scope, dependencies, affected modules, and required documentation updates.

---

## 🧪 Test Reports
- **docs/TEST_REPORTS/** → Contains setup guides and validation reports related to specific tasks. Includes test procedures, results, and verification notes for implemented features.

---

## 🤖 AI Prompts
- **docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md** → The single reusable prompt for AI-assisted execution. Use this before specifying a task (T-###) to ensure the AI first creates a task plan, updates necessary docs, and executes accurately.

---

## ✅ Checklists
- **docs/CHECKLISTS/PRE_PUSH_CHECKLIST.md** → Pre-push routine to maintain code and documentation consistency.
- **docs/CHECKLISTS/CONTRIBUTION_GUIDE.md** → Contribution workflow and coding standards (optional).

---

## 🗄 Database
- **database/** → Contains schema definitions, migration scripts, and seed data.

## 🐳 Containers
- **docker/Dockerfile** → Base build and runtime image.
- **docker/docker-compose.yml** → Local multi-service environment configuration.
