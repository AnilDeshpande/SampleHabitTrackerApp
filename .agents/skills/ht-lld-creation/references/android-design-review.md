# Android detailed-design review guide

Use the applicable checks while following [LLD Creation](../SKILL.md). Read an
area when its feature or risk is present; record why it is inapplicable otherwise.
The [LLD template](../../../templates/lld-template.md) captures the results.
These checks close concrete contracts; they are not a demand to add every named
mechanism or to ask the creator every question.

Contents:

- [State ownership and restoration](#state-ownership-and-restoration)
- [Work lifetime and concurrency](#work-lifetime-and-concurrency)
- [Construction and dependency wiring](#construction-and-dependency-wiring)
- [Test seams and evidence](#test-seams-and-evidence)
- [Handoff review](#handoff-review)
- [Learning coverage](#learning-coverage)

## State ownership and restoration

### State inventory

Inventory every affected screen's state, including loading, errors, eligibility,
operation status, dialogs/sheets, drafts, history, countdowns, and effects.
Classify each value once:

| Class | Ownership check |
| --- | --- |
| Durable | Saved business records and confirmed lifecycle/deadline facts belong to the approved durable store; reload them. |
| Restorable input | SavedStateHandle owns unsaved business input plus minimal initialization/target metadata for the destination. |
| Derived | Rebuild validation, projections, eligibility, countdowns, and busy UI from their owners; UiState is not a second mutable draft. |
| Transient effect | Navigation, action feedback, permission/focus requests, and accepted-job execution are not restored/replayed effects. |
| Visual-only | rememberSaveable owns small presentation choices that cannot change the command payload. |

Do not infer process-death guarantees from a ViewModel's survival across
configuration changes. Screens with no unsaved input explicitly need no saved
keys; do not serialize their Room-backed lists or history. Process-local runner
status is observed on screen reopening, not saved as an unfinished job.

### Saved inputs and initialization

For each ViewModel, list exact keys, primitive types, enum/bitmask encodings,
absent-key defaults, write events, and reset conditions. Distinguish route
arguments from application-owned keys. Match durable encodings where appropriate;
never default over an intentionally empty restored field.

Preserve raw text, including invalid/incomplete values, rather than BigDecimal
or a normalized number. Derive validation messages using current validation
inputs. Keep the Bundle payload minimal; exclude entities, lists, full UiState,
history, and errors. If draft size cannot be bounded, resolve a suitable durable
draft mechanism rather than claiming saved state can carry an arbitrary payload.

Specify create defaults and edit first-load seeding separately. A restorable
initialization marker prevents later Room emissions from overwriting edits.
Test flag absent/false and already true, including empty/invalid restored fields.
Also re-read durable status so restoration cannot make a deleted/retired target
editable.

### Target identity and navigation

Save the stable identity of the business target the input belongs to, not just
its parent entity. Initialize input and target together. Reconcile/reload as
required by architecture, then compare restored target identity with current
eligibility. If it closed or became unavailable, keep the input and show the
approved late/read-only outcome; do not silently retarget it.

Trace that same identity into an immutable command request at submission and
into the transaction's eligibility checks. A correct restored ID is insufficient
if the command looks up whichever target is current at execution.

Register every affected destination, route arguments, ViewModelStoreOwner,
SavedStateHandle owner, and entry/return behavior. A sheet/dialog's visual form
does not decide its owner; independent business drafts need a deliberate owner.
Define external notification/deep-link cold and warm entry, back-stack behavior,
argument validation, and acknowledgement/read-only policy where applicable.

### Restoration boundaries and evidence

Define rotation, retained-task system process death, user task removal, and
fresh-launch outcomes separately. Saved-state lifecycle capture is not durable
command execution. Persist inputs at their input events; consult the framework's
capture contract before relying on writes made while stopped. Never replay an
accepted command, navigation, snackbar, or permission launch on restoration.

Require fresh ViewModels with pre-filled handles and public UiState assertions.
Cover invalid text, edit initialization, stale target identity, no-input screens,
and no effect replay. Verify the real saved-state-aware factory path as well as
constructor tests. For retained-task device restoration, background to STOPPED,
kill the background process with `adb shell am kill <package>`, verify it ended,
and reopen from Recents. Rotation/force-stop do not substitute; separately check
user task removal and durable data retention.

## Work lifetime and concurrency

### Scopes and cancellation

Register owner, dispatcher/execution context, caller cancellation behavior,
screen/background lifetime, and process-death recovery for every applicable
operation. Lifecycle-aware UI collection observes state; viewModelScope owns
screen-only loading/derivation/validation. Decide the owner of accepted mutations
from their required lifetime rather than defaulting to the initiating screen.

Where accepted work must outlive a screen, inject an application-owned scope
with failure isolation and a defined dispatcher. The ViewModel awaits a result;
cancelling that wait must not cancel the work. A caller-owned withContext(io)
changes execution context, not ownership. Supervision isolates failures but does
not itself handle unobserved exceptions; specify error handling after departure.

Application work does not survive process death. If eventual execution is required,
choose durable scheduling/state under the approved product contract. WorkManager
workers remain owned by WorkManager's cancellation lifecycle; do not detach them
into an untracked application job. Define ticker activation separately from the
application scope's overall lifetime.

Inject dispatchers; select concrete ones at the root. Use Room's suspending
transaction mechanism when Room owns the transaction, keep database operations
in that context, prohibit main-thread queries, and propagate CancellationException
before handling ordinary failures. Changing dispatcher is not proof of off-main
lazy database acquisition or correct transaction context.

### Admission and transactional order

Provide command signatures with concrete identity/raw-input types, immutable
capture point, results, eligibility predicates, durable outcome, and failure
semantics. Register each admission key, collision rationale, atomic reservation,
and release point. Reopened screens observe operation-owner busy state rather
than a new screen-local flag.

Distinguish transaction ownership, the caller's wait, and after-commit effects.
When admission is intended only to prevent duplicate writes, release at
transaction commit/failure; platform effects are separate jobs that hold no key.
Decide which competing commands need distinct keys so a slow effect cannot block
a deadline-sensitive action. Admission is not a replacement for database
serialization or conditional writes.

Write the transaction order explicitly: serialization/gate, time snapshot,
reconciliation, conditional transitions/writes, commit. Verify exceptions using
the specific lifecycle invariant. For example, a frozen object that must resume
before catch-up cannot safely follow a blanket catch-up-before-write order.
If source wording conflicts, expose the finding and obtain its disposition;
do not invent an extra pass or silently rewrite an accepted ADR.

Describe death before commit versus after commit/before feedback, resubmission,
and duplicate behavior. No success before commit. Decide whether cross-process
idempotency tokens are needed; do not add them merely because process-local
admission ends at death. Schedule committed effects even if the command's own
business result was rejection but reconciliation committed other changes.

### Effects and foreground recovery

For each database/platform gap, define the committed facts, effect identity,
idempotency, retry/repair source, and recovery trigger. After-commit failure does
not roll back saved data or turn an already committed result into save failure.
A new cleanup/repair behavior is a proposal until approved, even if technically
convenient.

Reuse the complete existing eligibility/suppression predicate when appropriate.
Cover all implicated lifecycle states, not just acknowledgement/zero-count cases.
A durable schema/state addition needs justification when existing records already
provide a recovery predicate.

Specify one serialization path for related posts, updates, cancellations, and
cleanup; recheck eligibility at the effect boundary. Consider a newly committed
eligible event during cleanup and slow effects after a competing transition.
State when recovery happens; foreground repair does not promise work while dead.

Register exact platform notification identity, post/update/cancel mapping,
enumeration/filtering API, numeric conversion/overflow behavior, and API support.
Avoid lossy mappings or broad cancellation of unrelated notifications. An invariant
conversion failure must not enter a transient delivery retry loop.

For application-wide foreground maintenance use one process-lifecycle coordinator,
not per-Activity resume/start hooks. Define ensure-work, catch-up, dispatch/repair,
and ticker re-arm order as applicable, stop/unregister pairing, repeated-entry
serialization, and how other architecture triggers request the same coordinator.

### Scheduled work and results

For each durable job name kind, unique name/policy, enqueue owner/event, concrete
cadence/constraints, retry result, budget owner, backoff, and terminal behavior.
Evaluate lost requests while a prior job is running and dependency-chain blocking;
do not prescribe KEEP or APPEND_OR_REPLACE without that rationale.

Distinguish per-event durable failure counts from per-request runAttemptCount.
Foreground attempts, replacements, and newly arrived events may not share the
worker counter. Test differing counters, new events on an older retry request,
terminal events, and release of a handled exhausted dispatch chain.

Calculate nominal backoff waits and label execution/scheduling assumptions.
Doze and platform scheduling prevent treating that sum or a periodic interval
as a wall-clock guarantee. Foreground/command correctness and background
timeliness have separate contracts. Product-visible quiet hours or delivery
windows require product definition, not an inferred scheduling policy.

Define results with no remaining screen: drop ephemeral feedback, expose required
outcomes through durable read models, and explicitly decide whether a rejection
receipt is necessary. Do not add persistence for every undelivered toast.

## Construction and dependency wiring

### Graph and framework factories

Select composition mechanism, rationale, alternatives, and a concrete revisit
trigger. Register instance lifetimes and construction/acquisition paths for time,
dispatchers/scopes, storage, rules, repository/commands, coordination, ports,
platform adapters, factories, and timers that actually exist.

For every Android/AndroidX-created entry point identify its dependency bridge:
Application, Activity, destination ViewModel provider, worker factory/configuration,
callbacks, and external entries. Constructor injection alone is not that bridge.
Use saved-state-aware ViewModel factories with CreationExtras/createSavedStateHandle
where appropriate; an empty manually created handle bypasses restoration.

If custom WorkManager factories are selected, specify Configuration.Provider or
the chosen equivalent and required merged-manifest initialization changes for
the installed version. Preserve unrelated Startup initializers and avoid duplicate
initialization or lazy configuration recursion. Worker construction cannot depend
on a prior Activity/foreground callback. Test through the real factory.

Map every newly named class/port to the architecture's package layout and source
set. Domain ports expose plain values; adapters own Android types. Test clocks
remain test-only and reusable across harnesses without copied time rules.

### Startup, time, and callbacks

Separate cheap Application setup from heavy acquisition and foreground-only
activation. Thread-safe lazy container construction does not make a database
property getter safe on main. Specify shared suspending off-main initialization,
callbacks/converters, and concurrent first-caller behavior.

Cover screenless worker startup and cold/warm notification entry. No eager graph,
database open, catch-up, or foreground ticker belongs in lightweight Application
setup. A lightweight process-observer registration should defer heavy work.

Use one injected instant/zone authority and document zone freshness, transaction
snapshot location, ticker wait calculation, and time-change re-arm behavior.
Prohibit hidden platform time reads outside the production adapter. If a gate
exists, capture time after acquiring it so a waiting contender uses current time.

For each dynamic receiver/observer, specify application Context ownership,
version-compatible registration API/flags, lifetime, idempotent setup, unregister
pairing, and lightweight callback behavior. Verify relevant platform broadcasts
on required supported APIs rather than inferring behavior from fake calls.

### Artifact and decision ownership

The skill owns the method, the agent orchestrates, and the template holds output.
Keep those responsibilities separate. Audit breadth and preserve decision
provenance before declaring the implementation contract complete.

Temporary notes are authorized aids only. Promote them when requested; preserve
decisions and remove references to deleted notes. After each resolution, search
for stale proposal status, obsolete key schemes, closed findings still open,
and tests still marked optional. Check all related registers rather than only
editing the answer's original paragraph.

Implementation-critical choices must close before the formal handoff. Nonblocking
future decisions need reason, owner, and reopening event. Preserve approved
architecture/ADRs; record source discrepancies with their owner's disposition.
Existing drafting authority and accepted decisions do not expire between turns.

## Test seams and evidence

### Real enforcement and controlled ports

Name each component as real, fake, or mapping-only and explain injection path,
reason, and permitted claim. Keep pure rule services real. Where SQLite/Room
enforces an invariant, use actual production-configured storage, guards,
converters, repository, and commands; a fake repository/DAO/Context cannot prove
that invariant. Use a real harness Context for framework/database tests.

Control variability at Clock, dispatchers/scopes, notifier/work-scheduler ports,
and permission/channel-state providers. Recording fakes should model external
responses/failures, not duplicate the domain rules. A mapping-only repository
fake serves prepared read models/results; it is not evidence that a real commit
or effect happened. No mocking library is needed solely for this seam.

### Executors, contention, and migrations

State actual Room/database execution configuration rather than assuming coroutine
dispatcher injection controls it. Choose query/transaction executors or the
version-supported coroutine-context path coherently, not conflicting builder
settings. Use explicit completion signals/awaits and off-main database work.
Own scope cancellation, database close, executor termination, and file cleanup.

In-memory/single-connection serialization cannot prove production contention.
Race/gate fixtures use temporary files, production journal mode, real threads,
independent connections/Room instances and executors when needed, and barriers
for relevant arrival orders/deadlines. Verify the effective mode. SQLite's own
single-writer ordering can mask a missing application gate: inspect gate updates,
ordering/time capture, rollback, and a contender whose clock advances while waiting.

For each supported migration path, use versioned files and production opening
callbacks. Verify every guard's name/definition and invalid/valid write behavior,
not merely Room table-schema validation. Trigger creation with IF NOT EXISTS
does not replace an outdated body; changed guards need explicit replacement.
Test retained data and supported intermediate migrations as applicable.

### Virtual time and framework graph

If delays depend on Clock, bind FakeClock to the same virtual-time scheduler
that drives injected test dispatchers. Advance finite intervals/runCurrent;
forever-rearming timers make unbounded advanceUntilIdle inappropriate. Await
real database threads separately. Coordinate race clocks through thread-safe
barriers/checkpoints, not uncontrolled concurrent scheduler mutation.

Install a test container before Application/lifecycle/factory/WorkManager first
use when those entry points need replacement. A custom runner/test Application
must replace the whole graph; a late injected fake cannot un-cache a production
singleton. Host ViewModel tests control Main and reset it; instrumented callbacks
retain Android's UI thread while injected app work uses the test scheduler.

### Harness compatibility and assertions

Choose concrete suites/source sets/runners and a supported API/device matrix.
Host tests may use real SQLite with a different engine from the minimum Android
API and build-time SQL validation; neither proves minimum-platform compatibility.
Guard/migration SQL claims need required minimum-API platform SQLite evidence,
not a newer bundled driver. Record API and sqlite_version with results.
Verify that the selected managed/connected-device tooling supports each API;
propose an equivalent mechanism when it does not, retaining the evidence target.
Do not adopt this example's API numbers or Robolectric tier without project fit.

Drive public events and assert UiState, command results, persisted rows, and
recorded port effects as appropriate. Do not count StateFlow emissions or inspect
private functions/composables. Map every architecture Repository/Outbox label to
its real seam and observable assertion. Fakes cannot prove actual notification
service behavior, lifecycle restoration, or cold-entry back-stack behavior.

Where a command must outlive its screen, hold a real transaction at a controlled
barrier, clear the ViewModelStore, release it, and assert commit, admission release,
and no departed-screen feedback. Test slow post-commit effects against timely
competing actions and both crash windows. Accepted repairs and closed timing
choices must have required tests, not lingering proposed obligations.

## Handoff review

Before drafting/handoff, walk applicable paths end to end:

| Scenario | Passing design behavior |
| --- | --- |
| Invalid draft restored after system process death | Raw input and target metadata return through the actual destination handle; validation is recomputed and edit loading does not overwrite the draft. |
| Target closes before restored or delayed submission | The saved target is the submitted target; transaction eligibility rejects it without moving old input to a newer target. |
| Screen leaves during accepted work | Waiter cancellation does not cancel the chosen operation owner; durable commit and relevant effects remain observable. |
| Slow effect versus deadline-sensitive opposite action | Admission collision/release rules permit the intended action; gate/deadline and command-specific order determine the result. |
| Process death before/after commit | Uncommitted changes disappear; committed data survives; orphaned feedback and resubmission have an accepted policy. |
| Clock advances across a foreground boundary or worker run | Clock/delay share controlled time, real database completion is awaited, and all framework entry points receive the test graph. |
| Host SQL succeeds but minimum API differs | Required platform SQLite suites must pass before claiming guard/migration compatibility; supported runner/device selection is explicit. |
| Simple feature without async effects/notifications | Relevant contracts are filled; inapplicable machinery is explained and omitted rather than invented. |
| Material choice is unresolved | The decision lock fails and targeted discussion continues; no formal complete/Accepted artifact hides the choice. |
| Prior choices and drafting request are already approved | Carry them forward; do not insert a redundant permission gate or restart the questionnaire. |

Also check source traceability, ADR impact, route/key/command identity, package
boundaries, shared foreground/headless graph, current proposal/finding status,
links to temporary notes, and required versus executed evidence. A walkthrough
is a design exercise, not a device/test pass.

## Learning coverage

Provenance: [62 session learnings](../../../../docs/design/habit-tracker-v1-lld-refinement-learnings.md).
The [accepted Habit Tracker LLD](../../../../docs/design/habit-tracker-v1-low-level-design.md)
is a worked example, not mandatory reading for every task. Its values are not
universal defaults. This table maps each correction to an actionable check above
and a template/verification destination; it preserves the learning without
copying project-specific policy into the method.

| Learning | Generalized check | Output / evidence destination |
| --- | --- | --- |
| S-01 | [Inventory all affected state](#state-inventory), not just one explicitly durable row. | State ownership register; recreation evidence. |
| S-02 | [Give business input one owner](#state-inventory). | State ownership and saved-key registers. |
| S-03 | [Specify keys, types, defaults, encodings](#saved-inputs-and-initialization). | Saved-state key register. |
| S-04 | [Keep invalid raw input](#saved-inputs-and-initialization). | Key representation; invalid-input restoration test. |
| S-05 | [Protect edits from reseeding](#saved-inputs-and-initialization). | Initialization contract and flag-true/false tests. |
| S-06 | [Restore business target identity](#target-identity-and-navigation). | Saved metadata and stale-target handling. |
| S-07 | [Revalidate against current durable state](#target-identity-and-navigation). | Restoration eligibility and late/read-only evidence. |
| S-08 | [Explain no-input screens](#state-inventory). | None-with-reason entries; durable reload assertions. |
| S-09 | [Assign a real destination owner to drafts](#target-identity-and-navigation). | Route/ViewModel/handle register. |
| S-10 | [Bound saved payload](#saved-inputs-and-initialization). | Primitive-only key register or approved alternate draft store. |
| S-11 | [Separate lifecycle paths and one-off effects](#restoration-boundaries-and-evidence). | Restoration/non-restoration policy. |
| S-12 | [Test recreation, not just rotation](#restoration-boundaries-and-evidence). | Fresh-handle tests and retained-task device check. |
| S-13 | [Cover routes and cold/warm external entry](#target-identity-and-navigation). | Complete navigation register and back-stack checks. |
| W-01 | [Classify operation lifetimes](#scopes-and-cancellation). | Scope/cancellation register. |
| W-02 | [Separate waiting from ownership](#scopes-and-cancellation). | Call contract; departed-screen commit evidence. |
| W-03 | [Define both sides of commit](#admission-and-transactional-order). | Process-death/result policy and interruption checks. |
| W-04 | [Make effects repeatable and recoverable](#effects-and-foreground-recovery). | Effect register and database/platform crash-window tests. |
| W-05 | [Expose a new repair as a proposal](#effects-and-foreground-recovery). | Decision state; accepted recovery predicate before drafting. |
| W-06 | [Use the full existing eligibility predicate](#effects-and-foreground-recovery). | Recovery contract and all lifecycle-state cases. |
| W-07 | [Serialize repair and posting with rechecks](#effects-and-foreground-recovery). | Foreground/effect sequence; required interleaving tests. |
| W-08 | [Use one process maintenance coordinator](#effects-and-foreground-recovery). | Foreground sequence and repeated-entry checks. |
| W-09 | [Keep busy/admission outside a recreated screen](#admission-and-transactional-order). | Operation-owner projection; reopen/duplicate-tap test. |
| W-10 | [Resolve command/target key collisions](#admission-and-transactional-order). | Admission register and competing-command cases. |
| W-11 | [Separate transaction admission from slow effects](#admission-and-transactional-order). | Release point and deadline-sensitive-action verification. |
| W-12 | [Decide resubmission without automatic tokens](#admission-and-transactional-order). | Accepted duplicate/idempotency policy. |
| W-13 | [Carry saved identity into command execution](#target-identity-and-navigation). | Immutable command signature and transactional target assertion. |
| W-14 | [Resolve specific ordering invariants](#admission-and-transactional-order). | Transaction order; source-finding disposition; boundary test. |
| W-15 | [Inject execution and propagate cancellation](#scopes-and-cancellation). | Dispatcher/transaction/error contract. |
| W-16 | [Decide orphaned results and receipt need](#scheduled-work-and-results). | Durable/ephemeral result policy. |
| W-17 | [Close cadence, constraints, and retry behavior](#scheduled-work-and-results). | Scheduled-work register and worker configuration assertions. |
| W-18 | [Separate event attempts from worker counters](#scheduled-work-and-results). | Retry budget and mismatched-counter/exhaustion tests. |
| W-19 | [Label nominal waits versus guaranteed timing](#scheduled-work-and-results). | Backoff calculation and scheduling limitations. |
| W-20 | [Leave delivery windows to product definition](#scheduled-work-and-results). | External product question with owner/event. |
| W-21 | [Specify platform identity and enumeration](#effects-and-foreground-recovery). | Notification identity, API support, isolation/overflow checks. |
| O-01 | [Select construction mechanism and revisit trigger](#graph-and-framework-factories). | Construction decision and ADR impact. |
| O-02 | [Own graph instances and acquisition](#graph-and-framework-factories). | Instance register and lazy off-main opening checks. |
| O-03 | [Bridge every framework-created entry point](#graph-and-framework-factories). | Framework-created-object register. |
| O-04 | [Use restored factory handles](#graph-and-framework-factories). | ViewModel provider contract and real-factory check. |
| O-05 | [Wire actual worker factory configuration](#graph-and-framework-factories). | Worker/provider/manifest contract; real-factory tests. |
| O-06 | [Support lightweight headless startup](#startup-time-and-callbacks). | Startup sequence; headless/cold-entry verification. |
| O-07 | [Use one instant/zone authority](#startup-time-and-callbacks). | Clock/time-snapshot contract and controlled-time tests. |
| O-08 | [Pair callback registration and cleanup](#startup-time-and-callbacks). | Receiver/observer API/lifetime register and device checks. |
| O-09 | [Name scheduling ownership and policies](#scheduled-work-and-results). | Unique-work register and enqueue verification. |
| O-10 | [Place every new class and port](#graph-and-framework-factories). | Package/source-set/dependency-boundary table. |
| O-11 | [Promote authorized temporary notes](#artifact-and-decision-ownership). | Formal LLD/ADR lifecycle and verification sections. |
| O-12 | [Clean stale links/status/contracts after resolution](#artifact-and-decision-ownership). | Lock, findings, registers, and required tests synchronized. |
| O-13 | [Preserve accepted sources and record conflicts](#artifact-and-decision-ownership). | Source consistency findings and owner dispositions. |
| O-14 | [Close material choices before handoff](#artifact-and-decision-ownership). | Passing lock; classified nonblocking deferrals only. |
| T-01 | [Exercise real enforcement](#real-enforcement-and-controlled-ports). | Real/fake seam register and production database fixture. |
| T-02 | [Fake variability at injected ports](#real-enforcement-and-controlled-ports). | Clock/scope/permission/notifier/scheduler injection register. |
| T-03 | [Restrict repository fakes to UI mapping](#real-enforcement-and-controlled-ports). | Permitted claims; real Context/DAO/repository rule seam. |
| T-04 | [Assert public behavior](#harness-compatibility-and-assertions). | Event/UiState/persisted/port assertions, not private/emission tests. |
| T-05 | [Map every architecture verification label](#harness-compatibility-and-assertions). | Requirement-to-fixture/assertion evidence matrix. |
| T-06 | [Verify migrated guard catalog and behavior](#executors-contention-and-migrations). | Versioned migration fixture, definition checks, invalid/valid writes. |
| T-07 | [Use production-like real contention](#executors-contention-and-migrations). | File/journal/connections/threads/barriers; gate trace evidence. |
| T-08 | [Control Room execution separately](#executors-contention-and-migrations). | Executor/completion/teardown contract. |
| T-09 | [Couple Clock and timer virtual time](#virtual-time-and-framework-graph). | Shared scheduler and finite boundary test. |
| T-10 | [Replace the graph before framework first use](#virtual-time-and-framework-graph). | Test runner/Application/container/factory entry path. |
| T-11 | [Prove commit after ViewModel clearing](#harness-compatibility-and-assertions). | Real-command barrier/lifecycle/persisted-row verification. |
| T-12 | [Select host/device evidence tiers](#harness-compatibility-and-assertions). | Source-set/runner/API/SQLite matrix and minimum-API SQL threshold. |
| T-13 | [Verify device tooling support](#harness-compatibility-and-assertions). | Supported execution mechanism per required API. |
| T-14 | [Update required evidence after acceptance](#harness-compatibility-and-assertions). | Current obligations and truthful executed/unverified status. |
