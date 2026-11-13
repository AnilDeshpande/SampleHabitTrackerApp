# Increment 1 plan — create a habit and see it on Overview

Approved by the project creator on 2026-11-13. Builds bottom-up: domain rules, then database and
repository, then execution and coordination, then ViewModels and UI. Each layer ends at a checkpoint
with test evidence and one commit; the next layer does not start until the one below passes.

Sources: [architecture](../architecture/habit-tracker-v1-architecture.md),
[LLD](../design/habit-tracker-v1-low-level-design.md),
[ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md),
[ADR 0004](../adr/0004-hilt-dependency-injection.md), the locked
[prototype handoff](../prototypes/HANDOFF.md) and the [design direction](../prototypes/notes/DESIGN.md).
The foundation this builds on is recorded in [foundation.md](foundation.md).

## Decisions

| Decision | Choice |
| --- | --- |
| Scope | The cut below: full domain/data core now, create flow only on top. |
| Schema | Add the remaining tables as a real migration 1→2 (not a v1 rebuild), so migration and guard-catalog tests exist from the start. |
| Dynamic colour | Off; the brand green is used on every device. |
| Error display timing | Field errors show after the first Save attempt, and also on a restored draft whose text is invalid (LLD: validation is recomputed on restore). |

## Scope

| Built to final contract now | Deferred to later increments |
| --- | --- |
| All five rule services; the full schema and guards; `ReconcileTracking` including finalization, scoring and miss outbox rows; the `CreateHabit` command; `CommandRunner`, `ForegroundCoordinator`, `BoundaryTicker`; S1, S2, S3 and every state of those two screens: five frequencies (S9–S19), validation (S20–S22), discard dialog (S43–S46), save failure and retry (S91–S94), restoration (S103–S105), loading and read failure (S88–S90), notification prompt (S63–S66). | `SaveProgress`, `EditHabit`, retirement and undo, `AcknowledgeMisses`, the notification dispatcher, WorkManager workers, Habit review and Retired habits screens. |

`CreateHabit` runs `ReconcileTracking` first inside its transaction, so reconcile is built completely
rather than stubbed.

## Layers and checkpoints

| Layer | Build | Checkpoint evidence |
| --- | --- | --- |
| 1. Domain rules | Models (`Habit`, `Occurrence`, `Frequency`, status/result enums, `TimeSnapshot`, `CurrentStatus`); `HabitInputValidator`, `OccurrencePlanner`, `TargetPolicy`, `ScoringEngine`, `ContinuityCalculator`, `BadgeEvaluator`. Pure Kotlin, no Android. | Host unit tests for the cases the architecture names: validator (trimming, limits, locale decimals, exponent/grouping rejection); planner (each cadence's first period, Monday and first-of-month creation, Custom off-days, timezone overlap/gap/date-line skip, clock moved back); scoring (integer rounding, zero floor); badge (every 21-day window boundary). |
| 2. Database and repository | Remaining tables, indexes, converters, `SchemaGuards`, migration 1→2; `RoomHabitRepository` with `ReconcileTracking` and `CreateHabit`; `observeOverview()` read model with derived Active run and Success streak. | Host tests on in-memory Room: idempotent reconcile, duplicate names, retired-name reuse, first period created correctly. Device tests on a file-backed database at API 24 and API 36 with `sqlite_version()` logged: gate, guards, migration. |
| 3. Execution and coordination | `CommandRunner` (admission key `habit.create`, busy-state projection); `ForegroundCoordinator`; `BoundaryTicker`; `NotificationAvailability` and its Android adapter; Hilt modules. | Duplicate in-flight create rejected and key released on commit and failure; clearing the ViewModel mid-command still commits; ticker wakes under `FakeClock`. |
| 4. ViewModels and UI | Green theme and `NavHost` with the seven LLD routes (only `overview` and `habit/create` real); `OverviewViewModel`, `HabitFormViewModel` with `SavedStateHandle` keys from the LLD register; the screens. | ViewModel tests from public events to `UiState`; Compose tests; device checks on API 24 and 36: `am kill` draft restore, rotation, 200% font scale, screen-reader labels, light and dark. |

## UI state models (from the architecture and LLD)

```kotlin
sealed interface OverviewUiState {
  data object Loading : OverviewUiState                 // S88, shown only after 300 ms
  data object ReadError : OverviewUiState               // S89, Retry; never a false empty state
  data class Content(                                   // S1/S116 when both lists are empty
    val points: Long,
    val habits: List<HabitSummary>,
    val pendingRetirements: List<PendingRetirementRow>, // rendered in a later increment
  ) : OverviewUiState
}

data class HabitFormUiState(
  val name: String, val unit: String, val target: String,   // exact raw text, never parsed here
  val frequency: Frequency, val weekdays: Int,              // Monday = bit 0; Mon/Wed/Fri = 21
  val errors: FieldErrors,                                  // recomputed from raw text and locale
  val startsOn: StartPreview,
  val submission: Idle | Saving | Failed,                   // Saving observes the CommandRunner key
  val isDirty: Boolean,
)
// Effects, never restored or replayed: NavigateBack, ShowHabitSaved, RequestNotificationPermission
```

## Process

- A short design note before each layer; one commit per layer after its checkpoint passes.
- Only the external-effect ports are faked, as the LLD requires. Rules, Room, repository and commands
  are always real.
- Reuse `Clock`, `AppDispatchers`, `DatabaseProvider`, `NotificationPermissionStore`,
  `NotificationChannels`, `FakeClock` and the test Hilt modules.
- `graphify update .` after each commit, and a record in `docs/implementation/` when the increment ends.
