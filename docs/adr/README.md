# Architecture Decision Records

Architecture Decision Records (ADRs) capture accepted decisions that establish
or supersede durable data, concurrency, lifecycle, privacy, external-effect,
or core-pattern contracts. Small local design edits remain in the related
architecture document.

| ADR | Status | Decision |
| --- | --- | --- |
| [0001](0001-deterministic-on-device-tracking.md) | Superseded by 0002 | Use deterministic, on-device transactional tracking. |
| [0002](0002-global-catch-up-and-occurrence-continuity.md) | Accepted | Catch up all habits under one gate, keep one open occurrence per habit, freeze retirement, and aggregate missed-result notifications per habit. |
