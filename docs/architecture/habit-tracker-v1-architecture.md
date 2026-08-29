# Habit Tracker V1 — Architecture

## Status and scope

| Field | Value |
| --- | --- |
| Status | Proposed technical architecture |
| Date | 2026-08-26 |
| Product source | [`docs/prds/habit-tracker-v1-prd.md`](../prds/habit-tracker-v1-prd.md) |
| Platform | Single-user Android app; minimum API 24 |
| Persistence boundary | On-device only |

This document turns the approved product behavior into a technical design. It
does not add product behavior beyond the PRD. It is intentionally designed for
a small, testable Jetpack Compose application, not a multi-module or
client-server system.

## Architectural decisions

| Decision | Choice | Why |
| --- | --- | --- |
| UI | Jetpack Compose with Material 3 | Matches the existing app and supports explicit, accessible UI state. |
| Presentation state | Screen-level `ViewModel`s exposing immutable `StateFlow<UiState>` | Keeps form, loading, error, and one-off effect state out of composables. |
| Domain | Kotlin use cases plus pure rule services | Period, scoring, streak, and badge rules can be deterministically unit-tested. |
| Persistence | Room database | Habit, immutable history, and finalization must be atomically durable across restarts. |
| Preferences | DataStore only for small non-relational settings, if later needed | It is not the source of truth for habits or points. |
| Background work | WorkManager best-effort reconciliation; launch/resume reconciliation is authoritative | Android cannot guarantee exact-time background execution; the PRD requires elapsed periods to finalize on next app use. |
| Notifications | NotificationCompat with a deep link to habit review | Makes one missed-result notification actionable; in-app history remains the fallback. |
| Time | `java.time` local dates and weekday calculations, with core-library desugaring for API 24–25 | Preserves calendar semantics without time-of-day/DST arithmetic. |
| Navigation | Navigation Compose with stable habit IDs as arguments | Separates overview, form, and review states without passing domain objects between destinations. |

Dependencies and Gradle settings needed for these choices (Room, lifecycle
ViewModel Compose, Navigation Compose, WorkManager, DataStore, core-library
desugaring, and AndroidX notification support) are implementation work; they
are not changed by this document.

## System shape

```text
Compose screens
    │ user events / rendered UiState
    ▼
ViewModels ───────────────► navigation and transient UI effects
    │ calls
    ▼
Use cases / domain rule services
    │ transactions and queries
    ▼
Repository ───────────────► Room database
    │                              │
    ├────────► Notification gateway │ durable product truth
    └────────► WorkManager ◄────────┘
                  reconciliation trigger
```

Dependency direction is always inward: UI depends on presentation and domain
contracts; data implements repository contracts; domain does not depend on
Android, Room, Compose, or notification APIs.

## Package layout

Use feature-oriented packages under
`com.codetutor.samplehabittrackerapp`. Exact filenames can evolve, but these
boundaries should remain stable.

```text
app/
  HabitTrackerApplication.kt
  navigation/
    HabitTrackerNavHost.kt
  core/
    time/Clock.kt, LocalDateProvider.kt
    ui/UiText.kt
  feature/overview/
    OverviewScreen.kt, OverviewViewModel.kt, OverviewUiState.kt
  feature/habitform/
    HabitFormScreen.kt, HabitFormViewModel.kt, HabitFormUiState.kt
  feature/review/
    HabitReviewScreen.kt, HabitReviewViewModel.kt, HabitReviewUiState.kt
  feature/deletion/
    DeleteHabitViewModel.kt
  domain/
    model/
    repository/HabitRepository.kt
    usecase/
    rules/OccurrencePlanner.kt, FinalizationEngine.kt, ScoringEngine.kt,
      ContinuityCalculator.kt, BadgeEvaluator.kt
  data/
    local/HabitTrackerDatabase.kt, dao/, entity/
    repository/RoomHabitRepository.kt
    notification/MissedTargetNotifier.kt
    worker/FinalizeElapsedPeriodsWorker.kt
```

Small apps do not need separate Gradle modules. The `domain` package remains
Android-free so it can be unit-tested quickly. A simple application-level
composition root constructs repositories, rule services, workers, and
ViewModels; introducing a dependency-injection framework is optional, not an
architectural requirement for V1.

## Domain model and persistence design

### Source-of-truth principles

- `Habit` stores only mutable current configuration and lifecycle state.
- Each occurrence stores the target effective for that occurrence. A later
  target edit can therefore never rewrite completed history.
- Finalization, result, points effect, and continuity values are committed in
  one database transaction. This is the exactly-once boundary.
- The app-wide points balance is a stored aggregate updated in that same
  transaction. `pointsEffect` in occurrence history provides an audit trail.
- Decimal quantities use a canonical string/decimal representation or scaled
  integer, never binary `Double`. Target and progress values must compare
  exactly.

### Tables

| Table | Key fields | Purpose |
| --- | --- | --- |
| `habits` | `habitId`, name, unit, frequency, selectedWeekdays, creationLocalDate, status | Current editable habit configuration and active/pending-deletion state. Unit and schedule are immutable after creation. |
| `occurrences` | `occurrenceId`, `habitId`, startLocalDate, endLocalDate, effectiveTarget, progress, status, finalizedAt, result, pointsEffect | One scheduled period. An open row accepts replacement progress; a finalized row is immutable. Unique `(habitId, startLocalDate)` prevents duplicate outcomes. |
| `target_changes` | `changeId`, `habitId`, requestedTarget, effectiveStartLocalDate, changedAt | Chronological review of target changes. The occurrence snapshot is the authoritative historical target. |
| `app_state` | singleton ID, pointsBalance | The durable app-wide balance, initialized to zero. |
| `badges` | `badgeId`, `habitId`, type, awardedForLocalDate, awardedAt | One award record; unique `(habitId, type)` enforces the one-time Daily badge. |
| `deletion_tombstones` | `habitId`, expiresAtInstant | Recoverable deletion state until its 10-second expiry. Associated history remains present until final deletion. |
| `notification_events` | `occurrenceId`, attemptedAt, deliveryState | Records the single notification attempt for a missed occurrence and supports the in-app fallback state. |

Store local calendar dates as ISO-8601 `LocalDate` values and boundary moments
as instants only where elapsed time matters (for example, undo expiry). Avoid
persisting a zone as the source of truth for an occurrence: future scheduling
uses the device's current zone, while finalized rows already contain their
immutable outcome.

### Entity states

```text
Habit: ACTIVE ──confirm delete──► PENDING_DELETION ──undo──► ACTIVE
                                      │
                                      └─expiry──► removed with related history

Occurrence: OPEN ──local period has elapsed──► FINALIZED_SUCCESS | FINALIZED_MISS
```

`PENDING_DELETION` habits are excluded from the active overview and new
progress actions, but are not physically removed until expiry. A transaction
that finalizes deletion removes the habit, occurrences, target changes, badge,
tombstone, and notification-event records. It deliberately does not alter
`app_state.pointsBalance`.

## Scheduling and finalization

### Occurrence planner

`OccurrencePlanner` is a pure service. It receives a habit, the device's
current local date, and the current zone only to determine the local date. It
produces applicable occurrences using these rules:

| Frequency | First occurrence | Subsequent occurrence |
| --- | --- | --- |
| Daily | Creation date | Each local day |
| Weekly | Next Monday after creation; first full Monday–Sunday week | Each Monday–Sunday week |
| Fortnightly | Creation date | Consecutive 14-day blocks |
| Monthly | First day of next month | Each local calendar month |
| Custom | Selected weekday on or after creation | Every selected weekday; each is a one-day occurrence |

No row is created for an unselected Custom weekday. This ensures it cannot
silently generate a progress request, miss, points change, or streak event.
The planner creates needed open occurrences idempotently before the overview
or a habit review is queried.

### Reconciliation sequence

Run the same `ReconcileTracking` use case when the application enters the
foreground, when the overview is opened, and from a periodic/best-effort
WorkManager worker. Foreground execution owns PRD correctness; worker timing
only improves timeliness.

1. Read the current device local date and load active habits in a transaction.
2. Create any applicable occurrence rows through today, subject to first-period
   rules.
3. Find open rows with `endLocalDate < today`; their local period has elapsed.
4. Finalize each eligible row deterministically, ordered by end date then
   habit ID. Progress at least equal to the effective target succeeds;
   otherwise it misses.
5. For each row, calculate exactly one points effect, update its result and
   finalization timestamp, update `app_state`, calculate continuity data, and
   evaluate the badge in the same transaction.
6. After commit, attempt one notification for every newly finalized miss. Save
   the attempt result. The committed missed row itself is the in-app fallback.

The `OPEN → FINALIZED_*` update must predicate on `status = OPEN`; if it
updates zero rows, another invocation already finalized it. Combined with the
unique occurrence key and single transaction, this makes repeated starts,
worker retries, and process death safe.

### Rule services

| Service | Inputs | Output / invariant |
| --- | --- | --- |
| `ProgressValidator` | submitted decimal | Non-negative canonical value; invalid input is not persisted. |
| `TargetPolicy` | frequency, current date, requested target | Daily/Custom apply to current open occurrence; Weekly/Fortnightly/Monthly begin next occurrence. Never changes finalized rows. |
| `ScoringEngine` | result, points balance before result | Success: `+10`; miss: `-ceil(balance × 0.01)`, minimum `-1` when balance is positive, never below zero. |
| `ContinuityCalculator` | ordered finalized occurrences | Active run is scheduled periods since creation while active; success streak is consecutive successful finalized periods and resets on miss. |
| `BadgeEvaluator` | Daily finalized history | Award once if any 21 consecutive local dates contain at least 18 successful daily occurrences; absent/non-qualifying days count as misses. |

Calculate Active run from schedule/lifecycle rather than treating it as a
mutable counter. This avoids drift when reconciliation catches up multiple
periods. The overview can derive it from the planner/current date; it is not
shown for pending or finally deleted habits. Success streak may be stored as a
denormalized field for query speed only if it is recalculable from immutable
history.

## Presentation and navigation

The minimum destination graph is:

```text
Overview ──create──► Create habit
    │                    │ save
    ├──select habit──► Habit review
    └──edit/delete──► Edit habit / delete confirmation
```

`OverviewUiState` contains the points balance, no-habits state, active habit
summaries, and missed-result fallback indicators. `HabitFormUiState` preserves
all valid field values while displaying field-specific validation errors.
`HabitReviewUiState` contains chronological finalized occurrences, target
changes, and the badge state. UI effects such as “progress saved,” navigation,
and the visible undo countdown are emitted separately from durable UI state.

Composables render state and send events only; they do not calculate schedule,
points, or finalization rules. Give every actionable control and outcome a
semantic label/state description, show textual/iconic success/miss status in
addition to color, and allow layouts to reflow under system font scaling.

## Notifications and deletion recovery

After a committed missed finalization, `MissedTargetNotifier` creates one
notification keyed by occurrence ID. The pending intent navigates to that
habit's review route. On Android versions requiring runtime notification
permission, a denied or unavailable notification is recorded as unavailable;
the overview/review surfaces the same missed result, satisfying the fallback.

Deletion is not delegated solely to an in-memory snackbar timer. On confirmed
delete, persist the tombstone and `PENDING_DELETION` state with an expiry
instant, then show the 10-second undo affordance. On app foreground, worker
run, or expiry callback, `FinalizeExpiredDeletions` removes expired tombstones
and related habit data atomically. Undo before expiry atomically restores
`ACTIVE` and deletes the tombstone. This remains correct through process death
or restart.

## Error handling, privacy, and operational limits

- Repository failures become recoverable user-facing states; they never claim
  a save succeeded before its transaction commits.
- Invalid name, unit, target, progress, and Custom weekday input stays in the
  form and is identified at the relevant field.
- There are no network clients, accounts, analytics requirements, cloud sync,
  exports, or remote APIs in V1. No permission is needed other than notification
  permission where Android requires it.
- Database migrations must be versioned and tested before a schema change ships.
  Never migrate by discarding user history.
- The app makes no promise that the OS will run work at the exact calendar
  boundary; reconciliation on next use guarantees correct durable outcomes.

## Requirement-to-component traceability

| PRD requirement | Primary architecture responsibility |
| --- | --- |
| FR-001, FR-003, FR-005, FR-012 | Habit form/progress ViewModels, validators, `TargetPolicy`, Room transactions |
| FR-002, FR-004, FR-011 | `OccurrencePlanner`, `ReconcileTracking`, Room unique keys and transactions |
| FR-006, FR-007 | Overview/review ViewModels and read models sourced from Room |
| FR-008 | `ScoringEngine`, `ContinuityCalculator`, `BadgeEvaluator`, `app_state`, `badges` |
| FR-009 | `MissedTargetNotifier`, `notification_events`, overview/review fallback indicators |
| FR-010 | Deletion ViewModel, `deletion_tombstones`, `FinalizeExpiredDeletions` |

## Verification strategy

Prioritize unit tests for pure rules: every cadence's first and subsequent
period, Custom non-occurrence days, target effective dates, exact decimal
comparisons, scoring rounding/floor behavior, streak/run transitions, and all
21-day windows. Add repository integration tests for transaction idempotency,
restart persistence, finalized-row immutability, and deletion/undo expiry.
Compose UI tests should cover validation retention, accessible states, saved
feedback, empty overview, and navigation to review. Device/emulator tests
should cover notification-permission denial and deep-link behavior.

## Explicitly deferred implementation choices

- Exact visual design, copy, charting, and animations.
- A dependency-injection library versus a small manual composition root.
- Encryption at rest or PIN protection, pending a creator-approved threat model.
- Background-work cadence, provided it remains an optimization rather than the
  correctness mechanism.
