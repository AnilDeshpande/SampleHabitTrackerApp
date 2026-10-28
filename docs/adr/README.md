# Architecture Decision Records

Architecture Decision Records (ADRs) capture accepted decisions that establish
or supersede durable data, concurrency, lifecycle, privacy, external-effect,
or core-pattern contracts. Small local design edits remain in the related
architecture document.

| ADR | Status | Decision |
| --- | --- | --- |
| [0001](0001-deterministic-on-device-tracking.md) | Superseded by 0002 | Use deterministic, on-device transactional tracking. |
| [0002](0002-global-catch-up-and-occurrence-continuity.md) | Accepted | Catch up all habits under one gate, keep one open occurrence per habit, freeze retirement, and aggregate missed-result notifications per habit. |
| [0003](0003-manual-composition-root-and-android-dependency-wiring.md) | Superseded by 0004 | Use a lazy manual AppContainer with explicit ViewModel/worker factories and a replaceable test graph. |
| [0004](0004-hilt-dependency-injection.md) | Accepted | Use Hilt for dependency injection; keep ADR 0003's lifetimes, restoration, worker-factory and test-graph contracts. |
