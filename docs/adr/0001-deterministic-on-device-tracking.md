# ADR 0001 — Deterministic On-Device Tracking

## Metadata

| Field | Value |
| --- | --- |
| Status | Superseded |
| Date | 2026-09-21 |
| Decision owner | Habit Tracker V1 architecture owner |
| Related architecture document | [Habit Tracker V1 Architecture](../architecture/habit-tracker-v1-architecture.md) |
| Product source | [Habit Tracker V1 PRD](../prds/habit-tracker-v1-prd.md) |
| Supersedes / superseded by | Superseded by [ADR 0002](0002-global-catch-up-and-occurrence-continuity.md) on 2026-09-23 |

## Context and decision drivers

Habit Tracker V1 is a single-user Android app whose tracking history, points
balance, retirement history, and missed-target outcomes must remain trustworthy
while the app restarts, remains open across a period boundary, or receives
overlapping foreground and background triggers. Its product boundary also
requires existing occurrence boundaries to remain stable after a timezone
change and user data to remain on the original device.

The product source requires retained point effects to remain explainable after
retirement, exact target comparisons, and a missed-result notification that
remains eligible after a process crash. These requirements need one durable
consistency model rather than independent best-effort operations.

## Decision

Use Room as the on-device source of truth and make its transaction the durable
consistency boundary for period-sensitive tracking.

- Every reconciliation, progress save, and target change captures one time
  snapshot. It acquires the database-resident reconciliation gate, reconciles
  the affected habit before reading or writing it, and processes eligible
  occurrences in `endExclusiveEpochMillis`, `habitId`, `occurrenceId` order.
- Occurrences persist their local dates, boundary zone ID, and exclusive end
  epoch millis at creation. A timezone change affects only later occurrence
  planning. Conditional state updates finalize an `OPEN` occurrence once; a
  command that arrives after its stored boundary finalizes it first and rejects
  the late change.
- Room stores exact quantities as normalized decimal `TEXT` exposed through
  `BigDecimal` converters. The schema records non-nullability, foreign keys,
  unique keys, indexes, and valid state/result combinations as part of the
  cross-layer contract.
- The transaction records result, points effect, continuity, and retained
  history together. Retirement preserves the habit and its audit history,
  cancels still-open occurrences with zero points, and prevents retired habits
  from participating in active tracking.
- Finalizing a miss inserts one unique pending notification-outbox event in the
  same transaction. A post-commit dispatcher uses the stable
  occurrence-derived notification ID and records `POSTED`, `RETRYABLE`, or
  `UNAVAILABLE` delivery state, so retries update rather than duplicate the
  visible notification.
- Android backup and device-to-device transfer exclude all user-data storage
  domains used by the app, including Room, DataStore, preferences, and other
  user-data files.

## Alternatives considered

| Alternative | Why not selected |
| --- | --- |
| Reconcile only on foreground, overview, or WorkManager triggers | The app can remain open across a boundary and triggers can overlap, leaving late writes and balance-dependent finalization order undefined. |
| Use a process-local mutex with independently scheduled reconciliation | It cannot provide a durable ordering boundary across process restarts or independently started database work. |
| Post a notification immediately after committing a missed result | A crash after commit and before posting loses the delivery attempt; retry behavior lacks a durable idempotency record. |
| Delete retired habits while retaining the points balance | The retained aggregate would no longer have source records that explain how it was calculated. |
| Use binary floating point or recompute old boundaries in the current timezone | Both choices can change historical comparisons or already-created obligations. |

## Consequences

### Positive and required consequences

- Late changes are rejected consistently, each occurrence finalizes once, and
  balance-dependent points calculations apply in one defined order.
- Points, badges, and retained history remain auditable after retirement.
- A committed miss remains eligible for notification after a crash without
  creating duplicate visible notifications.
- Timezone changes cannot rewrite an existing occurrence's period boundary,
  and the product's original-device-only privacy boundary is enforceable on
  Android.

### Trade-offs and follow-up obligations

- Reconciliation and period-sensitive commands must share the transaction gate
  and ordering query; adding a new write path that bypasses them violates this
  ADR.
- The Room schema, converters, indexes, and `CHECK` constraints require
  versioned migrations and migration tests as the shipped schema evolves.
- The notification dispatcher needs retry scheduling and permission/unavailable
  handling; the in-app missed-result history remains the user-visible fallback.
- Retirement retains data and exposes it through a read-only retired-habits
  experience instead of providing permanent deletion.
- Android manifest and data-extraction configuration must be implemented and
  device-tested before release.

## Migration, compatibility, and rollback

This is the baseline record for the locked V1 architecture. The initial Room
schema must include these invariants before tracking data is released. Any
later change to an existing durable field, state transition, notification-event
state, or retained-history rule requires a versioned migration, compatibility
test, and—when it changes this contract—a successor ADR. A rollback must not
discard user history or leave a database at an unsupported schema version;
prefer a forward-compatible migration or a release-specific recovery path.

## Verification and links

The [architecture document](../architecture/habit-tracker-v1-architecture.md)
defines the exact schema, reconciliation sequence, outbox lifecycle,
retirement flow, traceability, and focused verification strategy. The
[PRD](../prds/habit-tracker-v1-prd.md) remains the authority for observable
product behavior. Implementation must add repository tests for reconciliation
overlap, ordering, late-command rejection, timezone boundaries, and
retirement races; outbox tests for crash recovery and stable notification IDs;
data tests for exact decimals and constraints; and device checks for
notification behavior plus backup and transfer exclusion.
