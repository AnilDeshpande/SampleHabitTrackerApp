# Foundation implementation — 2026-10-29

Build, dependency-injection, persistence and test-tier configuration for Habit
Tracker V1, done fresh on `main` from the Android Studio template. No product
behavior exists yet: the starter screen is unchanged and no habit workflow is
exposed. Sources: the [architecture](../architecture/habit-tracker-v1-architecture.md),
the [low-level design](../design/habit-tracker-v1-low-level-design.md),
[ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md) and
[ADR 0004](../adr/0004-hilt-dependency-injection.md) (Hilt, superseding ADR 0003's
manual wiring for the mechanism only).

## What was built

| Area | Result |
| --- | --- |
| Toolchain | AGP 9.5.0 on Gradle 9.8.2 (AGP 9.5 needs 9.6.0+), JDK 17 bytecode, compileSdk 37 (Lifecycle 2.11 requires it), minSdk 24, targetSdk 36. AGP's built-in Kotlin resolves to 2.4.10; the Compose compiler plugin is pinned to match. |
| Annotation processing | KSP 2.3.12 only (no kapt) for Room, Hilt and AndroidX Hilt. |
| Dependency injection | Hilt 2.60.1, AndroidX Hilt 1.4.0. `HabitTrackerApplication` is `@HiltAndroidApp` and installs `HiltWorkerFactory` through WorkManager's on-demand `Configuration.Provider`; only `WorkManagerInitializer` is removed from the merged manifest. Process-wide seams (`Clock`, `AppDispatchers`, application scope) and storage locations are separate modules so the test graph replaces each wholesale. |
| Persistence | Room 2.8.5 with the Room Gradle plugin; schema v1 exported to `app/schemas` and committed. Only the architecture's `app_state` singleton table exists. One shared production/test builder (seed callback, WAL for file-backed, no destructive-migration fallback). `DatabaseProvider` opens Room lazily and once on the injected io dispatcher, with dedicated query and transaction executors. The remaining tables, converters and SchemaGuards arrive with the repository milestone. |
| Preferences | DataStore 1.3.0 holds only `notificationPermissionRequested`. |
| Notifications / privacy | "Missed targets" channel through `NotificationChannelCompat` (no-op below API 26); `POST_NOTIFICATIONS` declared, with no startup prompt. `allowBackup="false"`; data-extraction rules exclude `database`, `sharedpref`, `file`, `external` and `root` from cloud backup and device transfer; the unused `backup_rules.xml` is removed. |
| Time | `java.time` through core-library desugaring (desugar_jdk_libs 2.1.5) for API 24–25. |
| Test libraries | Robolectric 4.17, Mockk 1.14.11 (external-effect ports and ViewModel UI-mapping tests only, per the LLD), coroutines-test 1.11.0, `hilt-android-testing`, `room-testing`, `work-testing`, AndroidX test 1.7.0. |
| Pinned, not yet used | Navigation Compose 2.10.2, `lifecycle-viewmodel-compose` / `lifecycle-process` 2.11.0 and `hilt-navigation-compose` 1.4.0 are in the version catalog; they become `:app` dependencies in the milestone that uses them. |

## Test tiers

| Tier | Source set | How it runs |
| --- | --- | --- |
| Host (Robolectric) | `app/src/test` | `./gradlew :app:testDebugUnitTest` |
| Instrumented, API 36 | `app/src/androidTest` | `./gradlew :app:pixel2Api36DebugAndroidTest` (Gradle Managed Device, AOSP) |
| Instrumented, API 24 | `app/src/androidTest` | connected AVD `habit_api24`, see below |

Fixtures used by both tiers (`FakeClock`, the test Hilt modules, the SQL cases in
`DatabaseSmoke`) live in `app/src/sharedTest/java`, added to both source sets.
The instrumented runner starts `HiltTestApplication` before any lifecycle callback.

Host SQLite is not minimum-API evidence. A SQL-facing test counts as guard or
migration evidence only after it passes on API 24. Every instrumented SQL test logs
the API level and `sqlite_version()` under the log tag `SqlEvidence`; Gradle keeps the
per-test logcat in `app/build/outputs/androidTest-results/**/logcat-*.txt`.

### API 24 on Apple Silicon

Managed devices start at API 27, and the SDK's legacy `avdmanager` cannot run on
current JDKs, so the API 24 device is a regular AVD built from the arm64 AOSP image
`system-images;android-24;default;arm64-v8a` (`android sdk install` it first).
Create `~/.android/avd/habit_api24.ini` (`path=…/habit_api24.avd`, `path.rel=avd/habit_api24.avd`,
`target=android-24`) and `habit_api24.avd/config.ini` with these essentials:

```ini
AvdId=habit_api24
abi.type=arm64-v8a
hw.cpu.arch=arm64
hw.device.name=pixel_2
hw.lcd.width=1080
hw.lcd.height=1920
hw.lcd.density=420
hw.ramSize=2048
disk.dataPartition.size=4G
image.sysdir.1=system-images/android-24/default/arm64-v8a/
tag.id=default
tag.display=Default Android System Image
```

Boot it headless, then pin the run to its serial so a higher-API device cannot
satisfy the API 24 tier:

```bash
~/Library/Android/sdk/emulator/emulator -avd habit_api24 -no-window -no-audio -no-snapshot -gpu swiftshader_indirect &
adb wait-for-device && adb shell getprop ro.build.version.sdk   # must print 24
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest
```

## Verification (2026-10-29)

| Check | Result |
| --- | --- |
| `./gradlew help`, `build --dry-run` | Passed on AGP 9.5.0 / Gradle 9.8.2. |
| `:app:assembleDebug`, `:app:assembleDebugAndroidTest` | Passed. |
| `:app:testDebugUnitTest` | Passed: 13 tests (production Application wiring against the merged manifest, Hilt test graph, in-memory seed and Flow, file-backed WAL and reopen, DataStore persistence, FakeClock, Mockk, template placeholder). Host SQLite 3.44.3. |
| API 36 managed device | Passed: 3 tests, SQLite 3.44.3, `journal_mode=wal`. |
| API 24 connected AVD | Passed: 3 tests on Android 7.0 arm64, SQLite 3.9.2, `journal_mode=wal`. |
| `:app:lintDebug` | 0 errors, 11 warnings (below). |
| Mutation check | The merged-manifest test fails when the `WorkManagerInitializer` removal is deleted, so it is not vacuous. |

Lint warnings: `targetSdk 36` is intentionally unchanged; `dataExtractionRules` without
`fullBackupContent` follows the architecture's explicit instruction because backup is
disabled; the redundant activity label and seven unused color resources come from the
template; the Compose plugin has a newer patch (2.4.21) but is held at the Kotlin
version AGP resolves. The manifest merger also prints a harmless "no other declaration
present" note for the `WorkManagerInitializer` removal; the merged manifest is correct.

Not covered here: real workers built through `HiltWorkerFactory` (no worker exists yet),
restored ViewModel state, SchemaGuards, migrations, multi-connection contention,
backup/transfer behavior on a device, and notification permission flows. Each belongs to
the milestone that introduces the code.

## Next implementation order

1. **Pure domain rules** (Android-free): input validation and name keys, occurrence
   planning across DST and timezone changes, target policy, integer scoring, continuity,
   the Daily badge.
2. **Transactional repository and commands:** remaining tables and converters,
   SchemaGuards with API 24 SQL evidence, gate acquisition and global catch-up.
3. **Execution and effects:** `CommandRunner`, foreground coordinator and
   `BoundaryTicker`, Hilt workers, outbox and notifications.
4. **Presentation:** Navigation Compose routes, Hilt ViewModels, the screens from the
   locked prototype.

The LLD still names the manual `AppContainer` in several places; ADR 0004 and the
LLD's supersession note say to read those as the Hilt graph. Rewording them is a
separate documentation change.
