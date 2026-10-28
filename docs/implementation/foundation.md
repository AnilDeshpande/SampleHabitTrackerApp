# Foundation implementation — 2026-10-28

The repository had a starter Compose Activity and theme, with AGP 9.2.1 and
Gradle 9.4.1. It had no database, application graph, background-work integration,
preference store, or product rules. This milestone establishes infrastructure;
the starter screen remains in place and no habit workflow is exposed yet.

## Sources and decisions

Reviewed the [PRD](../prds/habit-tracker-v1-prd.md),
[architecture](../architecture/habit-tracker-v1-architecture.md),
[LLD](../design/habit-tracker-v1-low-level-design.md),
[ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md),
[ADR 0003](../adr/0003-manual-composition-root-and-android-dependency-wiring.md),
and [prototype handoff](../prototypes/HANDOFF.md).

The architecture's 2026-10-28 decision and the LLD supersession note explicitly
select Hilt. Their linked `docs/adr/0004-hilt-dependency-injection.md` is absent
from this checkout. Hilt follows the recorded choice; the missing ADR and old
manual-factory examples remain a documentation follow-up. Existing lifetime,
restoration, and transaction contracts still apply. The LLD's accepted
restore-before-catch-up ordering governs undo when that command is implemented.

The existing Graphify graph predates the Hilt decision. It was used for source
location, then conclusions were checked directly. A code-only, offline graph
refresh accompanies this change; semantic documentation coverage remains stale.

## Delivered

| Area | Implementation |
| --- | --- |
| Build | AGP 9.5.0, Gradle 9.7.1, KSP 2.3.12, Java bytecode 17, core-library desugaring 2.1.5. Existing daemon JDK 21 retained. AGP built-in Kotlin/Compose compiler remain at compatible 2.2.10. |
| Android API contract | Compile SDK 37 is required by Lifecycle 2.11.0. Minimum API 24 and target API 36 remain unchanged. |
| DI | Hilt 2.60.1 Application/Activity entry points; singleton Clock, dispatchers, application scope, database provider, and preferences. No manual AppContainer. |
| Persistence | Room 2.8.5, schema version 1 exported under `app/schemas/`; all seven tables, declared indexes/foreign keys, enum/date/strict normalized BigDecimal converters. No destructive migration fallback. |
| Database startup | Shared production/test builder installs guards on every open; singleton balance/gate seeded on creation; explicit WAL for file-backed databases. Suspending provider initializes once off main with dedicated query/transaction executors. |
| Guards | Occurrence state/result/resolution/acknowledgement/boundary validity; retirement fields; weekday selection; active/pending name uniqueness; nonnegative balance; singleton IDs and deletion protection. SQL uses API 24-compatible constructs. |
| Time | Android-free Clock and immutable TimeSnapshot; current timezone read per snapshot; scheduler-backed FakeClock shared by both test tiers. Capturing after the gate remains a command-layer obligation. |
| Background integration | WorkManager 2.12.0 and AndroidX Hilt 1.4.0; Application Configuration.Provider with HiltWorkerFactory; only the default WorkManager initializer removed. No work is scheduled until real reconciliation/dispatch commands exist. |
| Preferences | DataStore 1.3.0 holds only the permission-requested flag, with singleton storage and injected application scope. I/O failures propagate instead of silently resetting the flag. |
| Notifications/privacy | Notification channel created on API 26+; POST_NOTIFICATIONS declared, with no startup permission prompt. Backup disabled and both backup/transfer rule files exclude all user-data storage domains. |
| Tests | Robolectric/JUnit/coroutine test harness, Hilt host graph, Hilt instrumentation runner, shared SQL contract cases, and API 36 managed-device configuration. |

Published versions were checked against [Google Maven metadata](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml),
[Room releases](https://developer.android.com/jetpack/androidx/releases/room),
[WorkManager releases](https://developer.android.com/jetpack/androidx/releases/work),
[DataStore releases](https://developer.android.com/jetpack/androidx/releases/datastore),
[Hilt setup](https://dagger.dev/hilt/gradle-setup.html), and
[KSP releases](https://github.com/google/ksp/releases).
AGP 9.5.0 was the newest stable artifact in Maven metadata; its release-notes
page was unavailable. The actual build validates this pinned combination.
The compatible Gradle 9.7.1 wrapper is not claimed to be the newest Gradle release.
UI/navigation dependency upgrades can accompany the presentation milestone.

## Verification

- `:app:assembleDebug` — passed.
- `:app:assembleDebugAndroidTest` — passed, including the shared Kotlin SQL cases.
- `:app:testDebugUnitTest` — passed: 8 tests, including the existing starter test.
  Covers real in-memory SQLite guard enforcement, guard definitions, foreign keys,
  duplicate outbox rows, singleton/gate rollback, exact decimal conversion,
  Hilt headless graph construction, lazy/concurrent database acquisition,
  dynamic timezone, immutable snapshots, and DataStore persistence after reopen.
- `:app:lintDebug` — passed with zero errors. Warnings include newer dependency
  versions, the deliberately retained target SDK, and existing starter resources.
- Merged manifest inspected: custom Application, backup disabled, notification
  permission, retained App Startup provider, and removed WorkManager initializer.
- `:app:pixel2Api36DebugAndroidTest` — passed: 2 tests (database contract/reopen
  and the existing application-context test), Android API 36 / platform SQLite
  3.44.3. The suite verifies WAL, retained history, and guard reinstallation.
- API 24 platform SQL evidence: outstanding; no API 24 device/emulator is connected.
  Host success and API 36 success cannot satisfy this requirement.

Run the API 36 suite with `./gradlew :app:pixel2Api36DebugAndroidTest`.
Run `./gradlew :app:connectedDebugAndroidTest` with an explicitly selected API 24
emulator/device, checking its API level before the run. Instrumented tests log
API/SQLite versions, verify WAL, execute the shared guard cases, and reopen a
file-backed database to confirm retained rows and guard reinstallation.

Schema version 1 is the unshipped baseline. There is no older shipped version to
migrate yet; each future schema change requires an explicit migration and
migration/guard verification at API 24 and API 36. Real backup/transfer behavior,
permission flows, production worker execution, and multi-connection contention
remain device-level obligations for the relevant later milestones.

## Next implementation order

1. **Pure domain rules:** locale-aware input validation and name keys, occurrence
   planning across DST/timezone/clock changes, target policy, integer scoring,
   continuity, and the Daily badge. Test these without Android or UI.
2. **Transactional repository and commands:** gate acquisition, one post-gate
   snapshot, global catch-up in total order, immutable finalization and audit
   trail, progress/target commands, retirement freeze/undo, and acknowledgement.
   The existing DAO exposes only the gate and balance read; it is not yet a
   product repository. Add file-backed two-connection race tests here.
3. **Execution and effects:** CommandRunner admission/lifetimes, foreground
   coordinator and BoundaryTicker, Hilt workers, six-hour unique reconciliation,
   outbox aggregation/retry/cleanup, notification availability and deep links.
   Add real worker-factory, lifecycle, crash recovery, and API 24/36 tests.
4. **Presentation:** navigation, restored ViewModels and screens from the locked
   prototype, permission request after first committed habit, accessibility,
   and process-death/device journeys.

No new product decision is needed for the foundation or these established
contracts. Quiet hours remain an external product question and should not be
invented while implementing notification dispatch.
