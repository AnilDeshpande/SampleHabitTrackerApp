# Increment 1 record — create a habit and see it on Overview

Built 2026-11-13 on branch `feature/increment-1-create-habit`, following the approved
[plan](increment-1-plan.md), bottom-up: domain rules, then database and repository, then execution and
coordination, then ViewModels and UI. One commit per layer. Prototype screens S1, S2 and S3 are real, with
every state of S1 and S2 (see Scope). Sources: the [architecture](../architecture/habit-tracker-v1-architecture.md),
the [LLD](../design/habit-tracker-v1-low-level-design.md), [ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md),
[ADR 0004](../adr/0004-hilt-dependency-injection.md), the [prototype handoff](../prototypes/HANDOFF.md) and the
[design direction](../prototypes/notes/DESIGN.md).

## What was built

| Layer | Result |
| --- | --- |
| 1. Domain rules | Models; `HabitInputValidator`, `OccurrencePlanner`, `TargetPolicy`, `ScoringEngine`, `ContinuityCalculator`, `BadgeEvaluator`; pure Kotlin, no Android. |
| 2. Database and repository | Seven tables as migration 1→2 generated from the exported `2.json`; `SchemaGuards` as SQLite triggers installed on every open; DAOs; `RoomHabitRepository` with `CreateHabit` and a complete `ReconcileTracking` (plan, then finalize in total order with integer scoring, the 21-day badge and miss outbox rows); a transactional overview read model re-emitted after each commit and assembled by a pure `OverviewAssembler`. |
| 3. Execution and coordination | `CommandRunner` (admission key, application-scope jobs, key released before effects); `TrackingCatchUp` (the single serialized foreground pipeline); `BoundaryTicker` (sleeps to the next period end, deadline or start; no-spin guard; failure backoff); `TimeChangeSource` adapter; `ForegroundCoordinator` (the one process-lifecycle observer, installed in `Application.onCreate`); `NotificationAvailability` port; `NotificationPermissionPolicy` (request once, API 33+, after the first creation). |
| 4. ViewModels and UI | Green Material 3 theme (dynamic colour off), the full seven-route `NavHost` (Overview and Create habit real; the rest placeholders), `OverviewViewModel`, `HabitFormViewModel` (draft in `SavedStateHandle` under the LLD keys), both screens, string resources. |

## Scope delivered

S1, S2 and S3, plus every state of those screens: all five frequencies with the Custom weekday chips (S9–S19),
validation and the duplicate-name error (S20–S22, S21), the discard dialog (S43–S46), saving and save failure
with retry (S91–S94), restored drafts (S103–S105), loading and read failure (S88–S90), and the one-time
notification prompt (S63–S66). Deferred as planned: `SaveProgress`, `EditHabit`, retirement and undo,
`AcknowledgeMisses`, the notification dispatcher and workers, and the Review, Record progress and Retired
habits screens, which are placeholders so the whole graph is reachable.

## Verification

| Check | Result |
| --- | --- |
| Host tests (`:app:testDebugUnitTest`) | 146 passed, 0 failed: 63 domain rules, 26 repository/guards/assembler, 23 coordination (runner, ticker, observer, permission policy, Hilt graph), 21 ViewModel, 13 foundation. |
| Device tests, API 24 (Android 7.0 arm64 AVD, SQLite 3.9.2) | 42 passed, 0 failed. |
| Device tests, API 34 (Pixel 9 AVD) | 42 passed, 0 failed. |
| Device tests, API 36 (Gradle Managed Device) | 42 passed, 0 failed. |
| Same 23 repository, guard, migration and contention scenarios on a file-backed WAL database | Pass on all three device tiers, including migration 1→2 (`MigrationTestHelper` against `2.json`, then guards present after migration) and two Room instances reconciling from eight concurrent threads (exactly five periods finalized, gate version advanced once per gated transaction). |
| `:app:lintDebug` | 0 errors, 7 warnings (target SDK note, redundant activity label, `dataExtractionRules` on API < 31, unused-resource notes). |
| Mutation checks | Removing the planner's "never start before the previous period ends" rule fails the two timezone tests; removing the guard's NULL handling fails `occurrenceGuards`. |

### Emulator inspection

The real debug app was installed and driven on the API 34 AVD, with the API 24 AVD checked for S1, S2 and S3.
Screenshots are in [evidence/increment-1](evidence/increment-1). Checked by eye:

- S1 empty Overview, S2 empty and filled form, S3 Daily card with every handoff line, the Weekly waiting state
  (S13) with "Next start: Mon 16 Nov", validation errors, duplicate-name error, discard dialog.
- The create loop end to end: Save commits, Overview shows the card, the OS notification prompt appears once on
  API 34 (not on API 24), and "Habit saved" appears after the prompt closes.
- Process death: with a dirty draft containing invalid `1,2.3`, the app was backgrounded, the process was killed
  with `am kill`, and the retained task was reopened from Recents (new PID). The destination, every field and
  the raw invalid text came back, with the error recomputed.
- Dark mode, 200% font scale (reflows without clipping; the form scrolls) and a placeholder route.
- No crash or ANR in the logs of either emulator.

## Decisions taken while building

- `CreateHabit` also plans the new habit's first period inside the same transaction, so the card shows at once
  instead of waiting for the next catch-up. The architecture does not say either way.
- A `kotlinx-serialization-core` 1.9.0 dependency constraint is in `app/build.gradle.kts`: Room's
  `MigrationTestHelper` fails with `AbstractMethodError` against the 1.7.3 that AGP's consistent resolution
  pins from `lifecycle-viewmodel-savedstate`.
- Non-Custom drafts are read as having no weekdays, so a Custom to Daily switch never emits a state with
  Daily and a stale mask (found by a test).
- The "Habit saved" snackbar waits for the system permission dialog, because its timer ran behind the dialog.

## Interpretations and additions to confirm

- Layer 1: surrounding whitespace is trimmed from numbers; `.5` and `5.` are accepted; the 9 + 3 digit limits
  are counted after dropping leading and trailing zeros.
- Overview: a Floating action "Create habit" appears once a habit exists (the prototype shows none); a
  non-Daily card shows "Progress: x / y" and "Ends Mon 19 Oct 12:00 AM" instead of the prototype's
  "12–18 Oct: x / y"; times use the device's short time format.
- The card's "Badge: not earned" and "Unacknowledged misses: 0" lines are shown always, as in the draft copy.
- A Custom habit with no open period shows "No occurrence today" on an unselected weekday and "No current
  occurrence" otherwise.
- The Save button reads "Retry" after a failed save. All copy remains draft.

## Not covered

- The LLD's SQL trace proving the gate update precedes the time snapshot; the gate is covered by version-counter,
  rollback and concurrency evidence instead.
- Rotation, TalkBack and a non-US locale (decimal separator, date formats) were not exercised on a device;
  accessibility is covered by semantic assertions (labels, selected state, disabled Save) and the 200% check.
- Compose navigation is verified by hand on the emulators, not by an automated end-to-end test, because the
  test graph runs application work on a virtual-time dispatcher.
- Nothing consumes miss outbox events yet: the dispatcher, workers, notification posting and the
  Overview-to-Review back stack arrive in later increments.
- The LLD still says "manual `AppContainer`" in places; read it as the Hilt graph (ADR 0004).
