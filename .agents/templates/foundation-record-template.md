# Foundation implementation — <date>

What was built, why, and what evidence proves it. Written at close-out.

## What was built

| Area | Result |
| --- | --- |
| Toolchain | |
| Annotation processing | |
| Dependency injection | |
| Persistence | |
| Preferences, notifications, privacy | |
| Time and concurrency seams | |
| Test libraries | |
| Pinned, not yet used | |

## Held-back versions

Component, version in use, newer version available, and the gate that blocks it.

## Test tiers and how to run them

| Tier | Source set | Command |
| --- | --- | --- |
| Host | | |
| Instrumented, newest device | | |
| Instrumented, mandatory minimum API | | |

State the device recipe (or link the device-tier procedure) and any serial-pinning rule.

## Verification

| Check | Result |
| --- | --- |
| Each build and test command | |
| Each device tier, with API level and platform SQLite version | |
| Lint (errors / warnings, and each accepted warning with its reason) | |
| App launch per device | |
| Mutation or negative check on any subtle rule | |
| Independent verification | |

## Not covered here

List behavior the foundation cannot yet prove and the milestone that will.

## Next implementation order

Numbered, in the order the spec and dependencies require.
