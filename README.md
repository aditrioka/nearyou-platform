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

This repository uses a **map-based documentation system** for both developers and AI assistants like *Augment Code*.  
All major plans, architecture docs, and phase progress are indexed in [`docs/PROJECT_MAP.md`](./docs/PROJECT_MAP.md).

**Start here:**
- [`docs/PROJECT_MAP.md`](./docs/PROJECT_MAP.md) → The main map document (references all docs and plans).
- [`docs/PLANS/NearYou_ID_MVP_Plan.md`](./docs/PLANS/NearYou_ID_MVP_Plan.md) → The complete execution plan.
- [`docs/PLANS/PHASE_0_COMPLETION_SUMMARY.md`](./docs/PLANS/PHASE_0_COMPLETION_SUMMARY.md) → Results after Phase 0.
- [`docs/PLANS/QUICK_START.md`](./docs/PLANS/QUICK_START.md) → Environment setup and onboarding guide.

> 🧠 **For AI assistants:** Always use `PROJECT_MAP.md` as the main reference to locate relevant documents when performing context-based coding or planning tasks.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).
