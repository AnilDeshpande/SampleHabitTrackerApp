# Habit Tracker V1 — PRD Refinement Observations and Learnings

## Purpose

This record explains how the original Habit Tracker V1 PRD and the first
architecture draft were refined after a reliability, retention, privacy, and
readability review. It preserves the reasoning behind the amendments so future
product and technical reviews can distinguish approved behavior from the
implementation mechanisms that enforce it.

Related documents:

- [Product requirements](habit-tracker-v1-prd.md)
- [Technical architecture](../architecture/habit-tracker-v1-architecture.md)
- [Original idea brief](../idea-briefs/habit-tracker-v1-idea-brief.md)
- [Ideation session notes](../idea-briefs/habit-tracker-v1-ideation-session-notes-2026-08-15.md)

## How the PRD was located and interpreted

The product source was established before changing technical design:

1. The idea brief (IB) supplied the original creator-approved intent and core
   product behavior.
2. The ideation notes (IN) retained earlier alternatives and the reasons they
   were replaced.
3. The PRD product-definition decisions (PD) resolved behavior that had been
   deferred from the idea brief.
4. Later creator-approved PRD amendments superseded an earlier IB or PD choice
   only where they explicitly conflicted. In this review, that applied to
   permanent deletion, timezone boundaries, and local-data retention.

This source hierarchy prevented an architecture document from silently
inventing or changing product behavior. A proposed change was treated as a PRD
amendment when it affected what the user can do, what history remains, where
data may exist, or which outcome is observable. It was treated as an
architecture decision when it specified the transaction, schema, lock, or
Android mechanism used to honor an already-approved behavior.

## Findings and approved resolutions

| Review finding | Why the original wording was incomplete | Approved resolution |
| --- | --- | --- |
| Reconciliation ran on foreground, overview, and WorkManager triggers only. | The app can stay open across a boundary, and foreground/work execution can overlap. A balance-dependent miss calculation could therefore run late, twice, or in different orders. | Every progress save and target change captures one time snapshot and reconciles its affected habit in the same Room transaction. A database-resident transaction gate allows only one reconciliation to apply results at a time. Eligible occurrences are processed by end boundary, habit ID, then occurrence ID. |
| Finalization was described as an update to an open row. | A late command could otherwise edit an occurrence after its period had already closed. | An occurrence has an immutable end boundary. If a command reaches that boundary, the transaction finalizes the occurrence first and rejects the attempted progress or target change. The open-to-finalized update is conditional on `status = OPEN`. |
| Notifications were attempted after a miss committed. | A process crash after the database commit but before notification delivery loses the only delivery attempt. | A miss writes one unique `PENDING` notification event in the same transaction as the result and point effect. A post-commit dispatcher moves events to `POSTED`, `UNAVAILABLE`, or `RETRYABLE`. Its stable occurrence-derived notification ID makes a retry update rather than duplicate a visible notification. |
| Permanent deletion removed the records that explain retained points. | Leaving the aggregate balance while deleting its contributing point effects makes the balance unauditable. | Permanent deletion was replaced by retirement: `ACTIVE → PENDING_RETIREMENT → RETIRED`. History, badges, and point effects remain available in a retired-habits view, while active tracking stops. |
| Final retirement did not define the fate of an open occurrence. | It could remain open indefinitely or receive a later score despite retirement. | Final retirement changes every open occurrence to `CANCELLED` with zero points and no streak or badge effect. |
| Decimal storage was left as a choice, and the current target was absent from the schema. | Different layers could choose incompatible representations or introduce binary rounding. | The architecture fixes normalized decimal `TEXT` with Room `BigDecimal` converters for current target, effective target, and progress. Habit current target is explicit; units are labels only and never converted. |
| On-device data did not define backup/transfer behavior. | Android backup or device-to-device transfer can move local data to another device despite no backend or sync client. | The product boundary now requires disabling cloud backup and device-to-device transfer for Room, DataStore, preferences, and every other user-data file. |
| Future timezone behavior did not fully cover existing open occurrences. | Recalculating a boundary in a new timezone changes an already-created obligation. | Each occurrence persists its local dates, boundary zone ID, and exclusive end epoch. A timezone change affects only occurrences created later. |
| Undo and retirement expiry could occur at the same instant. | Without mutually exclusive conditions, both operations might claim success. | Both use the same persisted deadline in conditional transactions: undo only when `now < deadline`; final retirement only when `now >= deadline`. Only one conditional update can commit. |
| Architecture diagrams used compact ASCII art. | The relationships were difficult to scan and did not communicate transaction or crash behavior clearly. | Existing system, state, and navigation diagrams were converted to Mermaid. Mermaid diagrams were added for reconciliation, notification outbox delivery, and retirement resolution. |

## Review method that exposed the gaps

The successful review pattern was to turn every important sentence into a
failure-mode question rather than accepting the happy path:

1. **Time:** What if the app stays open while the period changes? What exact
   instant determines whether a write is on time?
2. **Concurrency:** What if two triggers handle the same occurrence? Which one
   owns the balance before a points deduction?
3. **Crash recovery:** What durable record remains if the process dies between
   a database commit and an external effect?
4. **Retention:** If an aggregate is retained, do the source records that
   explain it remain available?
5. **Representation:** Is every persisted value exact, typed, nullable or
   non-null by design, indexed for its query, and constrained to valid states?
6. **Platform boundary:** Does “on-device” also exclude OS backup and device
   migration? Does a timezone change alter an existing obligation?
7. **Race conditions:** Do opposite actions use a shared persisted condition so
   only one can win?
8. **Reader comprehension:** Can a reviewer understand lifecycle, transaction,
   and delivery behavior without reconstructing it from prose?

For each affirmative risk, the next step was to identify the smallest durable
invariant: a fixed boundary, a transactional outbox row, a unique key, a
conditional update, a retained audit record, or a renderer-friendly diagram.

## Reusable PRD-refinement checklist

Before technical design begins, review each requirement against the following
questions:

- Does it define the user-visible outcome and its recovery path after restart?
- Does it define what happens at a time boundary and after a timezone change?
- Does it say whether a later action is rejected, corrected, superseded, or
  applied to a future period?
- Does it preserve the source history needed to explain any retained aggregate,
  score, badge, or entitlement?
- Does it distinguish an unavailable external effect from loss of the durable
  domain result?
- Does “private” or “on-device” account for backup, export, transfer, and
  every storage location used by the app?
- Does every terminal lifecycle transition define outstanding open work and
  pending external effects?
- Does the requirement have observable acceptance criteria for the exceptional
  path, not only the happy path?

When a requirement needs deterministic time, shared state, exact arithmetic,
or external delivery, the architecture review should additionally require one
snapshot boundary, one serialization strategy, one exact storage
representation, and an idempotency/retry strategy where an external effect is
involved.

## Architecture-refinement observations and learnings

The PRD amendments established the required outcomes; the architecture work
made their enforcement decision-complete. The following lessons apply to the
architecture itself, not only to the product requirements.

| Architecture area | Observation | Architecture learning and resulting direction |
| --- | --- | --- |
| Trigger versus correctness boundary | Foreground, overview, and WorkManager are useful places to start reconciliation, but none is a reliable boundary for a command that arrives while the process remains open. | Treat triggers as timeliness optimizations. Put correctness in the command/reconciliation transaction itself, using one captured clock snapshot. |
| Transaction ownership | A Room transaction alone does not explain how separately started reconciliation paths are ordered before balance-dependent scoring. | Make ownership explicit with a database-resident singleton reconciliation lock. All reconciliation and period-sensitive commands acquire it before changing rows, so the transaction—not a process-local mutex—serializes finalization. |
| Deterministic aggregate updates | A miss deduction depends on the current app-wide balance. Processing equivalent occurrences in a different order can produce different point effects. | Store and query a total ordering: `endExclusiveEpochMillis`, `habitId`, then `occurrenceId`. State that the same order applies to every reconciliation and command-triggered catch-up. |
| Time modeling | `LocalDate` explains calendar intent but cannot alone decide whether a command crossed a real instant boundary, especially after a timezone change. | Persist both calendar fields and an immutable execution boundary: `startLocalDate`, `endLocalDate`, `boundaryZoneId`, and `endExclusiveEpochMillis`. Future planning reads the current zone; existing rows never do. |
| Command contract | A save or target change that reads first and reconciles later can mutate a closed occurrence. | Define `SaveProgress` and `ChangeTarget` as period-sensitive commands: reconcile the affected habit first in the same transaction, then allow a change only if the habit is `ACTIVE` and the occurrence is still `OPEN`. |
| Schema as a contract | Conceptual tables left storage formats, nullability, state combinations, and query performance to individual implementers. | Specify Room/SQLite types, normalized decimal conversion, foreign keys, unique keys, indexes, and `CHECK` constraints. The schema is a cross-layer contract, not a data-layer detail. |
| Exact quantities | Allowing either decimal text or scaled integers permits incompatible choices and binary floating-point can undermine target comparisons. | Select one representation—normalized decimal `TEXT` exposed as `BigDecimal`—and apply it uniformly to current target, effective target, progress, and target-change requests. |
| Lifecycle closure | A retirement transition is not complete until it defines open work, history visibility, and pending side effects. | Give lifecycle state a single owner (`habits.status`) and resolve dependent records in the same final-retirement transaction: cancel open occurrences, preserve history, and suppress unposted notification events. |
| External-effect reliability | A notification is outside Room's transaction, so a committed domain result and a visible notification cannot be one atomic operation. | Use the transactional outbox pattern: atomically record intent, dispatch only after commit, make delivery state explicit, and use a stable platform ID for idempotent retries. |
| Platform privacy enforcement | Declaring a local-only persistence boundary does not configure Android to keep the data local. | Record the manifest and data-extraction-rule obligations alongside the persistence design, covering Room, DataStore, preferences, and all user-data domains. |
| Presentation architecture | Retired data must be reviewable without accidentally re-enabling active controls. | Model active and retired read models separately: the active overview exposes tracking controls, while the retired-habits view exposes retained immutable history only. |
| Documentation as an architecture tool | ASCII diagrams were compact but obscured state, transaction, and delivery relationships. | Use Mermaid diagrams where a relationship is easier to validate visually than in prose: component flow, independent state machines, navigation, reconciliation sequence, outbox lifecycle, and retirement resolution. Keep package layout and schema as text/table where they are more precise. |

### Architecture review sequence

The following sequence produced a more reliable technical design and should be
reused for future feature deltas:

1. Map each approved requirement to the UI, domain, persistence, platform, and
   verification responsibilities that own it.
2. Mark all values that affect money-like balances, history, or outcomes as
   exact representations with one source of truth.
3. For every time-sensitive operation, identify its snapshot, immutable stored
   boundary, transaction owner, ordering, and late-command behavior.
4. For every external effect, identify the committed event, idempotency key,
   dispatcher, retry states, terminal unavailable state, and in-app fallback.
5. For every lifecycle transition, identify its conditional guard, dependent
   records, retained audit evidence, and UI visibility rules.
6. Translate the final contract into constraints, indexes, state diagrams,
   traceability, and focused repository/device tests before implementation.

### Deferred architecture practice

The initial documentation pass began without an ADR directory or template.
The repository now uses [ADR 0001](../adr/0001-deterministic-on-device-tracking.md)
for the baseline durable tracking contract and includes a reusable ADR template.
If that contract later changes, or implementation introduces a materially
different concurrency, persistence, notification, or privacy pattern, create a
focused successor ADR rather than overwriting the accepted rationale. The
second review below did exactly that:
[ADR 0002](../adr/0002-global-catch-up-and-occurrence-continuity.md)
supersedes ADR 0001.

## Second review (2026-09-23)

A gap review of the approved architecture against the PRD found that several
guarantees in ADR 0001 did not hold end to end, and that the architecture had
silently resolved one product ambiguity. The creator approved nine PRD
decisions (B1–B9) and eight architecture decisions (A1–A8).

### Findings and approved resolutions

| Finding | Why it mattered | Approved resolution |
| --- | --- | --- |
| Commands caught up only the affected habit. | With a balance of 100, a miss and a success at the same boundary scored 109 or 108 depending on which command ran first. | A1: every command runs full catch-up under the gate; the time snapshot is captured after the gate is held. |
| The schema relied on `CHECK` constraints. | Room cannot declare them, and its validation ignores triggers but rejects undeclared indexes. | A2: idempotent triggers created in `onOpen`, verified after every migration. |
| Occurrences stored only an end instant. | A timezone change could create two open occurrences or none, with no rule for which receives progress. | B8 and A3: store the start instant, never overlap, advance a planner watermark, and select the current occurrence by instant. |
| An occurrence could elapse during the 10-second undo window. | Final retirement cancelled a genuinely elapsed period; undo resumed it out of order. | B2 and A4: freeze tracking at confirmation; cancel at final retirement or resume at undo. |
| No screen owned progress entry. | The core loop had no presentation owner, and the shared review screen needed a status-based control policy. | A5: a `RecordProgress` sheet and ViewModel; tracking controls only for `ACTIVE` habits from any route. |
| Nothing reacted to a boundary while the app stayed open. | The UI showed a closed period as open until the next trigger. | A6: a foreground `BoundaryTicker`, which also provides the undo-expiry callback. |
| One notification per missed period. | A long absence could exceed Android's per-app notification limit; the in-app fallback had no appear/clear rule. | B5 and A7: one notification per habit counting unacknowledged misses; acknowledgement on opening the review; `SUPPRESSED` state; bounded retry; both sides cancel on retirement. |
| The architecture chose "next Monday after creation" for a habit created on a Monday. | The PRD's "next full period" was ambiguous; the architecture had invented product behavior. | B1: the current period counts when creation falls on its first day. |
| Active run, late actions, target changes with no open period, clock changes, and input limits were undefined. | Each was a product-visible outcome with no approved answer. | B3, B4, B6, B7, B9. |
| Continuity, undo countdown, scoring arithmetic, overview status, and validation were under-specified. | Each allowed an implementer to diverge from the product rule. | A8: derived continuity, a durable pending-retirement row, integer scoring, a four-state status model, and `HabitInputValidator`. |

### Lessons

- **Partial fixes can break global invariants.** Serializing transactions is
  not enough when an aggregate depends on order; the scope of each catch-up
  must also be global.
- **Check framework capability before relying on a mechanism.** A schema
  contract is only as real as the persistence library's ability to express and
  validate it.
- **Intervals need both ends.** Any time-bounded record whose current instance
  is selected at runtime needs a stored start and end, plus a rule that
  prevents overlap.
- **A pause state must define what happens to work that elapses during it.**
  Lifecycle reviews should include transitions that are temporary, not only
  terminal.
- **Durable state beats transient effects for recoverable UI.** A countdown
  that must survive process death belongs in persisted state, not in a one-off
  UI event.
- **External effects have platform limits.** A per-event notification promise
  must be checked against what the platform will actually display.
- **Architecture must return ambiguity to product.** A choice such as "next
  Monday after creation" looks technical but changes observable behavior.

### Additional review questions

Add these to the review method above for future features:

- Does every command that touches a shared aggregate cover the same scope as
  the ordering guarantee claims?
- Can the chosen library actually express and validate each schema rule?
- What happens to work whose boundary passes while an entity is in a
  temporary state?
- Is any countdown or pending action derived from persisted state?
- Does any external-effect promise exceed a platform limit after a long
  absence?

## Ongoing verification expectations

The architecture's verification strategy should remain tied to these findings:

- Repository tests cover command/reconciliation overlap, an identical final
  balance whichever command triggers catch-up, late progress, target-change,
  and undo outcomes, retirement/undo races, retirement freeze and resume,
  immutable timezone boundaries, timezone overlap and gap, and a clock moved
  back.
- Outbox tests cover commit-before-dispatch crashes, retry exhaustion,
  unavailable and suppressed delivery, per-habit aggregation,
  acknowledgement, the retirement race, and stable notification IDs.
- Data tests cover normalized decimal conversion, foreign-key retention,
  schema-guard presence after every migration, status/result guards, and
  retirement's zero-point cancellation.
- Device checks cover notification permission behavior plus disabled backup and
  device-transfer configuration.
- Documentation review keeps architecture diagrams in Mermaid and validates
  that diagram semantics continue to match the adjacent prose.

## Key learning

A concise PRD can state the intended user journey, but trustworthy tracking
requires its exceptional paths to be equally explicit. The most valuable
refinements came from preserving the product outcome first—history remains
auditable, periods remain fair, and private data remains local—then assigning a
small, durable technical mechanism to each risk.
