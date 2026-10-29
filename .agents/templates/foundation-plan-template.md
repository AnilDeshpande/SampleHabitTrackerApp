# Foundation plan — <project> — <date>

Plan for laying the build, dependency, persistence, test and device foundation.
No product behavior is added by this plan.

## Sources and authority

List the PRD, architecture, LLD, ADRs and prototype handoff used, and which statement in
each decides a foundation choice. Note any source conflict and its owner.

## Confirmed terms

Spoken or abbreviated terms resolved with the creator (for example "Mockk" = the mocking
library). Do not plan on an unconfirmed reading.

## Requirement matrix

| Requirement | Source | Foundation action | Phase | Evidence that proves it |
| --- | --- | --- | --- | --- |
| | | | | |

## Scope decisions

| Decision | Chosen | Reason | Decided by |
| --- | --- | --- | --- |
| How much domain code the foundation includes | | | |
| Toolchain baseline (latest stable, or pinned to the IDE) | | | |
| Test tiers and the mandatory minimum-API device | | | |
| CI included | | | |
| Native sub-agents used | No, unless the creator asks | | |

## Phases

| Phase | Work | Gate | Commit |
| --- | --- | --- | --- |
| 0 Preflight | Environment inventory, spec matrix, IDE gate | Inventory complete | none |
| 1 Toolchain | Wrapper, AGP, SDK, version catalog | `help`, `build --dry-run`, `assembleDebug` | |
| 2 Dependencies | Plugins, libraries, processors | assemble, unit tests | |
| 3 App skeleton | Application, DI modules, storage, manifest | assemble, merged-manifest check | |
| 4 Test infrastructure | Host tier, instrumented tier, shared fixtures, smoke tests | unit tests, androidTest assemble | |
| 5 Device tiers | Mandatory minimum-API device, newest managed device | instrumented suites pass with platform evidence | |
| 6 Close-out | Lint, record, evidence, commit | checks green, record written | |

## Risks and how each is checked

| Risk | Check | Fallback |
| --- | --- | --- |
| | | |

## Out of scope

State what the foundation deliberately leaves to later milestones.
