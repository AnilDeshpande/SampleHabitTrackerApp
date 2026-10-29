# ADR 0004 — Hilt Dependency Injection

## Metadata

| Field | Value |
| --- | --- |
| Status | Accepted |
| Date | 2026-10-28 |
| Decision owner | Habit Tracker V1 creator |
| Related architecture document | [Architecture](../architecture/habit-tracker-v1-architecture.md) |
| Product source | [PRD](../prds/habit-tracker-v1-prd.md) |
| Supersedes / superseded by | [ADR 0003](0003-manual-composition-root-and-android-dependency-wiring.md), wiring mechanism only / None |

## Context and decision drivers

The creator decided: “we would be using DI Library … not Manuel Road now that is final”. Framework-created components still require the dependency and lifecycle contracts in ADR 0003.

## Decision

Use Hilt in place of ADR 0003's manual AppContainer as the wiring mechanism. ADR 0003's dependency lifetimes, state restoration, worker-factory and test-graph contracts remain in force. Read the LLD's AppContainer references as the Hilt graph for wiring.

## Alternatives considered

| Alternative | Why not selected |
| --- | --- |
| Manual AppContainer | The creator selected a DI library. |

## Consequences

### Positive and required consequences

Hilt provides the graph and framework integration while preserving the existing contracts.

### Trade-offs and follow-up obligations

On AGP 9, Hilt requires 2.59.2+ and KSP 2.3.6+. Verify compatible versions and framework integration during implementation.

## Migration, compatibility, and rollback

The wiring change does not change persisted data or the retained contracts. Any rollback must preserve those contracts and compatible worker construction.

## Verification and links

Verify restored state, worker construction and test graph through the framework entry points against [ADR 0003](0003-manual-composition-root-and-android-dependency-wiring.md) and the [LLD](../design/habit-tracker-v1-low-level-design.md). This decision alone executes no tests.

### Foundation verification (2026-10-29)

The compatibility obligation above was checked in the [foundation build](../implementation/foundation.md): Hilt 2.60.1 and AndroidX Hilt 1.4.0 with KSP 2.3.12 build and run on AGP 9.3.0 (the newest version the creator's Android Studio supports) using its built-in Kotlin; the same stack also built on AGP 9.5.0. The framework entry points were exercised: the production Application installs `HiltWorkerFactory` through WorkManager's on-demand configuration, and a Hilt test graph replaces the clock, dispatchers, application scope and storage on both the host (Robolectric) and instrumented (API 24 and API 36) tiers. Not yet verified, because the code does not exist: workers built through the factory, and restored ViewModel state.
