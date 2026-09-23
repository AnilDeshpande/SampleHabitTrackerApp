# ADR 0002 — Global Catch-Up and Occurrence Continuity

## Metadata

| Field | Value |
| --- | --- |
| Status | Accepted |
| Date | 2026-09-23 |
| Decision owner | Habit Tracker V1 architecture owner |
| Related architecture document | [Habit Tracker V1 Architecture](../architecture/habit-tracker-v1-architecture.md) |
| Product source | [Habit Tracker V1 PRD](../prds/habit-tracker-v1-prd.md), including the 2026-09-23 amendment |
| Supersedes / superseded by | Supersedes [ADR 0001](0001-deterministic-on-device-tracking.md) |

## Context and decision drivers

ADR 0001 made the Room transaction the consistency boundary for
period-sensitive tracking. A second review found that its contract did not
fully hold:

- A command caught up only its affected habit, so the balance-dependent miss
  deduction could be applied in a different order depending on which command
  ran first.
- An occurrence stored only its end instant, so after a timezone change a habit
  could have two open occurrences, or none, with no rule for which one receives
  progress.
- Retirement cancelled every open occurrence, including one whose period had
  already elapsed during the undo window, while undo resumed it out of order.
- One notification per missed period could exceed Android's per-app
  notification limit after a catch-up, and the in-app fallback had no rule for
  when it appears or clears.
- Room cannot declare the `CHECK` constraints the schema contract relied on.

The 2026-09-23 PRD amendment approved the product outcomes: first-period
starts, retirement freeze, late-action outcomes, per-habit notifications with
acknowledgement, one open occurrence per habit, a trusted device clock, and an
input contract. This record restates the full durable contract so a reader
needs only this ADR.

## Decision

Use Room as the on-device source of truth and make its gated transaction the
durable consistency boundary for all tracking.

- **Global catch-up.** Every catch-up and every period-sensitive command
  (create, edit, save progress, confirm retirement, undo retirement) acquires
  the database-resident gate, then captures one time snapshot, then runs
  `ReconcileTracking` for all habits before its own write. Elapsed occurrences
  finalize in `endExclusiveEpochMillis`, `habitId`, `occurrenceId` order.
- **Occurrence continuity.** Each occurrence stores its local dates, zone ID,
  `startInclusiveEpochMillis`, and `endExclusiveEpochMillis`, which never
  change. The planner creates a row only once its period starts, never starts
  it before the previous row's end, skips a candidate left with no time, and
  advances a per-habit watermark so clock or timezone changes never re-plan a
  decided date. A habit therefore has at most one current open occurrence.
- **Late commands.** Late progress is rejected after catch-up finalizes its
  occurrence. A late target change is applied by the cadence rule at the
  snapshot and reports its effective period. A late undo fails and the same
  catch-up finalizes the retirement.
- **Retirement freeze.** Confirmation catches up first, then freezes the
  occurrence that is open. Catch-up does not finalize a frozen occurrence.
  Final retirement cancels it with zero points; undo resumes it, and the same
  transaction's catch-up may finalize it. This is the single documented
  exception to end-boundary ordering.
- **Exact values and guards.** Decimals are normalized `TEXT` exposed through
  `BigDecimal`; scoring uses integer arithmetic. Valid state combinations,
  retirement fields, weekday masks, active-name uniqueness, and a non-negative
  balance are enforced by idempotent SQLite triggers created in Room's
  `onOpen` callback, in addition to repository commands.
- **Notifications.** Each miss inserts one unique outbox event in the
  finalization transaction. A post-commit dispatcher aggregates events into one
  notification per habit, with a stable habit-derived ID, stating the
  unacknowledged-miss count. Delivery states are `PENDING`, `POSTED`,
  `RETRYABLE` (at most five attempts), `UNAVAILABLE`, and `SUPPRESSED`.
  Opening a habit's review acknowledges its misses. Retirement and
  acknowledgement suppress unsent events and cancel the notification after
  commit, and the dispatcher cancels after posting if the habit is no longer
  eligible. The in-app fallback is the count of unacknowledged misses,
  independent of delivery state.
- **Retained history and privacy.** Retirement retains all history and point
  effects. Android backup and device-to-device transfer exclude every
  user-data storage domain, including Room and DataStore.

## Alternatives considered

| Alternative | Why not selected |
| --- | --- |
| Keep per-habit catch-up in commands (ADR 0001) | The scoring result depends on which command runs first. |
| Store only the end instant and pick the current occurrence by local date | Timezone changes produce overlapping or missing current occurrences. |
| Finalize elapsed occurrences of a pending habit normally | The user cannot record progress during the undo window, and the product approved a freeze. |
| One notification per missed period, grouped under a summary | Still exceeds Android's per-app limit after long absences and leaves the fallback undefined. |
| Hand-written table DDL with `CHECK` constraints | Conflicts with Room's generated schema and validation. |
| Repository-only invariant checks | Loses the database-level guarantee that ADR 0001 established. |
| Undeclared partial unique index for active names | Room's post-migration validation rejects undeclared indexes. |

## Consequences

### Positive and required consequences

- The points balance is identical whichever trigger or command performs a
  catch-up.
- Each habit has one unambiguous current occurrence across timezone and clock
  changes.
- Retirement never scores a period the user was locked out of.
- The number of visible notifications is bounded by the number of active
  habits, and the in-app fallback has a precise appear/clear rule.

### Trade-offs and follow-up obligations

- Every command pays for a full catch-up. This is negligible for a
  single-user habit list and must be revisited only if catch-up becomes
  measurably slow.
- Any new write path must use the gate, capture its snapshot after the gate,
  and run full catch-up first; bypassing them violates this ADR.
- Schema guards must remain idempotent and be verified after every migration.
- Implementation must add `BoundaryTicker`, the dispatcher's retry scheduling,
  the notification channel, permission handling, and the backup configuration
  before release.

## Migration, compatibility, and rollback

No tracking data has shipped, so the initial Room schema adopts this contract
directly: `habits.nameKey`, `habits.plannedThroughLocalDate`,
`occurrences.startInclusiveEpochMillis`,
`occurrences.missAcknowledgedAtEpochMillis`, `notification_events.habitId`,
and the `SUPPRESSED` delivery state. Any later change to these durable fields,
state transitions, or guards requires a versioned migration, a migration test
that asserts guard presence, and a successor ADR when this contract changes. A
rollback must not discard user history or leave a database at an unsupported
schema version.

## Verification and links

The [architecture document](../architecture/habit-tracker-v1-architecture.md)
defines the schema, planner, command order, outbox dispatch, retirement flow,
and a focused verification column for every requirement. Implementation must
add tests for:

- order independence of the final balance;
- timezone overlap, gap, and date-line skip, and a clock moved back;
- freeze and resume across the undo window;
- late progress, late target change, and late undo;
- guard presence after migrations;
- notification aggregation, retry exhaustion, crash after post,
  acknowledgement, and the retirement race;
- device checks for permission denial, the blocked channel, the deep-link back
  stack, and backup and transfer exclusion.
