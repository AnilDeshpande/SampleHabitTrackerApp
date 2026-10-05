# Habit Tracker V1 — Low-level design refinement learnings

Date: 2026-10-05. Purpose: session correction record for a future detailed-design
skill. This records decisions and review lessons; it does not create the skill,
change the approved design, or claim that implementation tests have run.

Sources are the creator's instructions and reviews in this session, the
[architecture](../architecture/habit-tracker-v1-architecture.md), the resulting
[Android low-level design](habit-tracker-v1-low-level-design.md),
[ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md), and
[ADR 0003](../adr/0003-manual-composition-root-and-android-dependency-wiring.md).
The architecture line numbers quoted during the discussion described that
version; the contracts and decision IDs below are the stable references.

“First draft” below means the earliest design state that needed the correction.
Some gaps were inherited from the architecture; others remained in my first
formal low-level design. A missing decision is not evidence that I implemented
the bad alternative. No repository, command runner, or Android wiring was built
in this session. Where I left a choice open, I say that rather than inventing an
implementation mistake. The temporary notes were initially requested by the
creator; promoting them later was a change in artifact maturity, not evidence
that making the temporary notes was wrong.

The repeated lesson was that a named mechanism is insufficient. “ViewModel,”
“application scope,” “composition root,” and “repository test” all needed a
complete contract. Follow-up corrections refined state identity, transaction
versus effect lifetimes, construction paths, and the database used as evidence.
Entries explicitly identify those second passes. The API 24 device-tooling
adjustment and the qualification of the retry-duration estimate were identified
by me during verification; they are distinguished from creator corrections.

## 1. Who keeps this state?

### S-01 — Cover every screen and every state value

**First draft / gap:** The architecture explicitly promised rotation/process-death
survival for the pending-retirement row, but form and progress descriptions only
promised preservation during validation. A general ViewModel/StateFlow choice
left all other process-death behavior unspecified.

**What you told me:** Add “Screen-state ownership and restoration” and classify
every state value exactly once: durable, restorable input, derived, transient
effect, or visual-only. Cover every screen, not just Create Habit.

**Why it matters on Android:** A ViewModel survives rotation but disappears when
Android kills a background process. Rotation checks can therefore pass while
unsaved business input is lost.

**General rule:** Require a per-screen state inventory with one owner, persistence
mechanism, reconstruction source, and lifecycle guarantee for each value.
Never infer process-death restoration from ViewModel use or a neighboring screen.

### S-02 — Give business input one testable owner

**First draft / gap:** Nothing prevented a draft from living in a ViewModel's
independent MutableStateFlow, Compose state, or duplicate stores.

**What you told me:** Business input belongs in the ViewModel's SavedStateHandle;
`rememberSaveable` is for visual-only state. There must be one owner that is
unit-testable.

**Why it matters on Android:** Multiple draft stores can diverge during
recomposition or recreation. A composition-owned field also bypasses ordinary
ViewModel restoration tests.

**General rule:** Make saved input handle-backed and update it on input events.
Treat UiState as a projection. Restrict `rememberSaveable` to presentation choices
that cannot change a business command's payload.

### S-03 — Register concrete saved-state keys, types, and encodings

**First draft / gap:** “Preserve the fields” did not identify the actual saved
keys or their storage representations.

**What you told me:** Register `form.name`, `form.unit`, `form.target`,
`form.frequency` as the enum name, and `form.weekdays` as an Int mask using the
same encoding as `habits.selectedWeekdays`: Monday is bit 0. Register the
progress keys separately. Obtain `habitId` from the route.

**Why it matters on Android:** Bundle-compatible representations and stable keys
are part of restoration. A different weekday encoding silently restores the
wrong schedule; Monday/Wednesday/Friday must remain mask `21`.

**General rule:** Publish a complete key/type/default/encoding register per
ViewModel. Reuse the durable representation where appropriate. Route arguments
identify the destination; do not add a second mutable copy of that identity.

### S-04 — Preserve invalid raw text rather than a parsed value

**First draft / gap:** The original form/progress descriptions did not decide
whether the stored draft was raw text or a parsed numeric value.

**What you told me:** Save `form.target` and `progress.typed` as exactly the text
typed, including invalid `1,2.3`; do not save target as BigDecimal.

**Why it matters on Android:** Parsing or normalization can erase incomplete or
invalid input before the user returns. Restoring a corrected number does not
restore what the user was editing.

**General rule:** Persist a draft's lexical representation. Parse, validate, and
normalize at explicit validation/save boundaries; derive errors rather than
saving an error object or losing the original string.

### S-05 — Protect restored edits from database initialization

**First draft / gap:** Loading the saved habit in edit mode could seed fields
again when a recreated ViewModel re-read Room.

**What you told me:** Add `form.initialised`; set it after the first Room seed.
When restored as true, subsequent loads must not overwrite unsaved edits.

**Why it matters on Android:** Correct SavedStateHandle restoration is still
useless if an asynchronous database emission immediately replaces it with the
last saved habit. Empty or invalid edits are especially easy to overwrite.

**General rule:** Define first-load seeding separately from durable-state refresh.
Use a restorable initialization marker, preserve intentionally empty values,
and test both initial seeding and recreation with the marker already set.

### S-06 — Restore the identity the input was written for

**First draft / gap:** Progress retained “the typed value” without retaining its
occurrence. Habit identity alone was insufficient.

**What you told me:** Save both `progress.typed` and `progress.occurrenceId`.
After restoration, determine whether that occurrence is still current.

**Why it matters on Android:** Android can recreate the screen after midnight or
another period boundary. The same habit now has a different occurrence, so
restoring the number alone can attach yesterday's input to today's period.

**General rule:** Restore input together with the minimal stable identity of its
business target. Recheck that identity against durable state; never silently
retarget a restored draft. W-13 records the second correction that carried the
same identity through the command contract.

### S-07 — Revalidate restored input against current durable state

**First draft / gap:** Field preservation during validation did not specify
restore-time validation or eligibility after the app had been absent.

**What you told me:** Recompute validation from raw text. For progress, compare the
saved occurrence with the current one after catch-up; if it closed, show the
architecture's late-submission state and retain the typed value.

**Why it matters on Android:** Time and the database can change while the process
is absent. Previously valid input may now be ineligible even though restoration
succeeded.

**General rule:** Restoration rebuilds a current read model, not a snapshot of
old eligibility. Revalidate text and business identity; preserve the user's
input while displaying closed, unavailable, or read-only state.

### S-08 — Explain which screens need no saved keys

**First draft / gap:** A generic saved-state solution could have serialized
Overview or Review UiState unnecessarily.

**What you told me:** Explicitly say OverviewViewModel and HabitReviewViewModel
need no saved keys, and explain why. Classify the remaining screens too.

**Why it matters on Android:** Persisted lists, history, miss counts, and retirement
deadlines are already durable. Saving their projections adds stale copies and
increases the saved-state payload.

**General rule:** “No saved input” is an explicit design result, not a missing
register. Rebuild Room-derived content and keep scroll/expansion in visual state.
A confirmed retirement deadline belongs in Room, not a restored countdown value.

### S-09 — Make Record progress a real destination with its own handle

**First draft / gap:** Describing a progress sheet did not establish its
ViewModelStoreOwner or SavedStateHandle lifetime.

**What you told me:** Make Record progress its own navigation destination keyed
by habitId. Later, require the route table and screen-state section to agree.

**Why it matters on Android:** A sheet's visual presence does not provide a stable
navigation entry. A parent ViewModel or temporary composable scope can share,
lose, or wrongly reuse its draft.

**General rule:** Specify destination identity, back-stack owner, ViewModel owner,
and saved-state owner even for dialogs/sheets. For this app,
`record-progress/{habitId}` owns RecordProgressViewModel and its handle.

### S-10 — Keep saved state small

**First draft / gap:** Restoration was not constrained by a payload budget or a
list of prohibited saved types.

**What you told me:** Save only short strings and primitive metadata. Never
entities, lists, or history.

**Why it matters on Android:** Saved state is Bundle-backed and travels through
Android lifecycle machinery. Large object graphs risk transport limits and
stale duplicated data.

**General rule:** Save only what is necessary to reconstruct a draft: strings,
weekday Int, occurrence Long, initialization Boolean. Reload entities/history;
recompute errors and complete UiState.

### S-11 — State restoration boundaries and non-restored effects explicitly

**First draft / gap:** “Survives process death” could be read as a promise of
restoring every termination path or replaying a previous command/effect.

**What you told me:** One-off effects are never restored. Drafts need not survive
swiping the task away from Recents; that is intended behavior.

**Why it matters on Android:** Retained-task recreation, rotation, task dismissal,
and force-stop are different lifecycle cases. Replaying navigation, saves,
permission launches, or success messages on recreation can repeat actions.

**General rule:** Describe retained-task process-death behavior separately from
user task removal. Save inputs promptly before lifecycle capture; reconstruct
durable state without restoring a coroutine, busy flag, or one-off effect.

### S-12 — Test recreation rather than only rotation

**First draft / gap:** The architecture's ordinary UI checks did not prove
SavedStateHandle restoration, edit seeding, or genuine background process death.

**What you told me:** Construct every ViewModel with a pre-filled handle, including
edit mode with `form.initialised = true`. Background the app, run
`adb shell am kill <package>`, and reopen from Recents.

**Why it matters on Android:** Reusing the same ViewModel across rotation cannot
expose state lost when the process and ViewModel are rebuilt.

**General rule:** Pair constructor-level restoration assertions with a retained-task
process-death device check. Verify invalid text, stale occurrence identity,
unsaved edits, and no effect replay; check task dismissal separately.

### S-13 — Complete the navigation register, including notification entry

**First draft / gap:** Individual route/scoping decisions still did not provide
one table covering every destination and the notification deep link.

**What you told me:** Add every route, including `record-progress/{habitId}` and
notification deep-link entry, and check consistency with screen ownership.

**Why it matters on Android:** A notification can enter a cold or existing task.
Wrong back-stack construction or route identity changes which ViewModel is
restored and which habit is acknowledged.

**General rule:** Register routes, arguments, owners, entry/return behavior, and
notification back-stack behavior together. In this app the explicit notification
entry resolves to Review with Overview → Review on cold and warm entry; a pending
retirement row remains inline overview content rather than a separate draft.

## 2. How long does work live?

### W-01 — Classify work lifetimes as explicitly as state lifetimes

**First draft / gap:** The architecture named an application-scoped BoundaryTicker
but did not specify scopes for SaveProgress, Create/EditHabit, Confirm/UndoRetirement,
or AcknowledgeMisses.

**What you told me:** Add a scope register: UI collection uses
`collectAsStateWithLifecycle`; screen-only loading/deriving/validation uses
`viewModelScope`; accepted commands and after-commit effects use an injected
application scope; eventual deferrable work uses WorkManager.

**Why it matters on Android:** The screen can close before its command completes.
A default ViewModel-owned job then disappears with the screen.

**General rule:** Inventory every operation's owner, cancellation boundary, and
recovery path. State observation, screen computation, accepted mutations,
foreground maintenance, and durable scheduled work are separate lifetimes.

### W-02 — Separate the caller's wait from the command's ownership

**First draft / gap:** A suspending command API alone did not explain whether
leaving the screen cancelled the database operation.

**What you told me:** The ViewModel awaits the result, but work belongs to the
application scope. Cancelling the wait must never cancel the accepted save.

**Why it matters on Android:** Navigation and ViewModel clearing are normal events,
not reasons to silently discard a Save the user already committed to.

**General rule:** Launch the transaction under the injected application
SupervisorJob/dispatcher and return an awaitable result. A caller-scoped
`withContext(io)` only changes execution context; it does not transfer ownership.
Handle failures even when no screen remains to receive the result.

### W-03 — Do not mistake an application scope for process durability

**First draft / gap:** Moving work out of the screen could leave an implied promise
that an accepted command must finish even when Android ends the process.

**What you told me:** Application scope does not survive process death. Accept
atomic transaction behavior and never claim success before commit.

**Why it matters on Android:** Android can kill the process during execution or
between commit and UI feedback. A restored draft does not prove the transaction
was rolled back.

**General rule:** Describe both sides of commit: unfinished transactions do not
persist; committed transactions remain even if success was never displayed.
Do not automatically replay a restored command. Keep user feedback tied to
commit, not job launch.

### W-04 — Keep after-commit effects outside the transaction and make them repeatable

**First draft / gap:** “After commit, cancel the notification” did not specify
its lifetime, idempotency, or failure recovery.

**What you told me:** Effects outlive the screen and must be idempotent. They may
need recovery on the next foreground after process death.

**Why it matters on Android:** Room and Android's notification service do not
participate in one atomic transaction. Death can happen after the database
changed but before posting/cancelling or enqueueing work.

**General rule:** Commit domain state first, then run repeatable platform effects
under a defined owner. Use durable outbox state or an explicit reconciliation
predicate for recovery. Effect failure must not undo a commit or report the
already committed save as failed. A late-save rejection can still commit catch-up
and outbox changes; schedule effects for those committed changes too.

### W-05 — Recover acknowledgement cancellation on foreground

**First draft / gap:** The architecture cancelled after acknowledgement, but there
was no repair for death between the acknowledgement commit and cancellation.

**What you told me:** Propose cancelling visible notifications for zero
unacknowledged misses on every foreground, and mark this new behavior for approval.

**Why it matters on Android:** The database can correctly show no outstanding
misses while Android still displays the old notification.

**General rule:** Review every database/platform-effect gap for a restart recovery
predicate. A new user-visible/platform behavior must be visibly proposed until
approved; the scratch note must not silently become an accepted contract.

### W-06 — Widen cleanup to the existing eligibility predicate

**First draft / follow-up gap:** AND-003's original zero-miss proposal did not
cover retirement with positive unacknowledged misses; OQ-001/OQ-002 and C-003
still required resolution.

**What you told me:** Approve and widen cleanup: cancel when the habit is not ACTIVE
or its unacknowledged-miss count is zero. This is already the outbox's SUPPRESSED
predicate; no new table or delivery state is needed.

**Why it matters on Android:** Death after retirement commit can also strand a
notification. A zero-count-only repair misses that crash window.

**General rule:** Reuse the complete domain eligibility predicate across dispatch
and repair paths. Test acknowledgement and retirement crash windows separately,
including retirement with positive misses. Avoid inventing schema for a repair
already derivable from durable state.

### W-07 — Serialize notification cleanup with posting and retain the recheck

**First draft / follow-up gap:** Even the widened cleanup needed an explicit
relationship to the serialized dispatcher and its eligibility recheck.

**What you told me:** Run cleanup inside that dispatcher, retain the recheck, put
it in the accepted foreground sequence, and make proposed tests required.

**Why it matters on Android:** Cleanup can race a new eligible missed result or a
late after-commit post/cancel job and leave a stale visible effect.

**General rule:** All notification post/update/cancel/enumerate decisions share
one serializer, with durable eligibility rechecked at the effect boundary.
Approval must update sequence, status, and tests together; do not leave a
behavior accepted in one section but “proposed” in another.

### W-08 — Use the process lifecycle for foreground maintenance

**First draft / gap:** The foreground trigger did not identify the Android callback
owner or prohibit per-Activity/per-screen startup jobs.

**What you told me:** One ProcessLifecycleOwner observer runs catch-up, notification
dispatch, and cleanup in application scope. Do not use Activity onStart/onResume.

**Why it matters on Android:** Activity lifecycle callbacks also run during
rotation and screen transitions, creating duplicate maintenance pipelines.

**General rule:** Register one process-level coordinator. Define foreground order,
serialize repeated requests, start/re-arm the ticker only while STARTED, and
stop its receiver/ticker without cancelling accepted commands. Route overview-open
catch-up through the same coordination path.

### W-09 — Keep duplicate-submission status outside the screen

**First draft / gap:** Disabling Save in a screen-local UiState did not stop a
newly opened screen from submitting while the old job was still running.

**What you told me:** Disable Save while a command is in flight and hold that
status in the app-scoped command runner.

**Why it matters on Android:** A screen can be dismissed/reopened or recreated
while its application-owned transaction continues.

**General rule:** Admission and busy state belong to the operation owner.
The screen observes them. Reserve admission atomically at submission rather
than depending on recomposition to prevent a second tap.

### W-10 — Specify admission keys by command and target

**First draft / follow-up gap:** OQ-003/C-002 left the key scheme unresolved.
A generic “in-flight command” register did not settle collisions between
confirmation, undo, progress, and editing for the same habit.

**What you told me:** Use `habit.create`, `habit.edit:{habitId}`,
`progress.save:{habitId}`, `retirement.confirm:{habitId}`,
`retirement.undo:{habitId}`, and `misses.ack:{habitId}`. Confirm and Undo use
different keys; the database gate/deadline decide their outcomes.

**Why it matters on Android:** Overly broad admission can prevent a timely Undo
while unrelated work for that habit is still completing.

**General rule:** Give every command a concrete admission key and explain why
commands share or do not share it. UI deduplication never replaces transactional
serialization, conditional writes, or deadline checks. Rewrite findings against
the final scheme rather than retaining an obsolete collision concern.

### W-11 — Release admission at transaction completion, not effect completion

**First draft / follow-up gap:** Moving transactions and effects to application
scope still did not pin when a key was released; holding it through a slow
notification cancel could interfere with timely user work.

**What you told me:** Hold the key until commit/failure only. Launch after-commit
effects as separate application jobs with no admission key.

**Why it matters on Android:** A slow platform cancellation must not consume the
ten-second retirement undo window or keep Save disabled after commit.

**General rule:** Separate transaction lifetime, effect lifetime, and UI-wait
lifetime. Return the transaction result and free admission at completion;
effects are application-scope sibling jobs, not children of the screen waiter.
Test timely Undo while a post-confirmation cancellation is deliberately held.

### W-12 — Decide create resubmission after process death without adding a token

**First draft / follow-up gap:** The admission discussion left create draft identity
and cross-process deduplication open.

**What you told me:** No draft token. Accept that manually re-saving the unchanged
restored draft after its create committed produces the duplicate-name error.

**Why it matters on Android:** Process death can occur after commit but before
navigation or success feedback; the restored form can look unsaved.

**General rule:** Record post-commit/no-feedback behavior explicitly. Process-local
admission does not imply exactly-once submission across process death. Use an
existing domain constraint when adequate; do not silently add durable tokens,
automatic replay, or a new idempotency protocol.

### W-13 — Carry the restored occurrence ID into SaveProgress

**First draft / second-pass gap:** Saving `progress.occurrenceId` for restoration
alone did not force the command to use it. A command accepting only habitId and
value could still choose “current occurrence” when it executed.

**What you told me:** SaveProgress receives habitId, occurrenceId, and raw text;
the occurrence is the one the user typed for and the handle restored.

**Why it matters on Android:** A save submitted before midnight may acquire the
gate after catch-up has opened the next day. Execution-time lookup could place
old input in the new period instead of rejecting it.

**General rule:** Trace identity from UI input through restoration, immutable
request capture, transaction validation, and result. Verify the submitted
occurrence's ownership/OPEN/current status after catch-up; never retarget it.

### W-14 — Resolve undo ordering using the specific freeze/resume invariant

**First draft / follow-up gap:** C-001/OQ-006 exposed conflicting generic wording
and remained open: ADR 0002's broad catch-up-before-write list included undo,
while its freeze rule and the architecture required restoration before catch-up.

**What you told me:** Keep gate → snapshot → conditional PENDING_RETIREMENT to
ACTIVE when now < deadline → same-transaction ReconcileTracking. Treat the
ADR's generic list as imprecise, note it for the architecture owner, and do not
edit the architecture or ADR 0002.

**Why it matters on Android:** A frozen occurrence can end during the undo window.
Reconciling while still frozen, then restoring, leaves that elapsed occurrence
open and defeats deterministic finalization.

**General rule:** Check command-specific transaction ordering against lifecycle
invariants, including timeout races. Do not mechanically apply a generic order
to an exception. Record the selected interpretation and source discrepancy;
get a material conflict resolved rather than handing it to a coding agent.

### W-15 — Make dispatcher, transaction, and cancellation contracts explicit

**First draft / gap:** Architecture-level asynchronous work did not decide the
Android execution mechanics.

**What you told me:** Inject io/default/main, hard-code no Dispatchers in classes,
use Room's suspending transaction API for gated work, prohibit
`allowMainThreadQueries`, and never swallow CancellationException in generic catches.

**Why it matters on Android:** Main-thread database work causes responsiveness
failures; changing dispatcher does not change ownership; swallowed cancellation
can keep cancelled work running and break structured cleanup.

**General rule:** Assign dispatchers by responsibility, select concrete ones only
at the root, keep transaction database work in Room's transaction context, and
rethrow cancellation before ordinary error handling. Capture time after the gate,
not before lock acquisition.

### W-16 — Specify results that complete after the screen disappears

**First draft / gap:** “Await and show the result” assumed the initiating screen
would still exist when an application-owned command finished.

**What you told me:** Drop transient “Progress saved” feedback if the screen left.
Information the user needs must remain understandable from the database.
Later close OQ-008: the period's final result and restored draft are sufficient;
no durable rejection receipt in V1.

**Why it matters on Android:** Navigation can discard the result recipient without
cancelling the underlying transaction. A durable domain outcome and a toast have
different retention needs.

**General rule:** Define delivery policy for orphaned results. Use existing durable
read models for required outcomes and drop ephemeral feedback. Decide explicitly
whether a rejection receipt is needed; do not turn every missed UI message into
new persistence.

### W-17 — Close periodic-worker timing instead of leaving an implementation choice

**First draft / repeated gap:** Worker names/policies were specified, but OQ-005
still deferred cadence/backoff to implementation after the creator asked for a
complete low-level design.

**What you told me:** Catch up every six hours, without constraints. The periodic
worker never returns retry; the next period, foreground, or command covers it.
Midnight period boundaries and Doze make this a timeliness optimization.

**Why it matters on Android:** Best-effort background work is not an exact alarm.
Unnecessary constraints or retries change delivery latency and can block work
without providing correctness.

**General rule:** Specify interval, constraints, retry behavior, enqueue owner,
and recovery triggers. Separate authoritative foreground/transaction correctness
from background timeliness. Preserve stored occurrence boundaries.

### W-18 — Use the outbox attempt budget, not the worker's run count

**First draft / follow-up gap:** “Exponential retry with five attempts” did not
specify the backoff base or which counter enforced the event limit.

**What you told me:** Use WorkManager's default exponential backoff from 30 seconds,
count against the outbox's five-failure limit rather than runAttemptCount, and
avoid old retries delaying newer APPEND_OR_REPLACE dispatches excessively.

**Why it matters on Android:** A work request can retry while new events arrive;
replacement work and foreground dispatch do not share runAttemptCount. Using
that count can prematurely exhaust new events or reset old events' budgets.

**General rule:** Make retry budget ownership durable and per event. Define
terminal outcomes and release a handled dispatch dependency when no eligible
retryable work remains. Test a worker run count that differs from event attempts,
fresh events on older requests, and fifth-failure transition to UNAVAILABLE.

### W-19 — Distinguish nominal backoff duration from an Android timing guarantee

**First draft / qualification:** The final timing instruction described a whole
retry window of about 7.5 minutes. I checked and qualified that estimate rather
than recording it as a wall-clock bound; this was my verification finding, not
another error the creator had to discover.

**What you told me:** Choose default 30-second exponential backoff with the
outbox's five-failure limit. The accepted design records the timing qualification.

**Why it matters on Android:** Four waits total 30 + 60 + 120 + 240 = 450 seconds,
but execution time, Doze, and scheduler deferral can extend elapsed time.

**General rule:** Calculate the nominal sequence and label scheduling assumptions.
A bounded attempt budget is not a guaranteed completion deadline. Do not promise
exact timing from WorkManager or treat the estimate as evidence from a test run.

### W-20 — Keep quiet hours in product definition

**First draft / completion:** Closing worker timing could have encouraged resolving
notification delivery policy in the same technical pass.

**What you told me:** Record quiet hours as a product question outside the design.

**Why it matters on Android:** Quiet hours change user-visible alert behavior,
including when a missed-result notification may appear; a backoff choice does
not authorize that product change.

**General rule:** Separate scheduling mechanics from notification policy. Name the
external product question and owner; do not add suppression rules, tables, or
new delivery windows as an implementation convenience.

### W-21 — Define notification identity and visible-notification lookup concretely

**First draft / follow-up gap:** OQ-007 left stable Int notification identity and
visible-notification enumeration unresolved. “Stable notification ID” did not
specify the tag, exact mapping, or overflow behavior.

**What you told me:** Post with tag `habit-missed-result` and
`Math.toIntExact(habitId)` as ID; fail fast on overflow. Use
NotificationManager.getActiveNotifications(), available at minimum API 24,
filtered by the exact tag.

**Why it matters on Android:** Android identifies a notification by tag and ID.
A different cancellation identity strands notifications; lossy ID conversion can
collide; broad enumeration/cancellation can affect unrelated app notifications.

**General rule:** Register the exact post/update/cancel identity, lookup API,
minimum-API support, and conversion failure policy. Preserve the same pair on
every effect path. Check overflow before platform calls and do not retry it as
transient delivery failure; ignore other tags during foreground cleanup.

## 3. Who creates objects?

### O-01 — Select the composition mechanism and record its reconsideration trigger

**First draft / gap:** The architecture deferred manual composition versus a DI
library until implementation planning. “Just inject it” left a core decision to
the coding agent.

**What you told me:** Use a lazy AppContainer owned by HabitTrackerApplication
for this small single-module app. Record Hilt as the alternative; revisit at a
multi-module split or when the container becomes hard to maintain. Make ADR 0003.

**Why it matters on Android:** The framework creates entry points, so dependency
construction needs a real bridge into the app's graph. Ad hoc constructors do
not determine which graph an Activity or worker receives.

**General rule:** Select the construction mechanism, owner, alternatives, and
reopening trigger in the design. Record a durable core-pattern decision in an
ADR; do not defer it merely because wiring will be implemented later.

### O-02 — Register the graph's singleton ownership and database-opening path

**First draft / gap:** “A simple composition root constructs things” did not
identify process-wide instances or when construction became expensive.

**What you told me:** The container owns Clock, dispatchers, application scope,
Room with SchemaGuards, repository, rule services, commands, notifier and
work-scheduler ports, and BoundaryTicker.

**Why it matters on Android:** Independently constructed clocks, databases,
serializers, or scopes can make foreground and headless work disagree. Opening
Room through a main-thread lazy getter can still block startup.

**General rule:** List graph instances, lifetimes, and acquisition behavior.
Separate cheap lazy container construction from one shared suspending database
initialization off main; include production converters and guards in that path.
Concurrent first callers must receive the same instance.

### O-03 — Give every framework-created object a dependency path

**First draft / gap:** Ordinary constructor injection did not close the Application,
Activity, ViewModel, WorkManager worker, or notification-entry paths.

**What you told me:** Add a table of every object Android creates and how it gets
its dependencies.

**Why it matters on Android:** Android does not call arbitrary dependency-filled
constructors for an Application or Activity. AndroidX creates ViewModels and
workers using its own providers and factories.

**General rule:** Require an Android-created-object register covering constructor
owner, factory/provider, graph lookup, and initialization timing. Include cold
notification entry and headless workers, not just an already open Activity.
Components receive explicit dependencies; only entry points/factories resolve
the container.

### O-04 — Use saved-state-aware ViewModel creation

**First draft / gap:** A decision to use SavedStateHandle did not guarantee that a
production factory supplied Android's restored handle.

**What you told me:** Create ViewModels with `viewModelFactory`, CreationExtras,
and `createSavedStateHandle()`.

**Why it matters on Android:** Manually creating an empty handle can pass a
constructor test while discarding framework restoration in production.

**General rule:** Specify the factory and destination owner together. Test the
actual saved-state-aware creation path as well as direct ViewModel construction.
The handle restored by navigation must be the one the ViewModel uses.

### O-05 — Connect WorkManager construction to the selected graph

**First draft / gap:** Saying workers call catch-up did not explain how WorkManager
could supply the real repository or a fake clock in a test.

**What you told me:** Use HabitTrackerWorkerFactory; Application implements
Configuration.Provider; remove WorkManager's default initializer so that factory
is used. Build worker tests through the real factory.

**Why it matters on Android:** A worker can be instantiated without an Activity.
Default worker construction cannot supply the app's constructor dependencies.

**General rule:** Document factory registration and merged-manifest changes,
not just the factory class. Remove only WorkManager initializer metadata,
preserve other Startup initializers, and resolve WorkManager lazily to avoid
configuration recursion. Verify both worker types through the actual factory.

### O-06 — Keep startup safe when Android launches a screenless process

**First draft / gap:** App startup and ticker setup could be interpreted as eager
Application work or work dependent on a foreground Activity.

**What you told me:** Keep Application.onCreate light; create the notification
channel, lazily construct the container/database, and start the ticker only
when the process is STARTED. Android can start a worker or notification entry
without a warm screen.

**Why it matters on Android:** Eager database work delays cold startup and runs
unnecessary foreground machinery in a headless worker process.

**General rule:** Define cheap entry-point setup versus deferred acquisition and
foreground activation. A lightweight process-observer registration must not
open Room or start catch-up synchronously. Headless worker dependency creation
must not require a previous foreground callback.

### O-07 — Make the clock an injected authority, including the zone

**First draft / gap:** TimeSnapshot and delayed ticker behavior did not prohibit
components from obtaining real time or the default zone independently.

**What you told me:** Clock exposes `now()` and `zone()`. Nothing calls
Instant.now, System.currentTimeMillis, or ZoneId.systemDefault directly.
TimeSnapshot is still captured inside the transaction after the gate.

**Why it matters on Android:** The user can change clock/zone while the app is
running. Hidden time reads undermine reproducible boundary tests and can make
one transaction use inconsistent time authorities.

**General rule:** Inject both instant and zone access through one interface.
The production adapter observes the current zone rather than caching startup
state. Commands take one transaction snapshot after serialization. Merely naming
a Clock is insufficient if other code still reads the platform clock directly.

### O-08 — Pair dynamic receiver registration with process lifetime

**First draft / gap:** A time-change signal was conceptually required, but receiver
registration API, export policy, and unregister ownership were not concrete.

**What you told me:** Use ContextCompat.registerReceiver with
RECEIVER_NOT_EXPORTED when the process starts, and unregister when it stops.

**Why it matters on Android:** Receiver policy and lifecycle leaks are platform
behavior, not ordinary pure Kotlin concerns. Registration must not multiply with
Activity recreation.

**General rule:** Register Android callback APIs, Context ownership, flags,
activation conditions, and cleanup pairing. Make callbacks signal the injected
coordinator instead of doing heavy synchronous work. Verify broadcasts on the
supported device APIs.

### O-09 — Name who schedules unique work and when

**First draft / gap:** Worker existence did not specify the enqueue owner,
unique names, or replacement policies.

**What you told me:** Enqueue periodic catch-up at app start as unique periodic
work with KEEP; list all unique work names and policies.

**Why it matters on Android:** Activity recreation or repeated foreground entry
can otherwise enqueue duplicate workers. Keeping an already-running one-time
dispatch request can also discard a needed later drain request.

**General rule:** Require a work register with name, worker, kind, policy, owner,
and enqueue trigger. This design uses `habit-tracker.reconcile.periodic.v1` with
KEEP and `habit-tracker.notifications.dispatch.v1` with APPEND_OR_REPLACE through
the work-scheduler port. W-17/W-18 close the later timing and retry decisions.

### O-10 — Place every newly named class and port in the existing package layout

**First draft / follow-up gap:** The four sections introduced classes and ports
without one explicit mapping to the architecture's package layout.

**What you told me:** Add a short placement table for AppContainer, CommandRunner,
the dispatcher holder, ForegroundCoordinator, HabitTrackerWorkerFactory,
notifier/work-scheduler/permission ports, and test FakeClock.

**Why it matters on Android:** Framework types can otherwise leak into domain
contracts, and different implementers can place the same responsibility in
unrelated layers. A test clock accidentally included in production is a
construction-boundary mistake.

**General rule:** Every new named component gets a package/source-set location
and role. Domain ports expose plain values; Android adapters own Context,
NotificationManager, WorkerParameters, and WorkManager. Test fixtures remain
in test source sets and are shared without duplicating time/rule logic.

### O-11 — Promote approved Android contracts into a durable design artifact

**First draft / requested completion:** Temporary scratch notes were deliberately
authorized initially. Once the decisions were ready, they still needed formal
placement and an ADR rather than remaining throwaway notes.

**What you told me:** Create the low-level design with the three additional
sections, each ending in verification obligations; record wiring as ADR 0003
using the ADR template; link it; delete the scratch file.

**Why it matters on Android:** State restoration, cancellation, framework factories,
and real SQLite verification are design contracts that guide implementation.
They cannot be treated as optional notes outside the authoritative design.

**General rule:** Respect artifact maturity: temporary capture first when requested,
then a formal design and focused ADR once authorized. Carry decisions and their
verification with them. The initial scratchpad was appropriate; failing to
promote it after authorization would not have been.

### O-12 — Remove stale scratch references and update the whole decision record

**First draft / follow-up gap:** After scratch deletion, references to it remained;
resolved discussions and proposal status also needed synchronized cleanup.

**What you told me:** Remove references to the deleted scratchpad, rewrite C-002
against the new keys, mark C-001/C-002/C-003 resolved with decisions, and update
the decision lock and verification obligations.

**Why it matters on Android:** Stale references or contradictory status labels
leave a coding agent unsure whether to use the old recovery/admission contract
or the accepted one.

**General rule:** A resolution is incomplete until all affected registers,
sequences, findings, statuses, links, and tests agree. Search for obsolete
references and old conditional/proposed wording after every decision round;
deleting a file alone is not document cleanup.

### O-13 — Check source authority without rewriting accepted records

**First draft / required review:** Adding concrete Android mechanics raised
consistency questions with architecture and ADR 0002. Later decisions also had
to coexist with ADR 0003's narrower historical scope.

**What you told me:** List contradictions rather than changing architecture.
For the follow-up resolutions, do not edit architecture, ADR 0002, or ADR 0003.
Record the undo wording note for the architecture owner.

**Why it matters on Android:** Platform wiring must implement the approved domain
lifecycle, not quietly change scoring, freeze behavior, or durable delivery
states in a separate low-level document.

**General rule:** Perform a cross-document consistency review with explicit
findings. Preserve protected accepted sources and separate a later creator
resolution from an ADR's original scope. A source's general wording must not
silently override its specific invariant; see W-14.

### O-14 — Do not use open questions to avoid implementation-critical decisions

**First draft / repeated gap:** My first formal design exposed OQ-001 through
OQ-008 rather than silently choosing unresolved behavior. That was honest, but
still left key, ordering, identity, harness, and timing choices for the coding
agent. After one resolution pass, harness/timing still remained open.

**What you told me:** Initially, put undecided items in Open questions. On review,
explain that these choices defeat an implementation-ready document, supply the
resolutions, retain only OQ-004/OQ-005 temporarily, and finally close those too.

**Why it matters on Android:** Defaults at these seams can lose inputs, cancel
saves, block Undo, or produce passing tests on the wrong SQLite. They are not
innocuous implementation preferences.

**General rule:** Expose unresolved material choices rather than inventing them,
but obtain their decisions before calling the design implementation-ready.
A deliberate deferral needs an owner and reopening event. Close approved items
everywhere and leave product questions, such as quiet hours, outside the
technical decision list.

## 4. What is real versus fake in tests?

### T-01 — Make the real database, guards, repository, and commands the rule seam

**First draft / gap:** The architecture said “Repository test” without naming the
database or real components. That allowed a fake repository to appear to satisfy
an invariant actually enforced by SQLite.

**What you told me:** Use real pure rule services, an in-memory Room database
with production SchemaGuards/converters, the real repository, and real commands.
Add a component-by-component seam register with injection path and reason.

**Why it matters on Android:** A fake has no actual trigger or gate. It cannot
prove late-save rejection, exactly-once finalization, or non-negative balance
even if it reproduces the expected result.

**General rule:** Test an invariant where it is enforced. State the actual database,
schema callbacks, converters, repository, commands, and execution topology.
A named test category is not evidence until its seam is explicit.

### T-02 — Fake external inputs and effects, not the rules

**First draft / gap:** “Use test doubles” did not identify which components were
allowed to be fake or how they entered the graph.

**What you told me:** Fake Clock, dispatchers/application scope, notifier and
work-scheduler ports, and notification permission/channel state. Ports record calls;
FakeClock shares virtual time with the test scheduler.

**Why it matters on Android:** Real permissions, notification services, wall time,
and scheduled jobs make rule tests nondeterministic. Replacing the database/rules
instead removes the very enforcement under review.

**General rule:** Put variability at explicit injected ports. Record post/cancel/
enqueue calls and inject failure states, while keeping production domain rules
and persistence real. Name the fake and its constructor/container injection path.

### T-03 — Allow a fake repository only for UI mapping; prohibit rule-test mocks

**First draft / gap:** A default fake or mocking-library approach was possible;
the baseline had no explicit restriction. This session did not actually introduce
such mocks, so the correction was preventive.

**What you told me:** A fake repository is allowed only in ViewModel UI-mapping
tests and must not reimplement rules. Do not mock Context, Room DAOs, or repository
in rule tests; a mocking library is unnecessary.

**Why it matters on Android:** Mock DAO calls can prove an interaction occurred
while proving nothing about real SQL, trigger creation, or transaction behavior.

**General rule:** Label mapping-only fakes and limit their claims. Use prepared
read models/results, not another planner/scoring implementation. Supply a real
harness Application Context whenever Room or Android framework behavior matters.

### T-04 — Assert public behavior instead of implementation trivia

**First draft / gap:** ViewModel test style was not constrained; emission counts,
private methods, or private composables could have become acceptance evidence.

**What you told me:** Send events and assert resulting UiState and recorded port
calls. Do not assert StateFlow emission counts or private implementation details.

**Why it matters on Android:** StateFlow conflation and lifecycle collection affect
emissions without changing visible behavior. Private-method tests break during
refactoring while missing real user outcomes.

**General rule:** Drive public events and observe state, command results,
persisted evidence, and external calls appropriate to the seam. Do not use a
mapping-only repository fake to claim a real after-commit effect happened.

### T-05 — Map every architecture verification label to its concrete seam

**First draft / gap:** The verification column named Repository and Outbox tests
without linking each requirement to real/fake components.

**What you told me:** Map every such reference to the seam register and give each
formal design section its verification obligations.

**Why it matters on Android:** A broad “tests added” statement can conceal that
notification delivery used a fake service or a gate claim used a fake repository.

**General rule:** Maintain requirement → invariant → fixture → public assertion
traceability. In this design R-memory, R-file, and O name the graph/topology.
Port fakes prove orchestration; device checks prove actual platform notification,
route, lifecycle, and backup behavior.

### T-06 — Test guard catalogs and behavior after every migration

**First draft / gap:** Repository checks did not specify how migrations preserve
guard triggers installed by Room's onOpen callback.

**What you told me:** Add migration tests checking that every guard trigger still
exists after each migration.

**Why it matters on Android:** Room's table-schema validation does not establish
that trigger enforcement survived. A migration can preserve readable rows while
losing future write protections.

**General rule:** Migrate real versioned files, reopen through the production
callback, compare trigger names and definitions via sqlite_master, and attempt
invalid and valid writes. Check every supported path. IF NOT EXISTS alone cannot
replace an old trigger body; changed guards need explicit replacement and tests.

### T-07 — Use file-backed, real-thread contention for races and gates

**First draft / follow-up requirement:** In-memory Room was a good ordinary rule
fixture, but its serialization could hide a missing or broken database gate.

**What you told me:** Concurrent triggers and Undo versus expiry use a temporary
file-backed database, production journal mode, and real threads. Other repository
tests may remain in-memory.

**Why it matters on Android:** A passing race under one connection's serialization
says little about competing production entry points.

**General rule:** Specify contention topology, not just a concurrency test name.
Use production WAL, independent Room instances/executors on the same file,
barriers for both arrival orders, and persisted/trace evidence that the gate
preceded time capture and domain work. SQLite single-writer behavior alone is
not proof that the application gate statement exists.

### T-08 — Control Room executors separately from coroutine dispatchers

**First draft / gap:** Injecting test dispatchers could be mistaken for making
all Room operations run on virtual time.

**What you told me:** Decide query/transaction executor setup; Room does not run
on the test dispatcher by default, and tests need deterministic coordination.

**Why it matters on Android:** Coroutine advancement can finish while real
query/transaction threads are still executing. Uncontrolled completion ordering
creates flaky assertions or falsely suggests work never committed.

**General rule:** Explicitly configure dedicated off-main query and transaction
executors, await database completion, and coordinate time changes with barriers.
Do not mix incompatible Room execution configuration paths. Own scope shutdown,
database close, executor termination, and file cleanup; avoid sleeps and main-thread
queries as synchronization shortcuts.

### T-09 — Bind fake time to the same scheduler that drives ticker delay

**First draft / gap:** The architecture promised a fake-clock boundary test, but
the ticker delayed until the boundary. A separately advanced fake instant did
not cause the coroutine delay to finish.

**What you told me:** Compute ticker wait from injected Clock and delay on an
injected dispatcher. FakeClock reads the test scheduler's virtual time so clock
advancement and delay advancement move together.

**Why it matters on Android:** A test that advances only a fake date can leave the
UI ticker asleep in real time and never exercise foreground finalization.

**General rule:** Design timer testability as a coupled clock/scheduler contract.
Use a base instant plus TestCoroutineScheduler.currentTime, shared dispatchers,
finite advance/runCurrent steps, and separately awaited Room completion. Do not
run unbounded advanceUntilIdle against a forever-rearming ticker.

### T-10 — Replace the entire graph before Android initializes entry points

**First draft / gap:** “Inject a fake clock” did not explain how the already
constructed Application, ViewModels, lifecycle observer, or workers received it.

**What you told me:** Tests must swap the container. A custom instrumented runner
creates a test Application using FakeClock, test dispatchers, and a temporary
database. Worker construction also uses the real factory.

**Why it matters on Android:** A production singleton initialized before the
test override can retain real time/database instances even when one later
constructor receives a fake.

**General rule:** Install the test graph before lifecycle/factory/WorkManager
initialization. Instrumented platform callbacks keep the real Android UI thread;
injected app work uses controlled scheduling. Host ViewModel tests also replace
Main for viewModelScope and reset it in teardown. Verify no production graph is
left cached in a test entry path.

### T-11 — Verify save lifetime with a real commit after ViewModel clearing

**First draft / gap:** Scope declarations did not prove the command was detached
from the screen's cancellation tree.

**What you told me:** Start SaveProgress, clear the ViewModel before it finishes,
and verify the progress row still commits.

**Why it matters on Android:** A superficially application-scoped design can
still nest work under a caller-owned job or cancel the effect with the waiter.

**General rule:** Test the lifecycle failure mode, not just job configuration.
Use a real runner/command/Room transaction with an explicit barrier; clear the
ViewModelStore, release execution, and inspect persisted progress, admission
release, surviving effects, and absence of departed-screen feedback.

### T-12 — Close the harness with separate host and device evidence tiers

**First draft / repeated gap:** OQ-004 intentionally left JVM/Robolectric/device
selection open, as initially requested. After the document matured, that still
left a consequential implementation choice. Host SQL alone could satisfy the
written checks without exercising minimum-API SQLite.

**What you told me:** Use Robolectric in src/test for ViewModels, in-memory
repository/outbox tests, and workers created through the real factory. Use
instrumentation at API 24 and API 36 for file-backed races/gates, migration and
guard catalogs, and everything requiring the custom test Application. A SQL-facing
guard/migration claim counts only after passing API 24.

**Why it matters on Android:** API 24 ships SQLite 3.9; host and Room compile-time
validation use newer SQLite. SQL accepted there can fail on the minimum device.

**General rule:** Bind each suite to source set, harness, Android API, SQLite
engine, and evidence threshold. Use platform SQLite on device, record sqlite_version,
and rerun host guard cases at API 24 before claiming guard evidence. An API 36
pass or Room compile-time check does not replace minimum-API execution.

### T-13 — Verify that requested device tooling supports the minimum API

**First draft / approved adjustment:** The requested final test matrix used
Gradle Managed Devices for API 24 and API 36. I checked the platform documentation
and found managed devices are documented for API 27+, then asked for a concrete
execution adjustment. This was not a creator-discovered defect in the draft.

**What you told me:** Approve a connected API 24 emulator/device via
`connectedDebugAndroidTest`, with a Gradle Managed Device at API 36. Keep the same
instrumented suites and mandatory API 24 evidence.

**Why it matters on Android:** An unsupported runner configuration can leave
minimum-API tests unexecutable while the design appears to require them.

**General rule:** Verify tooling support before locking an execution matrix.
Expose a real constraint and propose a concrete equivalent; do not silently
raise the minimum tested API. Select the connected API 24 device explicitly so
a higher-API device cannot accidentally satisfy that job.

### T-14 — Upgrade proposed tests to required tests and distinguish plans from evidence

**First draft / repeated completion:** The cleanup repair began as a proposal;
its tests had to change status when approved. Closing harness/timing also required
updating verification, not just deleting open-question rows.

**What you told me:** Make cleanup tests required, revise verification obligations
for accepted decisions, and preserve consistency across all four sections.

**Why it matters on Android:** A reviewed design can still leave its critical
crash-window, deadline, or minimum-SQL checks optional or on the wrong harness.

**General rule:** On acceptance, update the decision, execution sequence, fixture,
and required assertions together. Track unexecuted API 24 cases as unverified.
Documentation checks and declared obligations are not runtime-test results;
this session validated documents and did not execute the designed application tests.
