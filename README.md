# NearYou ID – Kotlin Multiplatform Project

This is a Kotlin Multiplatform project targeting **Android**, **iOS**, and **Server**.

* [/composeApp](./composeApp/src) contains the code shared across your Compose Multiplatform applications.
  It includes:
  - [commonMain](./composeApp/src/commonMain/kotlin) for code common to all targets.
  - Platform-specific folders like [iosMain](./composeApp/src/iosMain/kotlin) and [jvmMain](./composeApp/src/jvmMain/kotlin) for platform-dependent logic.

* [/iosApp](./iosApp/iosApp) contains the iOS application entry point.
  Even when sharing UI with Compose Multiplatform, you’ll still need this folder to integrate SwiftUI or platform code.

* [/server](./server/src/main/kotlin) holds the **Ktor backend** code.

* [/shared](./shared/src) contains shared business logic modules between all targets.
  The most important folder is [commonMain](./shared/src/commonMain/kotlin).

---

### 🧩 Build and Run Android Application

To build and run the Android app:

```bash
./gradlew :composeApp:assembleDebug
```

or on Windows:

```bash
.\gradlew.bat :composeApp:assembleDebug
```

### 🌐 Build and Run Server

To build and run the Ktor backend:

```bash
./gradlew :server:run
```

or on Windows:

```bash
.\gradlew.bat :server:run
```

### 🍎 Build and Run iOS Application

Open [/iosApp](./iosApp) in Xcode and run it from there, or use the run configuration in your IDE.

---

## 🗺️ Repository Navigation

This repository uses a **map-based documentation system** for both developers and AI assistants like *Augment Code* or *Copilot Workspaces*.  
All documentation and AI-related files are organized under the [`docs/`](./docs) directory.

**Start here:**
- [`docs/CORE/PROJECT_MAP.md`](./docs/CORE/PROJECT_MAP.md) → Main index for all documentation and project references.
- [`docs/PLANS/NearYou_ID_MVP_Plan.md`](./docs/PLANS/NearYou_ID_MVP_Plan.md) → Full product execution plan.
- [`docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md`](./docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md) → Universal AI execution prompt for any VibeCode task.

> 🧠 **For AI Assistants:** Always read `PROJECT_MAP.md` first to locate files, then use `VIBECODE_SHORT_META_PROMPT.md` to execute or continue a task.

---

## ✅ Validation-First System (AI / HUMAN / HYBRID)

NearYou ID integrates a **validation-first development flow** to ensure all changes (code, docs, or infrastructure) are reviewed and auditable.  
Each task defines a **Validation Plan** within its task file and produces a validation report afterward.

### 📋 Key Files
- **`docs/TEST_REPORTS/TASK_VALIDATION_TEMPLATE.md`** → Template for all validation reports.
- **`docs/CORE/TESTING.md`** → Contains “How to Validate Changes” appendix (document-based & command-based validation).
- **`docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md`** → Defines how AI performs tasks with validation-first discipline.

### 🔍 Validation Modes
| Mode | Description |
|------|--------------|
| `AI` | AI validates file consistency, structure, and links. |
| `HUMAN` | Human performs external/manual checks (e.g., web registration, design verification). |
| `HYBRID` | Both AI and human perform validation collaboratively. |

Validation results are stored under `docs/TEST_REPORTS/T-###_VALIDATION.md`, each including the validation owner, evidence, and pass/fail summary.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).
