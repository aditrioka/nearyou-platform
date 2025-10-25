# 🗺️ PROJECT_MAP.md

**Main navigation index for the NearYou ID repository.**  
Helps developers and AI tools locate all relevant project files efficiently.

---

## 📘 Core Documentation

### Essential Reading
- **[PROJECT_MAP.md](./PROJECT_MAP.md)** → This file - main documentation index
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** → System architecture, layers, and modular design
- **[SPEC.md](./SPEC.md)** → Product specifications and user flows
- **[INFRA.md](./INFRA.md)** → Infrastructure, deployment, and environment setup
- **[TESTING.md](./TESTING.md)** → Testing strategy and guidelines
- **[VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md)** → How to validate changes (AI/HUMAN/HYBRID)
- **[DECISIONS.md](./DECISIONS.md)** → Architectural Decision Records (ADRs)
- **[CHANGELOG.md](./CHANGELOG.md)** → Version history and release notes

### Additional Resources
- **[BEST_PRACTICES_EVALUATION.md](./BEST_PRACTICES_EVALUATION.md)** → Codebase compliance evaluation (2025-10-22)
- **[IMPROVEMENT_ROADMAP.md](./IMPROVEMENT_ROADMAP.md)** → Phased improvement plan
- **[IMPLEMENTATION_LOG.md](./IMPLEMENTATION_LOG.md)** → Log of implemented changes (2025-10-22)
- **[PERFORMANCE.md](./PERFORMANCE.md)** → Performance optimization guidelines

---

## 🗺️ Project Plans

- **[NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md)** → Full MVP execution plan with milestones
- **[PHASE_0_COMPLETION_SUMMARY.md](../PLANS/PHASE_0_COMPLETION_SUMMARY.md)** → Phase 0 summary
- **[QUICK_START.md](../PLANS/QUICK_START.md)** → Quick setup guide for developers

---

## ⚙️ Task Plans

- **[TASK_PLANS/](../TASK_PLANS/)** → Granular task execution plans (T-###)
  - Each file defines scope, dependencies, affected modules, and validation plan
  - Validation owner: `AI`, `HUMAN`, or `HYBRID`

---

## 🧪 Test Reports & Validation

- **[TEST_REPORTS/](../TEST_REPORTS/)** → Validation and test reports for specific tasks
- **[TASK_VALIDATION_TEMPLATE.md](../TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md)** → Template for validation reports

---

## 🤖 AI Workflow

- **[VIBECODE_SHORT_META_PROMPT.md](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md)** → AI execution workflow
  - Enforces validation-first discipline
  - Defines AI/HUMAN/HYBRID validation modes

---

## ✅ Checklists

- **[PRE_PUSH_CHECKLIST.md](../CHECKLISTS/PRE_PUSH_CHECKLIST.md)** → Pre-push verification checklist

---

## 📄 API Documentation

- **[API_DOCUMENTATION.md](../API_DOCUMENTATION.md)** → REST API reference

---

## 🗄️ Database & Infrastructure

- **`database/`** → Schema definitions, migrations, seed data
- **`docker/`** → Dockerfile and docker-compose.yml

---

## 📂 Source Code Structure

```
nearyou-id/
├── composeApp/          # Android & iOS UI (Compose Multiplatform)
│   ├── commonMain/      # Shared UI code
│   ├── androidMain/     # Android-specific code
│   └── iosMain/         # iOS-specific code
├── shared/              # Shared business logic (KMP)
│   ├── commonMain/      # Platform-independent code
│   │   ├── domain/      # Domain models & validation
│   │   └── data/        # Repositories & network
│   ├── androidMain/     # Android-specific implementations
│   ├── iosMain/         # iOS-specific implementations
│   └── jvmMain/         # JVM-specific implementations
├── server/              # Backend API (Ktor)
│   └── src/main/kotlin/ # Server code
├── iosApp/              # iOS app wrapper
├── database/            # Database scripts
└── docs/                # Documentation
```

---

## 🔗 Quick Links

| Document | Purpose |
|----------|---------|
| [README.md](../../README.md) | Project overview |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | System design |
| [SPEC.md](./SPEC.md) | Product requirements |
| [QUICK_START.md](../PLANS/QUICK_START.md) | Setup guide |
| [VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md) | Validation procedures |
| [API_DOCUMENTATION.md](../API_DOCUMENTATION.md) | API reference |

