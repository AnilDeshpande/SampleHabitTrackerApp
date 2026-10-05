# Habit Tracker V1 — Android Low-Level Design

## Metadata, authority, and scope

| Field | Value |
| --- | --- |
| Status | Accepted Android design; OQ-001 through OQ-008 are resolved. |
| Date | 2026-10-05 |
| Product source | [Habit Tracker V1 PRD](../prds/habit-tracker-v1-prd.md), including September 2026 amendments. |
| Architecture baseline | [Habit Tracker V1 architecture](../architecture/habit-tracker-v1-architecture.md), September 23 delta. |
| Durable tracking contract | [ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md); ADR 0001 remains superseded. |
| Wiring decision | [ADR 0003](../adr/0003-manual-composition-root-and-android-dependency-wiring.md). |
| Platform | Kotlin, Jetpack Compose, Material 3; one `:app` module; package `com.codetutor.samplehabittrackerapp`; minimum API 24. |
| Source provenance | Creator-directed Android design and decision-resolution rounds on 2026-10-05; current decisions are incorporated below. |

This document complements the architecture with Android screen-state,
cancellation, dependency-construction, time-control, and testing contracts.
It does not replace the architecture's schema or domain rules. Application code,
dependencies/SDK changes, and changes to the PRD or accepted tracking ADRs are
outside this documentation pass.

### Decision lock

The creator authorized these resolutions on 2026-10-05.

- **Verified product truth:** retain approved input/late-save behavior,
  retirement with ten-second undo, immutable history, deterministic scoring,
  notification acknowledgement/fallback, and original-device-only storage.
- **Inherited decisions:** Room truth, global gated catch-up, stored occurrence
  boundaries, retirement freeze, outbox states/attempt limits, foreground
  ticker, best-effort WorkManager, and DataStore's permission-requested flag.
- **Resolved technical choices:** destination-scoped SavedStateHandle inputs;
  application-owned commands; command/target admission keys released at commit
  or failure; separate application jobs for after-commit effects; undo's
  conditional restore before catch-up; accepted foreground notification cleanup
  for non-active habits or zero misses; tag plus exact Int notification identity;
  manual lazy container/factories; real database seams and shared virtual time;
  Robolectric host tests plus connected API 24 and managed API 36 instrumented
  evidence; six-hour unconstrained periodic catch-up with no retry, and default
  30-second exponential dispatch backoff bounded by persisted outbox attempts.
- **Accepted process-death behavior:** committed transactions remain saved;
  unfinished transactions do not. No create draft token: re-saving the unchanged
  restored draft after a committed create returns the existing duplicate-name
  error. V1 needs no durable rejection receipt or automatic command replay.
- **Safeguards and verification:** the four design sections end with required
  verification, including slow-cancellation versus undo, both crash windows,
  overflow/tag isolation, shared occurrence identity, factory restoration,
  real triggers/gates, migration checks, minimum-API SQL evidence, and worker
  timing/attempt-budget checks. Registers suffice; no additional
  diagram is required.
- **ADR/source impact:** ADR 0003 still records wiring; the architecture and
  ADRs are unchanged. C-001 contains a note for the architecture owner about
  ADR 0002's imprecise general ordering language; the specific freeze/undo rule
  governs implementation under the creator's decision.
- **Consistency status:** C-001, C-002, and C-003 are resolved. The progress route,
  saved occurrence ID, command payload, package boundaries, and test graph agree.
  No product-definition blocker remains for this scope.
- **Product questions outside this design:** quiet hours require a separate
  product decision; this design adds no quiet-hours policy or suppression rule.
- **Deferred:** no low-level design question remains open. The creator approved
  a connected API 24 emulator/device and a Gradle Managed Device at API 36,
  preserving both required instrumented tiers. Application/build implementation
  and amendments to source architecture/ADRs are outside this documentation change.

`Unclassified architecture questions: None`.

### Requirement coverage

| Design responsibility | Product requirements |
| --- | --- |
| Screen-state ownership and restoration | FR-001, FR-003, FR-005–FR-007, FR-010–FR-012 |
| Concurrency and work lifetime | FR-001, FR-003–FR-005, FR-009–FR-012 |
| Composition root and dependency wiring | Cross-cutting realization of FR-001–FR-012; especially foreground correctness and platform recovery |
| Test seams | FR-001–FR-005, FR-008–FR-011 database/rule invariants; FR-006, FR-007, FR-012 UI mapping checks |

## Screen-state ownership and restoration

### Ownership rules

Every screen-state value belongs to exactly one class. `UiState` is a read
projection of these owners, not a second writable store.

| Class | Owner and restoration contract |
| --- | --- |
| Durable | Room owns saved habits, lifecycle/deadlines, occurrences, finalized results, target-change history, points, badges, and acknowledgements. Reload after recreation; never copy them into saved state. |
| Restorable input | The destination-scoped ViewModel's `SavedStateHandle` owns unsaved business input and its minimal restoration metadata. Each input event writes its key immediately; expose handle-backed flows rather than an independent mutable draft. |
| Derived | Recompute screen models, validation/errors, current-period eligibility, late-submission state, countdowns, and control availability from Room and restorable inputs, with the current clock/locale and operation status where needed. Saving state observes the app-scoped command runner, including after screen reopening. Recompute loading and repository-error presentation from new reads/current operations; never restore an in-flight operation after process death. |
| Transient effect | One-off navigation, snackbars/toasts, saved-action messages, permission-launch requests, and focus/keyboard requests are emitted for current events and never restored or replayed. |
| Visual-only | Compose `rememberSaveable` owns small presentation choices such as scroll position, expansion, and cursor selection. None may determine a business command's payload. |

Business input belongs in `SavedStateHandle`, never `rememberSaveable`. This
gives each draft one owner and allows ViewModel unit tests without Compose.
ViewModels alone retain memory across configuration changes but cannot retain
it across process death. This ownership follows [Android's Compose state-saving
guidance](https://developer.android.com/develop/ui/compose/state-saving).

### Screen coverage

| Screen / ViewModel | State classification |
| --- | --- |
| Create/Edit Habit — `HabitFormViewModel` | Raw name, unit, target, frequency, weekdays, and edit-initialization marker: restorable input. Saved habit: durable. Form mode from route, validation, edit eligibility, and effective-period preview: derived. Save feedback/navigation/permission launch: transient effect. Scroll, cursor, and dropdown expansion: visual-only. |
| Record Progress — `RecordProgressViewModel` | Raw typed total and its occurrence ID: restorable input. Persisted progress and occurrence boundaries/results: durable. Target/end display, field error, current eligibility, and late-submission result: derived. Save feedback/navigation: transient effect. Cursor and sheet scroll: visual-only. |
| Overview — `OverviewViewModel` | Persisted points, habits, retirement status/deadlines, badges, and miss acknowledgements: durable. Summaries, no-habits state, `CurrentStatus`, Active run, Success streak, miss counts, and undo countdown: derived. Navigation and action messages: transient effect. List scroll: visual-only. |
| Habit Review — `HabitReviewViewModel` | Finalized occurrences, target changes, badges, and acknowledgements: durable. Chronological rows, superseded-change labels, and `canTrack`: derived. Navigation: transient effect. Scroll and expanded history rows: visual-only. |
| Retirement confirmation/pending row — `RetireHabitViewModel` | Habit lifecycle, frozen occurrence, and retirement deadline: durable. Confirmation content, action eligibility, and remaining time: derived. Confirm/Undo results and navigation: transient effect. Dialog visibility: visual-only; restoration never executes confirmation. |
| Retired Habits — `RetiredHabitsViewModel` | Retired habit records: durable. List/empty state and read-only routing eligibility: derived. Navigation: transient effect. List scroll: visual-only. |

`habitId` comes from the navigation route for habit-specific destinations. The
navigation back stack supplies screen identity; do not maintain a second draft
copy of `habitId`. Use the destination's saved-state-aware ViewModel factory;
constructing a fresh empty handle in production would bypass restoration.

Record progress is its own Navigation Compose destination,
`record-progress/{habitId}`, even if visually presented as a sheet. Its
ViewModel and SavedStateHandle belong to that destination's back-stack entry,
not the overview/review ViewModel or a composable's temporary scope. This
refines the architecture's sheet navigation; UI presentation may remain a sheet.

### Navigation route register

Routes are declared in `navigation/HabitTrackerNavHost.kt`. Habit arguments are
positive `Long` IDs validated at the route boundary; mode comes from the route.
Every ViewModel uses that destination's back-stack entry and factory. No route
carries a Room entity, form draft, or occurrence object.

| Destination / entry | Route or URI | State owner and navigation contract |
| --- | --- | --- |
| Overview | `overview` | `OverviewViewModel`; start destination and notification back-stack parent. |
| Create Habit | `habit/create` | `HabitFormViewModel` in create mode; no habit ID. Save/cancel returns to overview; opening the destination inserts no row. |
| Edit Habit | `habit/edit/{habitId}` | `HabitFormViewModel` in edit mode; route ID plus `form.initialised` protect restored edits. Save/cancel returns to the invoking overview. |
| Record progress | `record-progress/{habitId}` | `RecordProgressViewModel`; a distinct destination that may render as a sheet. Its handle retains `progress.typed` and `progress.occurrenceId`. Save/dismiss follows the architecture's return to overview; a late rejection keeps the destination/text available for deliberate correction. |
| Habit Review | `habit-review/{habitId}` | `HabitReviewViewModel`; entered from overview or retired habits. Acknowledge misses on opening; tracking controls require `ACTIVE`. |
| Retirement confirmation | `retirement-confirm/{habitId}` | `RetireHabitViewModel`; dialog destination. Confirmation returns to overview's durable pending row, cancellation returns without submitting a command. |
| Retired Habits | `retired-habits` | `RetiredHabitsViewModel`; opens read-only review for the chosen route ID. |
| Notification deep-link entry to Review | `habittracker://habits/{habitId}/review` | Explicit immutable PendingIntent to `MainActivity`; resolves to `habit-review/{habitId}` with Overview → Review back stack on cold or warm entry. Back returns to overview. No new exported intent filter. |

The pending-retirement row is inline overview content, not a destination;
its Undo event submits `retirement.undo:{habitId}`. Navigating away never owns
or cancels a submitted command. The notification entry and in-app review route
select the same habit and use the same acknowledgement/read-only policy.

### Saved-state key register

These are the complete application-owned keys, excluding navigation arguments.
Defaults apply only to absent keys; an intentionally empty restored field must
not be replaced with a default or a Room value.

| ViewModel | Key | Type and contract |
| --- | --- | --- |
| `HabitFormViewModel` | `form.name` | `String`: raw field text. |
| `HabitFormViewModel` | `form.unit` | `String`: raw field text; read-only in edit mode. |
| `HabitFormViewModel` | `form.target` | `String`: exact raw text, including invalid input such as `1,2.3`; never a saved `BigDecimal`. |
| `HabitFormViewModel` | `form.frequency` | `String`: frequency enum name; read-only in edit mode. |
| `HabitFormViewModel` | `form.weekdays` | `Int`: same mask as `habits.selectedWeekdays`, Monday bit 0 through Sunday bit 6. Monday/Wednesday/Friday = `21`; non-Custom = `0`. |
| `HabitFormViewModel` | `form.initialised` | `Boolean`: edit-mode initialization guard; absent means `false`. Set `true` after the initial Room-to-draft load completes. |
| `RecordProgressViewModel` | `progress.typed` | `String`: exact raw text, including invalid input. |
| `RecordProgressViewModel` | `progress.occurrenceId` | `Long`: the occurrence this draft belongs to; absent until a first current occurrence is loaded. |
| `OverviewViewModel` | None | No unsaved business input; all content is reloaded or derived. Scroll belongs to Compose. |
| `HabitReviewViewModel` | None | Route `habitId` selects Room history; rows and controls are rebuilt. Scroll/expansion belong to Compose. |
| `RetireHabitViewModel` | None | Route identifies the habit; Room owns confirmed retirement/deadline. Confirmation visibility is visual-only. |
| `RetiredHabitsViewModel` | None | Room supplies the list; no draft input. Scroll belongs to Compose. |

Keep saved state tiny: short field strings and primitive metadata only (the
weekday `Int`, occurrence `Long`, and initialization `Boolean`). Never save
entities, lists, history, errors, `BigDecimal`, or complete `UiState` objects.
Reload large content from Room. Android's bundle-backed saved state is intended
for [small restoration inputs](https://developer.android.com/develop/ui/compose/state-saving).

### Initialization and restoration

In create mode, restore existing keys before applying defaults to missing
keys. The unsaved Meditation draft restores name `Meditation`, unit `minutes`,
target text `10`, Custom frequency, and weekday mask `21`; no habit is inserted.

In edit mode, load the route's habit from Room. Only when
`form.initialised != true`, seed its editable draft fields and immutable unit,
frequency, and weekdays, then set `form.initialised = true`. Keep editing
disabled until this first initialization completes. When the flag is already
true, Room refreshes durable status and eligibility but must never overwrite
restored draft fields, including empty or invalid values. Repeated Room
emissions cannot reseed the draft. A retired or missing habit cannot become
editable merely because a draft was restored.

On restoration, run `HabitInputValidator` again on raw text and the current
locale. Validation messages are derived, not saved. Thus `1,2.3` reappears
unchanged with its recomputed error; trimming, parsing, and normalization belong
at validation/save boundaries, not in the saved draft.

For progress, initialize raw text and occurrence ID together on first load,
then retain both. Before declaring a restored draft eligible, await catch-up
and reload the habit's current occurrence. Compare it with
`progress.occurrenceId`, verifying that the saved occurrence belongs to the
route's habit. If it is still current, `OPEN`, and the habit is `ACTIVE`,
revalidate the restored raw text and allow submission. If it finalized while
the process was dead, load that occurrence's final result and show the
architecture's late-submission state while retaining `progress.typed`. Never
silently assign the old text to a new occurrence; recording it for the new
period requires deliberate user action. Missing/cancelled occurrences and
non-active habits expose the existing unavailable/read-only state. Actual
`SaveProgress` still performs transactional catch-up and eligibility checks.

Restoration does not repeat saves, navigation, snackbars, permission launches,
or other one-off effects. An operation interrupted by process death is resolved
by re-reading durable state, not by restoring its coroutine or success effect.

Saved state restores a retained task after system-initiated process death. It
does not retain drafts or visual state after the user swipes the task away from
Recents; that loss is intended. Committed Room data remains. Write draft keys
on input events, before backgrounding: saved state is captured through the host
Activity lifecycle, and writes made while already stopped are not guaranteed
to be captured until another start/stop cycle. See [SavedStateHandle's task and
lifecycle contract](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate).

### Verification obligations

Robolectric host ViewModel tests in `app/src/test` use fresh instances constructed
with pre-filled `SavedStateHandle`s, fake repositories, and controlled time/locale:

- Create: restore the complete Meditation draft and mask `21`; verify raw
  invalid target `1,2.3` remains unchanged with a recomputed error. A real-command
  test commits a create, recreates the form from the unchanged saved draft,
  resubmits, and checks duplicate-name validation with no second habit row.
- Edit: set `form.initialised = true` and draft values different from Room;
  verify initial and subsequent Room emissions preserve the edits, including
  an empty field. With the flag absent/false, verify one-time seeding and the
  transition to `true`.
- Progress: restore raw input and occurrence ID for both a still-current
  occurrence and one finalized during absence. Verify the latter shows its
  final result, retains text, and cannot submit it to the new occurrence.
- Overview, Review, Retirement, and Retired Habits: build fresh ViewModels with
  route arguments where applicable and no application-owned saved keys; verify
  Room rebuilds summaries/history, read-only policy, and persisted retirement
  countdown. Observe no replayed one-off effects in any restoration case.

Device checks run at API 24 and API 36 using the custom test Application where
needed. Enter the unsaved draft, background the app and allow it to reach
the stopped state, then run:

```bash
adb shell am kill com.codetutor.samplehabittrackerapp
```

Verify the old process ended, then reopen the retained task from Recents.
Assert the same destination and all draft fields return. Repeat for invalid
input, unsaved edits, and progress whose occurrence closes during absence.
Separately swipe away the task and verify a fresh launch has no restored draft
while Room data remains. Use `am kill` for the background-process check;
`force-stop` and rotation exercise different lifecycle cases. The [ADB activity
manager documentation](https://developer.android.com/tools/adb) defines `kill`
as killing processes safe to kill without affecting user experience.

Verify every route creates its own ViewModel/handle, and both cold and warm
notification entry build the Overview → Review back stack and acknowledge the
same habit. Recreate Record progress and assert the saved occurrence ID is the
one sent to `SaveProgress`; never substitute a newly current occurrence.
These are required implementation checks, not tests executed by this
documentation change.

## Concurrency and work lifetime

### Scope register and call pattern

| Work | Lifetime owner | Cancellation contract |
| --- | --- | --- |
| Observe screen state | Compose `collectAsStateWithLifecycle` | Collection stops with the UI lifecycle; it does not own writes. |
| Screen-only loading, deriving, and validating | `viewModelScope` | Cleared with the ViewModel. CPU-heavy work uses injected `default`; no user-committed transaction belongs here. |
| Accepted `CreateHabit`, `EditHabit`, `SaveProgress`, `ConfirmRetirement`, `UndoRetirement`, and `AcknowledgeMisses`, plus their after-commit effects | Injected application scope owned by AppContainer: `SupervisorJob` plus injected dispatcher | Survives screen departure and process `STOPPED`; ends with the process. Failure of one job must not cancel unrelated commands. |
| Await command result and update the screen | `viewModelScope` | Leaving cancels the wait and drops transient UI feedback, never the accepted command. |
| Foreground catch-up, notification dispatch, and cleanup | Application scope, requested by one process-lifecycle observer | No Activity `onStart`/`onResume` hooks. One foreground pipeline at a time. |
| BoundaryTicker | Application scope; ticker job active only while process is `STARTED` | Stop/re-arm the ticker with the process lifecycle; keep accepted commands running. |
| Persisted, deferrable reconciliation and dispatch/retry | WorkManager | Worker execution obeys WorkManager cancellation; durable rows make retries safe. It is not detached into an untracked application job. |

`CommandRunner.submit(key, immutableRequest)` atomically reserves the key and
starts an application-owned transaction job before returning a result ticket.
The ViewModel awaits that ticket from `viewModelScope`; leaving cancels only the
wait. Runner-owned transaction completion releases the key in `finally` after
Room commits or fails, including validation/rejection results. It then schedules
any effects of a committed transaction as separate application jobs holding no
admission key; even a rejected late save can commit catch-up/outbox changes.
No effect runs for a rolled-back transaction. The result ticket represents the
transaction result, not completion of Android notification work.

### Command admission key register

| Command | Key | Input identity |
| --- | --- | --- |
| `CreateHabit` | `habit.create` | Immutable submitted draft; one app-wide create slot, no draft token. |
| `EditHabit` | `habit.edit:{habitId}` | Route habit plus submitted edits. |
| `SaveProgress` | `progress.save:{habitId}` | Route habit, saved `progress.occurrenceId`, and raw `progress.typed`. |
| `ConfirmRetirement` | `retirement.confirm:{habitId}` | Route habit. |
| `UndoRetirement` | `retirement.undo:{habitId}` | Route habit; persisted deadline decides timeliness. |
| `AcknowledgeMisses` | `misses.ack:{habitId}` | Review route habit. |

Substitute the same canonical decimal habit ID into keys on each screen entry.
Runner busy state is process-local and observable outside screens. Reopening
observes its command's key and disables Save only while that transaction is
in flight. Duplicate taps for an occupied key are rejected before recomposition.
Commit/failure frees the key even if notification cancellation is still running.
Confirm and Undo have different keys: database gate/snapshot/deadline predicates,
not admission, decide their outcomes. Different command keys never replace the
transaction's serialization and eligibility checks.

The runner alone owns transaction/effect jobs; both are children of the injected
application scope, and an effect job is not a child of the transaction job or
the screen's waiting coroutine. Cancelling a waiter must not cancel either.
A caller-scoped `withContext(io)` still inherits cancellation
and is insufficient. Handle ordinary failures inside each application job even
if no waiter remains; `SupervisorJob` does not handle exceptions by itself.
This follows Android's [external-scope and cancellation guidance](https://developer.android.com/kotlin/coroutines/coroutines-best-practices).

No extra SavedStateHandle key or create draft token is used. If Android ends the
process after a create commits, re-saving the same restored draft hits the
existing duplicate-name error. This is accepted V1 behavior rather than an
exactly-once admission guarantee across process death.

### Transaction, input identity, and results

Inject `io`, `default`, and `main` through a dispatcher holder. Only the
production composition root selects concrete `Dispatchers`; classes do not
hard-code them. Database acquisition/opening and blocking platform I/O use
`io`, substantial pure computation uses `default`, and UI/lifecycle adapter
operations use `main`. Do not use `GlobalScope` or `allowMainThreadQueries`.
Keep the existing transaction's database work in Room's transaction context.

Use Room's suspending `withTransaction`. For `CreateHabit`, `EditHabit`,
`SaveProgress`, and `ConfirmRetirement`, acquire the database gate first,
capture the TimeSnapshot after acquisition, reconcile all habits, then perform
the command-specific conditional write. `ReconcileTracking` uses that gate and
snapshot before its own planning/finalization. Acknowledgement retains its
existing single transaction without the reconciliation gate.

`UndoRetirement` has the specific order: gate → snapshot → conditional
`PENDING_RETIREMENT → ACTIVE` when `now < deadline`, clearing the deadline →
`ReconcileTracking` for all habits in that same transaction. If the condition
fails, that catch-up finalizes expired retirement and the command reports late
undo. Restoring before catch-up lets a frozen occurrence that elapsed during
the undo window finalize immediately; reconciling only before restoration would
leave that occurrence open. This is the architecture's order and the specific
ADR 0002 freeze/resume rule. Treat its generic catch-up-before-write list as
imprecise, as recorded for the architecture owner in resolved C-001.

No notifier or WorkManager enqueue occurs inside the transaction. Never swallow
`CancellationException` in `catch (Exception)` or `runCatching`; rethrow it
before handling ordinary failures. See [Room's transaction
contract](https://developer.android.com/reference/androidx/room/RoomDatabaseKt).

`SaveProgress(habitId, occurrenceId, rawText)` takes an immutable copy of the
route habit, `progress.occurrenceId`, and `progress.typed` when admitted.
After catch-up it must verify that this exact occurrence belongs to the habit,
is still `OPEN` and current at the transaction snapshot, and the habit is
`ACTIVE`. If the user typed before midnight but execution acquires the gate
after midnight, reject that old occurrence with its finalized result. Never
substitute whichever occurrence is current at execution. Validate raw text with
the shared validator; conversion to exact stored decimal happens at the save
boundary, not by changing the draft.

The application scope does not survive process death. Accepted behavior is
atomicity without an automatic retry of the user's unfinished command:

- Death before commit: the transaction's writes are not saved; no partial
  habit/progress/points/outbox changes survive.
- Death after commit, before result delivery: the changes remain saved, even
  though the screen never received success. Reload Room to resolve the outcome.
- Death after commit, before an external effect: the durable tracking result
  remains; outbox retry handles eligible posting work and the accepted
  foreground cleanup recovers visible ineligible notifications on next foreground.

Thus "an in-flight save is simply not saved" applies only before commit. The
UI cannot reliably distinguish these cases from an absent result, and must
never claim success before commit or automatically replay an ambiguous create.
A deliberate re-save uses the accepted duplicate-name behavior above.
The recovery boundary follows SQLite's [atomic-commit
contract](https://sqlite.org/atomiccommit.html).

If the screen has left, omit transient "Progress saved" feedback. The overview
and review rebuild persisted progress and closed-period results, so a late save
cannot erase or conceal its period's outcome. The existing schema does not
record that a particular attempted command was rejected; it records the final
period result. V1 deliberately has no durable rejection receipt: the final
period result and restored draft are sufficient. Do not add a receipt table,
replay an interrupted command, or represent an ephemeral ticket as durable evidence.

### After-commit effects and foreground recovery

Posting/updating/cancelling notifications and enqueueing unique work must be
idempotent using stable platform identities. Retain the architecture's
conditional outbox updates and cancellation checks. Once a transaction commits,
an effect failure cannot turn its result into "save failed" or roll it back;
retain the committed result and use the defined durable retry/fallback path.
After-commit jobs hold no command key and enter the same serialized dispatcher
for notification operations, including confirmation/acknowledgement cancellation.

Install one `ProcessLifecycleOwner` observer for this process. Its `ON_START`
launches the application-owned sequence: ensure periodic work is scheduled,
await catch-up, then within the serialized dispatcher drain pending notifications
and clean up visible ineligible notifications; finally re-arm the ticker.
Foreground transitions and ticker wakes share the existing database gate and
serialized dispatcher.
`ON_STOP` stops the ticker and unregisters its receiver, without cancelling
accepted commands. Rotation and screen navigation do not install observers or
start independent foreground pipelines. [ProcessLifecycleOwner](https://developer.android.com/reference/androidx/lifecycle/ProcessLifecycleOwner)
filters configuration-change transitions and is not an exact-time scheduler.

The architecture's overview-open catch-up trigger remains: opening the overview
requests reconciliation through this same application-owned coordinator. It
does not register another observer or depend on Activity lifecycle callbacks.

### Accepted foreground cleanup — AND-003

On every app foreground, inside the serialized dispatcher, enumerate visible
app-owned missed-result notifications through the notifier port. Re-read Room
eligibility and cancel a notification if its habit is not `ACTIVE` **or** its
unacknowledged-miss count is zero. Use the same suppression predicate as the
outbox and keep its post-side eligibility recheck. This covers process death
between committed acknowledgement/retirement and Android cancellation,
including retirement with positive unacknowledged misses. Repeat cleanup safely;
it changes no results, acknowledgement timestamps, outbox state definitions,
or schema. Cleanup is accepted behavior, not a proposal.

All posts/updates/cancels, including separate after-commit jobs, use the shared
notification serializer. Recheck eligibility at the effect boundary so a stale
read cannot permanently remove a newer eligible notification; a newly committed
miss's dispatch runs through that same serializer and the existing outbox checks.
Cleanup recovers on foreground; it promises no work while the process is dead.

### Notification identity contract

Post/update with `tag = "habit-missed-result"` and
`id = Math.toIntExact(habitId)`; cancel with that exact tag/ID pair. Do not hash,
truncate, wrap, or use untagged cancellation. Overflow fails fast with
`ArithmeticException`, before any platform notification call; it is an invariant
failure rather than a transient posting error to retry. The creator accepts
that V1-scale IDs fit Int; the exact conversion makes that assumption explicit.
See [Math.toIntExact](https://developer.android.com/reference/java/lang/Math#toIntExact(long)).

The Android notifier calls `NotificationManager.getActiveNotifications()` and
filters by the exact tag. The notification's `id.toLong()` identifies its habit
without a lookup table or hash collision. Re-read that habit and miss count;
a missing habit cannot be `ACTIVE`. Notifications with other tags are untouched.
The API was introduced at API 23 and is available at minimum API 24; see
[active notification enumeration](https://developer.android.com/reference/android/app/NotificationManager#getActiveNotifications()).

### Verification obligations

Start `SaveProgress` through a real command runner, hold its transaction at a
controlled barrier, clear the ViewModel through its ViewModelStore, then release
the barrier. Assert the real Room progress row committed, admission cleared,
and no departed-screen effect replayed. Seed an unrelated elapsed miss to
verify that any generated after-commit dispatch/port calls also survive.
Also verify every admission key rejects duplicate in-flight submissions and
releases on commit/failure, including business rejection. Reopening a screen
observes its key; after commit a delayed effect cannot keep Save disabled.
Hold the notifier's cancellation at a barrier after retirement confirmation
commits, then submit Undo before the deadline: its distinct key must admit it
and its transaction must restore/finalize as appropriate without awaiting cancel.
Verify both confirm/undo arrival orders against the database gate and deadline.

Execute undo before the deadline after the frozen occurrence's end has passed:
it restores first, then finalizes exactly once in the same transaction. At/after
the deadline, late undo retires and cancels the frozen occurrence without scoring.
Execution after midnight must reject the captured progress occurrence and never
update the new one. Test process interruption on both sides of commit using a
file-backed database; committed-create draft resubmission yields duplicate name.

Required cleanup tests cover lost acknowledgement cancellation, retirement with
positive misses, repeated foreground cleanup, and cleanup/post interleavings.
Assert an `ACTIVE` habit with positive misses retains its notification, all
ineligible ones are cancelled, and no new outbox state/table/acknowledgement is
created. Verify the exact tag/ID in recorded post/update/cancel calls, ID round
trip for enumeration, unrelated-tag isolation, and fail-fast overflow with no
platform call or transient retry. V1 tests inspect final results/restored drafts;
they require no rejection receipt or replayed transient effects.

## Composition root and dependency wiring

### Manual composition root — ADR 0003

The creator selects a manual `AppContainer`, created lazily and thread-safely
by `HabitTrackerApplication`. Keep the readable object graph in one place for
this single-module app. Hilt is the alternative: it offers generated wiring
but adds framework/configuration overhead that is not needed for this graph.
Revisit with the implementation owner at a multi-module split or when the
container becomes difficult to maintain.

[ADR 0003 — Manual composition root and Android dependency wiring](../adr/0003-manual-composition-root-and-android-dependency-wiring.md)
records this accepted core-pattern decision. It selects an option permitted by
the architecture's DI deferral; the architecture itself remains unchanged.
ADR 0002's tracking contract remains in force.

The container owns one instance per process of Clock, the dispatcher holder,
application scope, CommandRunner, Room database with SchemaGuards and production
type converters, repository, pure rule services, commands, notifier port,
notification permission/channel-state adapter, work-scheduler port,
HabitTrackerWorkerFactory, foreground coordinator, and BoundaryTicker.

Separate cheap container construction from database acquisition. A shared
suspending lazy database provider opens and initializes Room once on injected
`io`, including the SchemaGuards `onOpen` callback; concurrent first callers
await the same initialization. A synchronous property getter on the main
thread must not open/query the database. Production explicitly uses
`JournalMode.WRITE_AHEAD_LOGGING`; file-backed race fixtures use this same
setting and verify the effective journal mode. Implementation of this wiring
and build/configuration changes are separate work.

### Class and port placement

Paths are relative to the `com.codetutor.samplehabittrackerapp` package under
`app/src/main/java/`, except FakeClock. They extend the architecture's existing
package boundaries without adding Gradle modules. Ports expose Kotlin value
objects/enums only; Android Context, Notification, WorkerParameters, and
WorkManager types stay in data/platform adapters.

| New class / interface | Package-relative file | Responsibility |
| --- | --- | --- |
| `AppContainer` | `AppContainer.kt` | Composition root alongside HabitTrackerApplication. |
| `CommandRunner` | `core/work/CommandRunner.kt` | Application jobs, transaction admission keys, busy-state projection. |
| `AppDispatchers` | `core/coroutines/AppDispatchers.kt` | Injected io/default/main dispatcher holder. |
| `ForegroundCoordinator` | `core/lifecycle/ForegroundCoordinator.kt` | Single process observer's foreground sequence. |
| `HabitTrackerWorkerFactory` | `data/worker/HabitTrackerWorkerFactory.kt` | WorkManager construction path for both workers. |
| `MissedResultNotifier` | `domain/port/MissedResultNotifier.kt` | Plain-value post/update/cancel/enumerate port. |
| `AndroidMissedResultNotifier` | `data/notification/AndroidMissedResultNotifier.kt` | NotificationManager, tag/ID and active-notification adapter. |
| `WorkScheduler` | `domain/port/WorkScheduler.kt` | Plain-value unique-work scheduling port. |
| `WorkManagerWorkScheduler` | `data/worker/WorkManagerWorkScheduler.kt` | WorkManager names/policies/configuration adapter. |
| `NotificationAvailability` | `domain/port/NotificationAvailability.kt` | Permission/channel-state port; no permission-launch effect ownership. |
| `AndroidNotificationAvailability` | `data/notification/AndroidNotificationAvailability.kt` | Platform permission/channel checks; existing NotificationPermissionStore retains the DataStore requested flag. |
| `FakeClock` | `core/time/FakeClock.kt` under `app/src/test/java/` | Scheduler-backed test Clock; reuse through shared test-fixture configuration for instrumented tests, never production source. |

Existing Clock/TimeSnapshot/BoundaryTicker remain under `core/time`, the
MissedResultDispatcher under `data/notification`, and screen ViewModels in their
existing feature packages. A test Application/runner and instrumented fixtures
live under `app/src/androidTest/java/`; share the FakeClock implementation through
test-only fixture/source configuration rather than copying its time rules.

### Android-created object register

| Object created or invoked by Android/AndroidX | Dependency path |
| --- | --- |
| `HabitTrackerApplication` | Framework no-argument construction; owns the lazy production container provider. Instrumented test runner substitutes the test Application. |
| `MainActivity` | Framework construction; obtains container through Application, supplies route-aware ViewModel factories, and hosts navigation. No database creation or foreground maintenance in Activity callbacks. |
| All screen ViewModels | Navigation/ViewModelProvider calls `viewModelFactory`; `CreationExtras.createSavedStateHandle()` supplies restored inputs and route arguments. Factory passes container dependencies; no manually empty handle in production. |
| `ReconcileTrackingWorker` and `DispatchMissedResultsWorker` | WorkManager calls `HabitTrackerWorkerFactory.createWorker(context, className, parameters)`; it injects the shared commands, ports, Clock, and dispatchers. Returns a new worker per request and null only for unknown worker classes. |
| Application WorkManager configuration | `HabitTrackerApplication` implements `Configuration.Provider`; configuration installs the real worker factory without forcing database initialization. |
| Process lifecycle observer | Application installs one lightweight observer delegating through the container provider. Heavy startup work is launched from `ON_START`, not performed in the registration callback. |
| Dynamic time-change receiver | Ticker constructs/registers it via injected application Context; `onReceive` only signals re-arm/catch-up to the application scope. |
| Notification PendingIntent entry | Android starts/reuses `MainActivity`; route resolution obtains the same lazy container and route-scoped ViewModel factory. No warm-process assumption. |

Factories follow [ViewModel CreationExtras guidance](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories).
Remove only `androidx.work.WorkManagerInitializer` metadata from the merged
`androidx.startup.InitializationProvider`, preserving other initializers such
as process lifecycle. Use `WorkManager.getInstance(applicationContext)` with
`Configuration.Provider`; do not initialize a second WorkManager manually.
The scheduler adapter obtains WorkManager lazily, so requesting configuration
cannot recurse through container construction into another configuration
request. See [custom WorkManager initialization](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration).

### Startup and unique work register

`Application.onCreate` stays light: create the notification channel and register
the lightweight process observer. Do not open Room, run catch-up, construct the
whole graph, or start the ticker there. Android can start a worker in a process
with no Activity; notification entry also needs to work from a cold process.
Worker creation therefore cannot depend on a foreground initialization step.

The foreground coordinator asks the work-scheduler port to ensure periodic
catch-up at first app foreground/start and on later starts. This is an app-start
responsibility, not a per-screen responsibility; repeating it is safe under
`KEEP`. A headless worker startup uses WorkManager's existing persisted work
and does not launch a foreground ticker.

| Unique work name | Worker / kind | Policy and enqueue owner |
| --- | --- | --- |
| `habit-tracker.reconcile.periodic.v1` | `ReconcileTrackingWorker`, periodic | `ExistingPeriodicWorkPolicy.KEEP`; foreground coordinator at app start via work-scheduler port; every **6 hours**, with **no constraints**. Worker reconciles then drains eligible outbox work; never returns `Result.retry()`. |
| `habit-tracker.notifications.dispatch.v1` | `DispatchMissedResultsWorker`, one-time dispatch/retry chain | `ExistingWorkPolicy.APPEND_OR_REPLACE`; after-commit dispatch scheduling via work-scheduler port. Keep WorkManager's default `EXPONENTIAL` policy and **30-second** base. The persisted outbox `attemptCount`, not `runAttemptCount`, enforces the fifth-failure limit. |

The one-time policy is a design choice to avoid a `KEEP` request being ignored
while an earlier dispatcher is still running. Each drain is idempotent; a
committed pending event survives enqueue failure and is found on foreground or
periodic reconciliation. Only the one-time dispatch worker returns
`Result.retry()` for remaining eligible retryable delivery work; it must not
enqueue itself recursively. Once a handled drain leaves no eligible retryable
work, return `Result.success()`, including when events become `UNAVAILABLE` or
`SUPPRESSED`, so exhausted delivery does not hold the chain. Unique names/policies
follow [WorkManager's work-management contract](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work).

### Worker timing and retry budget

Periodic catch-up is best effort every six hours, with no network, charging,
battery, or other constraints. It never returns `Result.retry()`; after a failed
run, the next period, foreground reconciliation, or a period-sensitive command
covers catch-up. Notification delivery failures retain their outbox state and
schedule the one-time dispatch retry path; they do not make the periodic worker
retry. Propagate cancellation rather than converting it into a retry.
Period ends are local calendar midnights under the architecture's planner;
reconciliation uses the stored boundary instants and does not retime existing
occurrences. Cadence affects how soon a closed-app miss is noticed, not which
period receives a result. Doze can defer overnight execution; neither this
interval nor a worker guarantees a midnight wake-up.

Dispatch uses WorkManager's default exponential backoff from 30 seconds. A
transient delivery failure updates the outbox's persisted `attemptCount`; the
fifth failed attempt marks the event `UNAVAILABLE` under the architecture.
Never use WorkManager's `runAttemptCount` as the event budget: worker runs,
foreground attempts, and replacement work requests do not share that counter.
Fresh events retain their own budget even when an older work request has retried.
A terminal event is not posted or retried again by a later worker invocation.

For five consecutive failures in one uninterrupted retry sequence, the four
nominal waits are 30, 60, 120, and 240 seconds: **450 seconds (7.5 minutes)**
plus execution time. This bounds the planned waits for that sequence and avoids
unbounded backoff from an exhausted event holding newer dispatch requests behind
`APPEND_OR_REPLACE`. It is **not a wall-clock deadline**: delays are inexact,
Doze/system scheduling can extend them, and other foreground attempts may change
the remaining event budget. See [WorkManager retry and scheduling guidance](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work).

Quiet hours are a product question outside this design. No quiet-hours behavior
is selected here; any future policy requires product approval before design changes.

### Clock, ticker, and test-container replacement

The sole time interface is `Clock.now(): Instant` and `Clock.zone(): ZoneId`.
The production adapter delegates instant readings to
`java.time.Clock.systemUTC().instant()` and obtains the current zone from
`TimeZone.getDefault().toZoneId()` on each snapshot, rather than caching the
startup zone. Nothing calls `Instant.now()`, `System.currentTimeMillis()`, or
`ZoneId.systemDefault()` directly. Capture `TimeSnapshot` once inside the
transaction after the gate, preserving the architecture's time authority.

Ticker waits are computed from `Clock.now()` and run with `delay` on its
injected dispatcher. Recompute on commits, time-change signals, and process
start. Register `ACTION_TIME_CHANGED`, `ACTION_DATE_CHANGED`, and
`ACTION_TIMEZONE_CHANGED` through `ContextCompat.registerReceiver` with
`RECEIVER_NOT_EXPORTED` when the process starts; unregister when it stops.
Registration/re-arm is idempotent, uses application Context, and happens through
the injected main adapter. Verify the intended system broadcasts on supported
devices; see [context-registered receiver guidance](https://developer.android.com/develop/background-work/background-tasks/broadcasts).

In tests `FakeClock.now()` equals a fixed base instant plus the shared
`TestCoroutineScheduler.currentTime`; its zone is controlled. Inject test
dispatchers sharing that scheduler and an independent application test scope.
Advancing virtual time now advances both clock readings and ticker delays.
Await database completion separately; virtual time does not execute Room's real
threads. Use finite `advanceTimeBy`/`runCurrent` steps, not unbounded
`advanceUntilIdle` against a forever-rearming ticker. Zone-change tests update
the fake zone and deliver a re-arm signal.

The shared scheduler/dispatcher setup follows [Android coroutine test
guidance](https://developer.android.com/kotlin/coroutines/test); binding FakeClock
to that scheduler is this app's explicit test-time contract.

Instrumented tests use a custom runner whose `newApplication` starts a test
Application with a test container, installed before lifecycle callbacks,
ViewModel creation, or WorkManager initialization. It supplies FakeClock, test
dispatchers/application scope, a temporary database, and fake ports. Main-thread
platform callbacks remain on Android's UI thread; injected app work is driven
by the test scheduler. The test container also supplies WorkManager's real
factory through configuration. No initialized production singleton can remain
cached when the container is replaced.

### Verification obligations

Show the overview using this test graph, await initial database
state, advance shared virtual time across a stored boundary, then await Room
and UI updates and assert finalization/current status. This makes the
architecture's boundary test executable on both instrumented API tiers using
the custom test Application. Robolectric host tests build both workers
through the real HabitTrackerWorkerFactory with real WorkerParameters (or a
WorkManager test builder configured with that factory), run their work, and
assert database changes plus recorded notifier/scheduler calls. Also verify
headless startup and factory use in merged configuration with the custom test
Application on both API tiers. Assert periodic `KEEP`, six-hour interval, no
constraints, and no `Result.retry()` after an injected periodic-run failure.
Verify the next period/foreground/command still reconciles. Assert dispatch
requests retain default exponential/30-second backoff, four nominal waits sum
to 450 seconds, the fifth persisted transient failure becomes `UNAVAILABLE`,
and a handled exhausted drain releases its work-chain dependency. Give a worker
a `runAttemptCount` different from persisted event attempts; verify only the
latter controls exhaustion, including a fresh event on an older worker request.
Also verify receiver registration/unregistration across lifecycle transitions.
Check the
route table's factory/handle scope on every destination, notification deep-link
back-stack construction, and that the class/port placement keeps the domain
Android-free. AppContainer must supply the same CommandRunner, notification
serializer, and tag-aware notifier to foreground and after-commit paths.

## Seam register (test doubles)

### Component choices

| Component | Real or fake | Injection path and reason |
| --- | --- | --- |
| Pure rule services: validator, planner, target policy, scoring, continuity, badge evaluator | Real | Test container constructs production services; unit tests supply inputs. No alternative rule implementation. |
| Room schema, DAOs, converters, SchemaGuards | Real | Shared production/test database-builder configuration with the same callback, converters, and migrations. Default repository fixtures are in-memory; SQLite must actually enforce contracts. |
| Repository and commands, including gated reconciliation and acknowledgement | Real in all rule/repository/outbox tests | Test container supplies the real Room repository and commands; inspect persisted rows and public results. |
| CommandRunner and dispatcher coordination | Real | Container injects independent test application scope; exercise admission, cancellation ownership, and after-commit ordering. |
| Clock | Fake | FakeClock from the same virtual-time scheduler as coroutine test dispatchers; race tests coordinate scheduler changes through barriers. |
| Dispatchers and application scope | Test-controlled | Shared scheduler for normal app coroutines; real off-main threads/executors for database and contention tests. Test cleanup explicitly cancels the app scope. |
| Notifier port | Recording fake | Container injection; records stable post/update/cancel identities and can inject transient failure or a crash-window interruption. No Android notification service in rule tests. |
| Work-scheduler port | Recording fake | Container injection; records names, policies, requests, and enqueue failures without running unrelated platform jobs. |
| Notification permission and channel state | Fake | Inject permission/channel-state provider into dispatcher; deterministically supply allowed, denied, or blocked state. |
| Repository in ViewModel UI-mapping tests only | Permitted fake | Constructor/test container injection; serves prepared read models/results. It never recalculates periods, points, guards, or command rules. |
| Context for Room-capable tests | Real harness Context | Robolectric Application Context in host tests; Android test Application Context in instrumented tests. Never a mocked Context. |

Never mock Context, Room DAOs, or the repository in a rule test. No mocking
library is needed. A rule fixture with a fake repository is not evidence for
SQLite gates, triggers, late-save rejection, or exactly-once finalization.

ViewModel tests send public events and assert resulting `UiState`, public
command results where relevant, and recorded port calls when using a real
command graph. Do not count StateFlow emissions or test private functions or
private composables. UI-mapping-only fakes must not be used to claim real
after-commit effects occurred; that claim belongs to the real-command seam.

ViewModel-only coroutine unit tests install the shared test dispatcher as Main
with `Dispatchers.setMain` and reset it in fixture teardown, because the default
`viewModelScope` uses Main independently of the container's injected dispatchers.
Instrumented Compose tests retain Android's actual UI thread and await its idle
boundary; their injected application work still uses the test scheduler.

### Test harness tiers

OQ-004 selects two tiers. Both retain the real/fake choices in the seam register;
Robolectric does not authorize replacing SQLite enforcement with fake rules.

| Tier / source set | Required suites | Harness and evidence |
| --- | --- | --- |
| Host — `app/src/test` | ViewModel restoration/event-to-UiState tests; real in-memory repository and outbox tests; workers built through the real `HabitTrackerWorkerFactory`. | Robolectric supplies a real harness Application Context; coroutine/test-container dependencies remain controlled as above. Fast host SQL results support development but do not establish minimum-API guard or migration compatibility. Pure rule tests remain host tests with real rule services. |
| Instrumented — `app/src/androidTest`, API 24 and API 36 | File-backed race/gate tests; every migration and guard-catalog test; SQL guard behavior needed for minimum-API evidence; all tests requiring the custom test Application, including the overview/fake-clock boundary and Android entry-point tests. | Use the custom runner/test Application and production database builder, including platform SQLite, guards, converters, WAL and real-thread contention where applicable. The same required suites run at both API levels. |

**API 24 is mandatory:** a SQL-facing test counts as evidence for a guard or
migration only after it passes on API 24. Host success, Room's build-time query
validation, or an API 36-only pass cannot substitute. Share guard case inputs
and assertions across tiers where practical; do not create a second rule
implementation. In-memory host guard assertions in the verification matrix
below also need their SQL-facing cases on the API 24 instrumented tier before
being claimed as guard evidence.

Android API 24 ships SQLite 3.9, whereas host SQLite and Room's build-time query
checker use newer engines; accepted SQL syntax and trigger behavior can differ.
Use Android's platform SQLite on instrumented devices, rather than a bundled
newer driver that would bypass the minimum-API check. Record `sqlite_version()`
with each instrumented SQL test report, along with API level and results. See
[Android's SQLite version table](https://developer.android.com/reference/android/database/sqlite/package-summary).

Use a regular **API 24 emulator/device** via
`./gradlew :app:connectedDebugAndroidTest`, and a **Gradle Managed Device at
API 36**. The creator approved this execution adjustment because Android's
[managed-device documentation](https://developer.android.com/studio/test/managed-devices)
limits managed devices to API 27 and higher. Select the API 24 device explicitly
so a connected higher-API device
cannot accidentally satisfy the minimum-API job. Both execute the same required
instrumented suites. This is a resolved tooling adjustment; it preserves the
API 24 evidence rule. Managed-device configuration is later implementation work.

### Room execution and contention topology

Use the Room executor configuration path consistently (do not also configure
`setQueryCoroutineContext`). Normal repository fixtures explicitly set a
dedicated single-thread off-main query executor and a separate dedicated
single-thread off-main transaction executor via `setQueryExecutor(queryExecutor)`
and `setTransactionExecutor(transactionExecutor)`. Both are owned and closed by
the fixture; neither is a main-thread/direct executor or a scheduler thread
blocked inside Room. This avoids Room silently using shared default executors
or assuming an injected coroutine dispatcher controls its work.

Determine event ordering with explicit completion signals, awaited public
commands/DAO reads, and bounded barriers; never sleeps or emission counts.
Advance virtual time only at coordinated checkpoints and await database work
before asserting. Query/transaction threads execute real SQLite; logical
ordering and clock movement are controlled by the test. Teardown cancels app
jobs, closes databases, then terminates/joins executors and deletes test files.
These executor choices use [Room's explicit executor
APIs](https://developer.android.com/reference/androidx/room/RoomDatabase.Builder).

In-memory tests alone cannot prove the gate. Concurrent-trigger and
undo-versus-expiry tests use a temporary file-backed database, production WAL,
real concurrent caller threads, and at least two independent Room instances
opened on the same file. Give each instance its own query and transaction
executor so a shared Room transaction queue cannot serialize both contenders
before SQLite sees them. Use barriers to establish both arrival orders; clock
reads must be thread-safe and stable at each chosen phase. At the exact deadline
undo is late regardless of arrival order; also test a pre-deadline undo winner
and a post-deadline expiry winner.

Even separate connections encounter SQLite's single-writer serialization.
Therefore a passing final-balance race alone does not prove the gate statement
exists. Assert the singleton gate version changes once per gated transaction,
and use a lightweight test SQL trace plus FakeClock capture records to verify
the gate update precedes the time snapshot and any domain reads/writes. Include
a contender waiting for database acquisition whose clock advances before it
acquires the gate: its snapshot must use the later time. Verify rollback also
rolls back the gate increment. See [SQLite isolation](https://sqlite.org/isolation.html).
Tests exercise public commands and persisted evidence, not a fake lock.

### Verification obligations

`R-memory` means real in-memory Room plus production guards/converters,
repository, commands, real rules, and injected fake time/ports.
`R-file` is that graph with the file-backed contention/restart topology above,
run instrumented at API 24 and API 36. `R-memory` and ordinary `O` cases run on
Robolectric; their guard claims also require API 24 SQL-facing evidence under
[Test harness tiers](#test-harness-tiers).
`O` adds the real missed-result dispatcher with recording notifier and scheduler,
fake permission/channel state, and controlled interruption points.

| Architecture verification reference | Required seam and evidence |
| --- | --- |
| FR-001 repository test: duplicate active names and retired-name reuse | R-memory: command behavior plus direct real SQL/DAO attempts prove the name guard rejects invalid rows. |
| Time/concurrency table: late save at/after boundary; FR-003 replace-not-add and late rejection | R-memory: save the captured occurrence ID, inspect unchanged closed/new rows, normalized exact values, and returned final result. |
| Time/concurrency table: identical balance whichever command triggers catch-up | R-memory for both trigger orders; R-file for concurrent command/worker triggers. Assert balance and each occurrence's point effect. |
| Time/concurrency table: clock moved back | R-memory: watermark, immutable boundaries/results, and no reopened/recreated occurrence. |
| FR-004 idempotency, overlap, total order, and process death | R-memory for sequential repeats; R-file for overlap and interrupted transaction/reopen. Assert exactly one result, points effect, badge, and event per eligible occurrence. |
| FR-005 target-change/history checks | R-memory: cadence rules, no-open/late changes, superseded entries, and unchanged finalized history. |
| FR-008 score/badge/continuity checks | Real pure rules for arithmetic/window cases; R-memory for non-negative balance guard, aggregate audit links, and exactly-once awards. |
| Platform integration Outbox tests: crash after post, retry exhaustion, aggregation, retirement race | O + R-memory for deterministic post-before-state-update, five-failure limit, stable habit ID, and aggregation; O + R-file for retirement/post interleavings. Assert persisted delivery states and recorded effects. |
| FR-009 Outbox tests: acknowledgement, unavailable delivery, suppressed events, retry, foreground cleanup | O: permission/channel fakes, retained in-app miss count, conditional states, exact tag/ID, unrelated-tag isolation, overflow, and required cleanup for non-active/zero-miss habits. O + R-file covers both commit-before-cancel crash windows and cleanup/new-post interleavings. |
| FR-010 and time/concurrency table: undo/expiry race, freeze/resume, late undo, restart countdown | R-memory proves restore-before-catch-up and immediate once-only finalization; real runner + O holds a post-confirm cancel while timely undo commits independently. R-file covers both gate arrival orders, deadline contention, and restart. |
| FR-011 guard-presence migration and restart tests | Instrumented API 24 and API 36 migration fixtures using real versioned files and production callbacks; restart R-file. Check guard names/definitions and behavior, not a mocked callback. API 24 passing results are mandatory guard/migration evidence. |

Every generic Repository/Outbox test in the architecture uses these real
database seams. Planner/validator/scoring-only tests remain pure-rule tests.
Compose accessibility, deep-link back stack, OS notification display, and
backup/transfer checks remain their UI/device seams; fake ports cannot prove
Android behavior. AND-003 cleanup and identity checks are required: fake ports
prove the predicate/serialization, while a device check proves tag-filtered
NotificationManager enumeration and cancellation on supported Android versions.

On both API 24 and API 36, for every supported migration path, migrate a real
old-schema database, reopen through Room so `onOpen` runs, and query
`sqlite_master` to compare every production guard trigger with the canonical
catalog. Then attempt prohibited
inserts/updates for status/result combinations, acknowledgement eligibility,
boundary ordering, retirement fields, weekday masks, active-name uniqueness,
and negative balance. Verify actual rejection, valid writes, and retained data.
`CREATE TRIGGER IF NOT EXISTS` cannot replace a changed trigger body; a migration
that changes a guard must explicitly replace it and verify the new definition.

Required evidence includes passing host suites and both instrumented API tiers;
record their source sets, API/SQLite versions, guard catalog, and migration paths
in the test reports. An unexecuted/skipped API 24 SQL case remains unverified,
even if host/API 36 tests pass. Verify workers use the real factory in Robolectric,
while custom-Application initialization is exercised instrumented at both APIs.
These obligations require actual Room/SQLite and real harness Contexts; mocked
Android objects do not satisfy them. All checks in this document are required
implementation evidence, not tests executed by this documentation change.

## Architecture and ADR consistency review

Reviewed and resolved by creator decision on 2026-10-05. The architecture,
ADR 0002, and ADR 0003 remain unchanged. ADR 0003's statement that it does not
itself authorize cleanup or settle undo describes the scope of that wiring
record; the creator separately authorizes those contracts in this document.

| Finding | Status | Accepted decision |
| --- | --- | --- |
| C-001 — Undo transaction ordering | Resolved | Gate → snapshot → conditional restore before deadline → same-transaction global catch-up. The specific freeze/resume rule and architecture order govern; ADR 0002's general command list is imprecise, not an instruction to leave a resumed elapsed occurrence open. |
| C-002 — Admission and timely undo | Resolved | Keys distinguish command and habit target. `retirement.confirm:{habitId}` and `retirement.undo:{habitId}` are independent. Release admission at commit/failure; separate after-commit jobs hold no key. Database gate and deadline decide the race; slow cancellation cannot block timely undo. |
| C-003 — Cancellation recovery | Resolved | Accepted foreground cleanup inside the serialized dispatcher cancels visible tagged notifications for non-ACTIVE habits or zero unacknowledged misses. Preserve eligibility recheck and existing outbox schema/states; this repairs acknowledgement and retirement crash windows on next foreground. |

**Note for the architecture owner:** ADR 0002's Global catch-up list includes
undo under reconcile-before-write wording, while its Retirement freeze rule
requires resume before same-transaction catch-up can finalize the frozen row.
The creator selects the architecture command table's specific order. A later
source-wording clarification may be appropriate; no architecture or accepted
ADR text is changed in this pass, and implementation needs no further order
choice or invented extra catch-up pass.

| Consistency check | Result |
| --- | --- |
| Progress occurrence identity | Destination restores `progress.occurrenceId`; runner snapshots that same ID and `progress.typed` into `SaveProgress(habitId, occurrenceId, rawText)`; catch-up rejects a closed/non-current occurrence. |
| Progress navigation/state scope | `record-progress/{habitId}` owns its ViewModel/handle and may render as a sheet. Route table, screen-state section, and command-key table agree. |
| Edit restoration and admission | `form.initialised` protects restored edits. `habit.edit:{habitId}` gates duplicate transactions without holding the key for notification effects. |
| Create after process death | No draft token or auto-replay. Resaving the unchanged restored draft after its create committed hits duplicate-name validation. |
| Work and state lifetime | Handle restores input; runner owns current-process transaction jobs, with separate key-free effect jobs. UI busy state follows the transaction key only. |
| Gate/snapshot and undo | Gate precedes time capture. General commands catch up before writing; undo conditionally restores before catch-up; acknowledgement retains its no-gate transaction. |
| Clock/ticker/worker graph | Factories share Clock/dispatchers; fake clock and ticker wait share a scheduler; real Room executor completion is awaited separately. |
| Notification identity/recovery | Stable tag plus `Math.toIntExact(habitId)` is shared by every notification path. Accepted cleanup uses the existing suppression predicate, preserves recheck, and adds no table/state. |
| Rejection visibility | Finalized period result and restored draft suffice; no V1 durable rejection receipt. |
| Package boundaries | New ports use plain Kotlin types in domain/port; Android implementations stay in data packages; test clocks remain test-only. |
| Test obligations | Real rules/Room/repository/commands prove invariants; mapping-only fakes prove UI projection. Robolectric hosts in-memory/UI/factory tests; API 24 and API 36 instrumented tiers prove file-backed contention, migrations, guards, and the custom Application. Guard/migration evidence requires API 24. |
| Worker timing | Six-hour unconstrained periodic reconciliation never requests retry. One-time dispatch keeps default exponential/30-second backoff; persisted event attempts enforce exhaustion, and 7.5 minutes is nominal planned delay, not an OS delivery deadline. |

### Closed questions

| Previous ID | Resolution |
| --- | --- |
| OQ-001 and OQ-002 | Widened AND-003 cleanup approved for non-active habits or zero misses, within the shared dispatcher. |
| OQ-003 | Command/target keys and transaction-only admission lifetime specified; no create draft token. |
| OQ-004 | Two-tier harness accepted: Robolectric host suites, connected API 24 instrumentation, and managed API 36 instrumentation; API 24 is mandatory for guard/migration evidence. The creator approved the connected-device adjustment for API 24. |
| OQ-005 | Periodic catch-up every six hours without constraints or retry; one-time dispatch uses default exponential/30-second backoff and the outbox fifth-failure budget. Quiet hours remain a separate product question. |
| OQ-006 | Architecture's conditional-restore-before-catch-up undo order accepted; owner note retained above. |
| OQ-007 | Exact Int habit ID with fixed tag; active-notification enumeration filters by that tag. |
| OQ-008 | No durable rejection receipt in V1. |

## Open questions

None within this low-level design. OQ-001 through OQ-008 are closed; the API 24
connected-device adjustment is approved and the minimum-API evidence requirement
is fixed.

Quiet hours are an external product question owned by the creator/product owner,
to be resolved in product definition before adding any delivery-window behavior.
This is not a worker-timing deferral or an approved notification policy.
Existing architecture deferrals for encryption/PIN threat models and visual design
remain outside this document's scope. Verification obligations are implementation
requirements; this update performs documentation checks, not runtime tests.
