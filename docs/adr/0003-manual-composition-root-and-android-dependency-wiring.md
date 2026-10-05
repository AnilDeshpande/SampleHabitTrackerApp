# ADR 0003 — Manual Composition Root and Android Dependency Wiring

## Metadata

| Field | Value |
| --- | --- |
| Status | Accepted |
| Date | 2026-10-05 |
| Decision owner | Habit Tracker V1 creator / architecture owner |
| Related architecture document | [Habit Tracker V1 architecture](../architecture/habit-tracker-v1-architecture.md) |
| Related low-level design | [Android low-level design](../design/habit-tracker-v1-low-level-design.md#composition-root-and-dependency-wiring) |
| Product source | [Habit Tracker V1 PRD](../prds/habit-tracker-v1-prd.md), including September 2026 amendments |
| Supersedes / superseded by | None. Selects the manual-root option of the architecture's DI deferral; [ADR 0002](0002-global-catch-up-and-occurrence-continuity.md) remains accepted. |

## Context and decision drivers

Android constructs the Application and Activity; AndroidX providers construct
ViewModels and WorkManager workers. Constructor injection alone does not
explain how those framework-created objects receive the shared repository,
clock, dispatchers, or restored SavedStateHandle. A worker may start the process
without any screen, and a notification may open the app from a cold process.

The architecture allows a simple application-level composition root and defers
manual wiring versus a DI library. The creator selects manual wiring for this
small, single-module app so its dependency graph is visible in one place.
Tests also need a replacement graph: the foreground boundary test must advance
both the fake clock and coroutine delay, and worker tests must use the same
factory path that production WorkManager uses.

## Decision

Use a manual, lazy, thread-safe `AppContainer` owned by
`HabitTrackerApplication`. Keep construction choices at the composition root;
pass dependencies explicitly to services and framework factories rather than
having domain/data classes locate global dependencies.

The container owns one process instance of Clock, injected `io`/`default`/`main`
dispatchers, the application coroutine scope (`SupervisorJob` plus injected
dispatcher), CommandRunner, the Room database, repository, each pure rule
service, command services, notifier and work-scheduler ports, permission/channel
state adapter, worker factory, foreground coordinator, and BoundaryTicker.
The low-level design defines their contracts and test substitutions.

- Construct the lightweight container without opening Room. A shared
  suspending provider initializes/opens the database once off the main thread
  with production converters and the SchemaGuards `onOpen` callback.
- Keep `Application.onCreate` limited to notification-channel creation and
  installing one lightweight process observer. Ticker activation requires
  process `STARTED`; headless worker creation requires no foreground callback.
- Obtain Activity dependencies through Application and create each destination
  ViewModel with `viewModelFactory`, `CreationExtras`, and
  `createSavedStateHandle()`. Record progress owns a destination back-stack
  entry and therefore its own restored input handle.
- Create workers through `HabitTrackerWorkerFactory`. Implement
  `Configuration.Provider` on Application and remove only WorkManager's default
  initializer metadata, retaining other App Startup initializers. Resolve
  WorkManager lazily to avoid recursive configuration/graph initialization.
- Use the injected Clock throughout rules and platform scheduling. TimeSnapshot
  capture remains after database-gate acquisition; this ADR does not change
  transaction ordering or period rules.
- Provide a custom instrumented runner/test Application that installs a test
  container before factories and lifecycle work initialize. FakeClock and
  test dispatchers share a virtual-time scheduler; Room fixtures retain real
  SQLite and explicit executor control.

This is a core-pattern and dependency-ownership decision. It does not authorize
foreground notification cleanup, resolve the undo-order discrepancy, select the
repository-test harness, or supersede ADR 0002's durable tracking contracts.

## Alternatives considered

| Alternative | Why not selected |
| --- | --- |
| Hilt | Generated graph/factory integration is useful at larger scale, but the creator prefers one readable manual graph for this single-module app. Revisit at a multi-module split or when the container becomes difficult to maintain. |
| Construct dependencies independently in each screen or worker | Risks inconsistent clocks, multiple databases/scopes, and test substitutions that do not cover Android-created entry points. |
| Global service lookup inside components | Hides dependency ownership and makes partial test overrides easy to miss. Only framework entry points/factories resolve the container; components receive dependencies explicitly. |

## Consequences

### Positive and required consequences

- Foreground, notification-entry, and headless worker paths obtain the same
  graph without requiring a warm Activity.
- Application-owned user-command work can outlive its screen, while restored
  business input remains scoped to its navigation destination.
- Test containers can control time and external effects through the production
  construction paths, without replacing repository/SQLite rule enforcement.
- DI-framework dependencies are unnecessary for this decision.

### Trade-offs and follow-up obligations

- Factory/container wiring must be maintained explicitly. New Android-created
  components need a documented injection path and corresponding factory test.
- Lazy providers must be thread-safe, avoid main-thread database work, and avoid
  cycles involving WorkManager configuration or scheduler initialization.
- Dispatcher injection does not control Room executors automatically; tests
  must configure and await both execution mechanisms separately.
- A test container must replace the entire graph before first use. Tests own
  scope cancellation, database closure, and executor termination.
- The implementation owner revisits Hilt at a multi-module split or when the
  manual graph becomes hard to maintain. A materially changed ownership pattern
  receives a successor ADR rather than rewriting this accepted rationale.

## Migration, compatibility, and rollback

This documentation decision changes no shipped schema, domain lifecycle,
notification event state, SDK level, or stored user data. Implementation must
register the Application and merge the custom WorkManager initializer removal
before relying on constructor-injected workers; confirm the merged manifest
and factory behavior together.

A future switch to Hilt may change construction mechanics while preserving
dependency lifetimes, restored handles, test seams, and ADR 0002. Reverting a
custom WorkManager setup without a compatible worker factory would break
worker construction; keep configuration and worker construction compatible in
any rollback. No data-destructive migration is permitted or required here.

## Verification and links

The [composition-root verification obligations](../design/habit-tracker-v1-low-level-design.md#composition-root-and-dependency-wiring)
require restored ViewModels through real factories, worker construction through
the real HabitTrackerWorkerFactory, headless/cold startup, one periodic unique
work registration, lifecycle receiver pairing, and an overview boundary test
using shared virtual time plus awaited real database completion.

The [test seam register](../design/habit-tracker-v1-low-level-design.md#seam-register-test-doubles)
requires real rules, repository, commands, and Room guards/converters for
database invariants, explicit executor ownership, and separate file-backed
contention fixtures. The [open questions](../design/habit-tracker-v1-low-level-design.md#open-questions)
remain outside this accepted wiring decision. These are implementation
verification obligations; no runtime tests were executed by this ADR change.
