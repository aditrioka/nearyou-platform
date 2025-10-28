# 🗺️ PROJECT_MAP.md

**Main navigation index for the NearYou ID repository.**  
Helps developers and AI tools locate all relevant project files efficiently.

---

## 📘 Core Documentation

### Essential Reading
- **[PROJECT_MAP.md](./PROJECT_MAP.md)** → **This file - main documentation index**
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** → System architecture, layers, and modular design
- **[SPEC.md](./SPEC.md)** → Product specifications and user flows
- **[INFRA.md](./INFRA.md)** → Infrastructure, deployment, database, and environment setup
- **[TESTING.md](./TESTING.md)** → Testing strategy and guidelines
- **[PERFORMANCE.md](./PERFORMANCE.md)** → Performance testing, metrics, and optimization
- **[VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md)** → How to validate changes (AI/HUMAN/HYBRID)
- **[DECISIONS.md](./DECISIONS.md)** → Architectural Decision Records (ADRs)
- **[CHANGELOG.md](./CHANGELOG.md)** → Version history, release notes, and current status

### Additional Resources
- **[BEST_PRACTICES_EVALUATION.md](./BEST_PRACTICES_EVALUATION.md)** → Codebase compliance evaluation (2025-10-22)
- **[IMPROVEMENT_ROADMAP.md](./IMPROVEMENT_ROADMAP.md)** → Phased improvement plan
- **[IMPLEMENTATION_LOG.md](./IMPLEMENTATION_LOG.md)** → Log of implemented changes (2025-10-22)

---

## 🗺️ Project Plans

- **[NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md)** → **Full MVP execution plan with milestones and tasks**
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

- **[VIBECODE_SHORT_META_PROMPT.md](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md)** → **AI execution workflow**
  - Enforces validation-first discipline
  - Defines AI/HUMAN/HYBRID validation modes
  - Quick reference for vibe code development

---

## ✅ Checklists

- **[PRE_PUSH_CHECKLIST.md](../CHECKLISTS/PRE_PUSH_CHECKLIST.md)** → Pre-push verification checklist

---

## 📄 API Documentation

- **[API_DOCUMENTATION.md](../API_DOCUMENTATION.md)** → REST API reference with all endpoints

---

## 🗄️ Database & Infrastructure

- **`database/`** → Schema definitions, migrations, seed data
  - **[database/README.md](../../database/README.md)** → Quick reference (see [INFRA.md](./INFRA.md) for complete docs)
  - **[database/migrations/](../../database/migrations/)** → All database migrations
- **`docker/`** → Dockerfile and docker-compose.yml

---

## ⚡ Performance Testing

- **`performance-tests/`** → k6 load testing scripts
  - **[performance-tests/README.md](../../performance-tests/README.md)** → Quick reference (see [PERFORMANCE.md](./PERFORMANCE.md) for complete docs)
  - **[performance-tests/auth-load-test.js](../../performance-tests/auth-load-test.js)** → Authentication load test script

---

## 📂 Source Code Structure

```
nearyou-id/
├── composeApp/          # Android & iOS UI (Compose Multiplatform)
│   ├── commonMain/      # Shared UI code
│   ├── androidMain/     # Android-specific code
│   └── iosMain/         # iOS-specific code
├── shared/              # Shared business logic (KMP) - SINGLE SOURCE OF TRUTH FOR MODELS
│   ├── commonMain/      # Platform-independent code
│   │   ├── domain/      # Domain models & validation
│   │   └── data/        # Repositories & network
│   ├── androidMain/     # Android-specific implementations (Keystore)
│   ├── iosMain/         # iOS-specific implementations (Keychain)
│   └── jvmMain/         # JVM-specific implementations
├── server/              # Backend API (Ktor) - IMPORTS MODELS FROM /shared
│   └── src/main/kotlin/ # Server code
├── iosApp/              # iOS app wrapper
├── database/            # Database scripts and migrations
│   ├── init.sql         # PostGIS setup
│   └── migrations/      # Database migrations
├── performance-tests/   # k6 load testing scripts
└── docs/                # Documentation (THIS FILE)
```

---

## 🔗 Quick Links

| Document | Purpose | Type |
|----------|---------|------|
| [README.md](../../README.md) | Project overview and quick start | Entry Point |
| [PROJECT_MAP.md](./PROJECT_MAP.md) | This file - navigation hub | **Navigation** |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | System design and MVI pattern | Technical |
| [SPEC.md](./SPEC.md) | Product requirements | Product |
| [QUICK_START.md](../PLANS/QUICK_START.md) | Setup guide | Getting Started |
| [VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md) | Validation procedures | Process |
| [API_DOCUMENTATION.md](../API_DOCUMENTATION.md) | API reference | Technical |
| [INFRA.md](./INFRA.md) | Infrastructure & database | Technical |
| [PERFORMANCE.md](./PERFORMANCE.md) | Performance testing | Technical |
| [VIBECODE_SHORT_META_PROMPT.md](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md) | AI workflow guide | Process |
| [NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md) | Complete execution plan | Planning |
| [CHANGELOG.md](./CHANGELOG.md) | Version history & status | Status |

---

## 📊 Current Project Status

**Version:** 1.0.0 (Production Ready)  
**Status:** ✅ Authentication Platform Complete  
**Test Coverage:** 32/32 tests passing (100%)  
**Compliance Score:** 9.8/10

**For detailed status, see [CHANGELOG.md](./CHANGELOG.md#current-status-as-of-2025-10-24)**

---

## 🎯 Navigation Tips

### For New Developers
1. Start with [README.md](../../README.md) - Quick project overview
2. Read [QUICK_START.md](../PLANS/QUICK_START.md) - Set up development environment
3. Review [ARCHITECTURE.md](./ARCHITECTURE.md) - Understand system design
4. See [NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md) - Full execution plan

### For AI-Assisted Development
1. Read [VIBECODE_SHORT_META_PROMPT.md](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md) - AI workflow
2. Use [PROJECT_MAP.md](./PROJECT_MAP.md) (this file) - Find all documents
3. Follow [VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md) - Validation procedures
4. Check [NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md) - Task details

### For Task Implementation
1. Read task plan in [TASK_PLANS/](../TASK_PLANS/)
2. Follow [VIBECODE_SHORT_META_PROMPT.md](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md) workflow
3. Validate using [VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md)
4. Document in [TEST_REPORTS/](../TEST_REPORTS/)
5. Update [CHANGELOG.md](./CHANGELOG.md) and MVP Plan

### For API Development
1. Read [API_DOCUMENTATION.md](../API_DOCUMENTATION.md) - API reference
2. Review [ARCHITECTURE.md](./ARCHITECTURE.md) - Backend architecture
3. See [INFRA.md](./INFRA.md) - Database schema
4. Check [TESTING.md](./TESTING.md) - Testing strategy

### For Database Work
1. See [INFRA.md](./INFRA.md#database-setup) - Complete database documentation
2. Quick reference: [database/README.md](../../database/README.md)
3. Migrations: [database/migrations/](../../database/migrations/)

### For Performance Testing
1. See [PERFORMANCE.md](./PERFORMANCE.md) - Complete performance documentation
2. Quick reference: [performance-tests/README.md](../../performance-tests/README.md)
3. Scripts: [performance-tests/](../../performance-tests/)

---

## 📝 Document Hierarchy

```
Root (README.md)
│
├─── Core Docs (docs/CORE/)
│    ├─── PROJECT_MAP.md (This file)
│    ├─── ARCHITECTURE.md
│    ├─── SPEC.md
│    ├─── INFRA.md (includes database docs)
│    ├─── PERFORMANCE.md (includes performance testing docs)
│    ├─── VALIDATION_GUIDE.md
│    ├─── TESTING.md
│    ├─── DECISIONS.md
│    └─── CHANGELOG.md (includes deployment status)
│
├─── Plans (docs/PLANS/)
│    ├─── NearYou_ID_MVP_Plan.md (Complete execution plan)
│    ├─── QUICK_START.md
│    └─── PHASE_0_COMPLETION_SUMMARY.md
│
├─── Prompts (docs/PROMPTS/)
│    └─── VIBECODE_SHORT_META_PROMPT.md (AI workflow)
│
├─── Task Plans (docs/TASK_PLANS/)
│    └─── T-{id}_{name}.md
│
├─── Test Reports (docs/TEST_REPORTS/)
│    └─── T-{id}_VALIDATION.md
│
├─── API Docs (docs/)
│    └─── API_DOCUMENTATION.md
│
├─── Database (database/)
│    ├─── README.md (Quick ref → see INFRA.md)
│    ├─── init.sql
│    └─── migrations/
│
└─── Performance Tests (performance-tests/)
     ├─── README.md (Quick ref → see PERFORMANCE.md)
     └─── auth-load-test.js
```

---

**Last Updated:** 2025-10-28  
**Maintained By:** Development Team  
**Purpose:** Single source of truth for documentation navigation

