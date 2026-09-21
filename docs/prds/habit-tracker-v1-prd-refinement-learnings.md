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

## Ongoing verification expectations

The architecture's verification strategy should remain tied to these findings:

- Repository tests cover command/reconciliation overlap, deterministic
  finalization order, late-command rejection, retirement/undo races, and
  immutable timezone boundaries.
- Outbox tests cover commit-before-dispatch crashes, retryable failures,
  unavailable delivery, and stable notification IDs.
- Data tests cover normalized decimal conversion, foreign-key retention,
  status/result constraints, and retirement's zero-point cancellation.
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
